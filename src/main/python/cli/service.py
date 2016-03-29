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
import time
import sys
import signal
from os.path import join
from os import environ
from os import kill
from threading import Thread
from jaguar import jaguar
from utils import check_output
from utils import daemonize
from utils import execute_envsh
from utils import dir_must_exist
from utils import which
from utils import out
from utils import flush
from utils import pid_exists
from utils import parse_conf
from utils import quit

DEBUG = False
ON_POSIX = 'posix' in sys.builtin_module_names

JAGUAR_HOME = "JAGUAR_HOME"
JAGUAR_CLASSNAME = "com.teraproc.jaguar.JaguarApplication"
JAGUAR_PROPERTIES = "jaguar.properties"
JAGUAR_CONF_ARGS = "--spring.config.location={0}"
JAGUAR_LOGBACK = "logback.xml"
JAGUAR_LOG_ARGS = "--logging.config={0}"
DEFAULT_JVM_OPTS = "-Djava.net.preferIPv4Stack=true -Xmx2048m"
JAGUAR_PORT_ARGS = "--server.port={0}"

finished = False

def debug(text):
    if DEBUG: print '[DEBUG] ' + text

def print_output(name, src, toStdErr):
    try:
        while not finished and src is not None:
            out(toStdErr, src.readline())
            flush(toStdErr)
        src.close()
    except:
        pass

def run(commandline):
    global finished
    debug ("Executing : %s" % commandline)
    exe = subprocess.Popen(commandline,
                           stdin=subprocess.PIPE,
                           stdout=subprocess.PIPE,
                           stderr=subprocess.PIPE,
                           shell=False,
                           bufsize=1,
                           close_fds=ON_POSIX)

    t = Thread(target=print_output, args=("stdout", exe.stdout, False))
    t.daemon = True
    t.start()
    t2 = Thread(target=print_output, args=("stderr", exe.stderr, True))
    t2.daemon = True
    t2.start()
    debug("Waiting for completion")
    while exe.poll() is None:
        # process is running; grab output and echo every line
        time.sleep(1)
    finished = True
    debug("Completed with exit code : %d" % exe.returncode)
    t.join()
    t2.join()
    return exe.returncode

@jaguar.group()
@click.pass_obj
def service(config):
    """Jaguar service operations."""
    try:
        parse_conf(config)
    except:
        config.server['port'] = '8080'

@service.command()
@click.option('--daemon/--no-daemon', default=False,
              help='Specify if Jaguar service should be '
                   'daemonized or not. Default value '
                   'is False.')
@click.pass_obj
def start(config, daemon):
    """Start Jaguar service.

    Examples:

    $ jaguar service start

    $ jaguar service --daemon start
    """
    # Make sure java is available
    if not environ.get(JAGUAR_HOME):
        raise click.UsageError('JAGUAR_HOME envrionment variable is not set.')
    dir_must_exist(environ.get(JAGUAR_HOME))
    execute_envsh(config.conf)
    if environ["JAVA_HOME"] is not None and environ["JAVA_HOME"]:
        prg = join(environ["JAVA_HOME"], "bin", "java")
    else:
        prg = which("java")

    if prg is None:
        quit("Cannot locate java.")
    # Jaguar conf
    properties = join(config.conf, JAGUAR_PROPERTIES)
    args = JAGUAR_CONF_ARGS.format(properties).split()
    # Jaguar log
    logback = join(config.conf, JAGUAR_LOGBACK)
    args.extend(JAGUAR_LOG_ARGS.format(logback).split())
    # Jaguar port
    args.extend(JAGUAR_PORT_ARGS.format(config.server['port']).split())
    # Jaguar classpath
    classpath = join(environ.get(JAGUAR_HOME), 'lib', '*')
    jvm_opts_list = DEFAULT_JVM_OPTS.split()
    # Shall we daemonize?
    if daemon:
        daemonize()
        environ['LOG_APPENDER'] = 'FILE'
    else:
        environ['LOG_APPENDER'] = 'STDOUT'

    commandline = [prg]
    commandline.extend(jvm_opts_list)
    commandline.append("-classpath")
    commandline.append(classpath)
    commandline.append(JAGUAR_CLASSNAME)
    commandline.extend(args)

    return run(commandline)

@service.command()
@click.pass_obj
def status(config):
    """Show status of Jaguar service.

    Examples:

    $ jaguar service status
    """
    statusCmd = 'jps -l | grep {0}'.format(JAGUAR_CLASSNAME)
    command = ['bash', '-c', statusCmd]
    debug ("Executing: {0}".format(command))
    try:
        subprocess.check_call(command, stderr=subprocess.STDOUT)
    except subprocess.CalledProcessError:
        click.echo('No Java process {0} is found.'.format(JAGUAR_CLASSNAME),
                   err=True)

@service.command()
@click.pass_obj
def stop(config):
    """Stop a running Jaguar service on the local host.

    Examples:

    $ jaguar service stop
    """
    statusCmd = 'jps -l | grep {0}'.format(JAGUAR_CLASSNAME)
    command = ['bash', '-c', statusCmd]
    debug ("Executing: {0}".format(command))
    try:
        output = check_output(command, stderr=subprocess.STDOUT)
        pid = int(output.split()[0])
        r = 0
        while pid_exists(pid) and r < 10:
            kill(pid, signal.SIGKILL)
            r += 1
            time.sleep(1)
        if r == 10:
            click.echo("Failed to stop Jaguar service.")
        else:
            click.echo("Jaguar service has been stopped successfully.")
    except subprocess.CalledProcessError:
        click.echo('No Java process {0} is found.'.format(JAGUAR_CLASSNAME),
                   err=True)
    except ValueError:
        click.echo('Failed to convert {0} into integer.'.format(pid),
                   err=True)

