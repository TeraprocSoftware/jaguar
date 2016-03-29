#!/usr/bin/env python

'''
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
'''

import click
import subprocess
import json
import ConfigParser
import StringIO
import resource
from sys import stderr
from sys import stdout
from sys import exit
from os import environ
from os import access
from os import X_OK
from os import pathsep
from os import fork
from os import setsid
from os import close
from os import dup2
from os import O_RDWR
from os import kill
from os.path import join
from os.path import expanduser
from os.path import join
from os.path import exists
from os.path import isfile
from os.path import split

CONF = "conf"
ENV_KEYS = ["JAVA_HOME", "JAGUAR_HOME", "JAGUAR_CONF_DIR"]
JAGUAR_HOME = "JAGUAR_HOME"
JAGUAR_CONF_DIR = "JAGUAR_CONF_DIR"

def check_output(*popenargs, **kwargs):
    r"""Run command with arguments and return its output as a byte string.
    Backported from Python 2.7 as it's implemented as pure python on stdlib.
    >>> check_output(['/usr/bin/python', '--version'])
    Python 2.6.2
    """
    process = subprocess.Popen(stdout=subprocess.PIPE, *popenargs, **kwargs)
    output, unused_err = process.communicate()
    retcode = process.poll()
    if retcode:
        cmd = kwargs.get("args")
        if cmd is None:
            cmd = popenargs[0]
        error = subprocess.CalledProcessError(retcode, cmd)
        error.output = output
        raise error
    return output

def daemonize():
    try:
        if fork() != 0:
            # Parent
            exit(0)
    except OSError as e:
        quit("Unable to fork, errno: {0}.".format(e.errno))
    # This is the child process. Continue.
    # Become group leader
    if setsid() == -1:
        # Uh oh, there was a problem.
        quit("Unable to setsid.")
    # Close all file descriptor
    for fd in range(3, resource.getrlimit(resource.RLIMIT_NOFILE)[0]):
        try:
            close(fd)
        except OSError:
            pass
    # Redirect 0, 1, 2 to /dev/null
    import os
    devnull_fd = os.open("/dev/null", O_RDWR)
    dup2(devnull_fd, 0)
    dup2(devnull_fd, 1)
    dup2(devnull_fd, 2)

def out(toStdErr, text) :
    if toStdErr:
        stderr.write(text)
    else:
        stdout.write(text)

def flush(toStdErr) :
    if toStdErr:
        stderr.flush()
    else:
        stdout.flush()

def is_exe(fpath):
    return isfile(fpath) and access(fpath, X_OK)

def which(program):
    fpath, fname = split(program)
    if fpath:
        if is_exe(program):
            return program
    else:
        for path in environ["PATH"].split(pathsep):
            path = path.strip('"')
            exe_file = join(path, program)
            if is_exe(exe_file):
                return exe_file
    return None

def execute_envsh(confDir):
    envscript = '%s/jaguar-env.sh' % confDir
    if exists(envscript):
        envCmd = 'source %s && env' % envscript
        command = ['bash', '-c', envCmd]
        proc = subprocess.Popen(command, stdout = subprocess.PIPE)
        for line in proc.stdout:
            (key, _, value) = line.strip().partition("=")
            if key in ENV_KEYS:
                environ[key] = value
        proc.communicate()

def dir_must_exist(dirname):
    if not exists(dirname):
        raise click.UsageError('Directory {0} does not exist.'.format(dirname))
    return dirname

def parse_conf_dir(conf):
    if conf:
        return conf
    if environ.get(JAGUAR_HOME):
        conf = join(environ.get(JAGUAR_HOME), CONF)
    if not conf:
        raise click.UsageError('Jaguar configuration directory cannot be located.')
    return conf

def parse_conf(config):
    endpoint = config.server.get('endpoint')
    conf = config.conf
    if not endpoint:
        conf_path = expanduser(join(conf, 'jaguar.conf'))
        try:
            conf_str = '[root]\n' + open(conf_path, 'r').read()
        except IOError as e:
            import errno
            if e.errno == errno.EACCES:
                raise click.FileError(conf_path, 'Check file permission.')
            else:
                raise click.FileError(conf_path, 'Check if the file exists.')
        conf_fp = StringIO.StringIO(conf_str)
        confs = ConfigParser.RawConfigParser()
        confs.readfp(conf_fp)
        if confs.has_option('root', 'server'):
            endpoint = confs.get('root', 'server')
        else:
            raise click.BadParameter('Failed to get Jaguar server from '
                                     'configuration file: ' + conf_path)
    server_url_parts = endpoint.split(':')
    if len(server_url_parts) != 2:
        raise click.BadParameter('Wrong Jaguar server format: ' + endpoint +
                                 '. Jaguar server must be a URL in the format of '
                                 ' host:port.')
    server_url = 'http://' + endpoint
    server_host = server_url_parts[0]
    server_port = server_url_parts[1]
    config.server['url'] = server_url
    config.server['host'] = server_host
    config.server['port'] = server_port

def pid_exists(pid):
    import errno
    if pid < 0:
        return False
    try:
        kill(pid, 0)
    except OSError as e:
        return e.errno == errno.EPERM
    else:
        return True

def is_json(json_str):
    try:
        json.loads(json_str)
    except ValueError:
        return False
    return True

def quit(message):
    if message:
        click.echo(message, err=True)
    exit(1)

def display(response):
    click.echo(response.status_code)
    if response.text:
        parsed = json.loads(response.text)
        click.echo(json.dumps(parsed, indent=2, sort_keys=True))
