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
import ConfigParser
from os.path import join
from os.path import expanduser
from getpass import getuser

class Config(object):
    def __init__(self, server_url, server_host, server_port, user, verbose):
        self.server_url = server_url
        self.server_host = server_host
        self.server_port = server_port
        self.user = user
        self.verbose = verbose


@click.group()
@click.option('--server', envvar='JAGUAR_SERVER', default='',
              metavar='<URL>',
              help='URL as host:port of the Jaguar server. '
                   'This is the same as JAGUAR_SERVER environment variable.')
@click.option('--conf', envvar='JAGUAR_CONF_DIR',
              metavar='<PATH>', type=click.Path(exists=True, resolve_path=True),
              help='Path to the Jaguar configuration directory. '
                   'This is the same as JAGUAR_CONF_DIR environment variable.')
@click.option('--verbose', envvar='JAGUAR_VERBOSE', is_flag=True,
              default=False,
              help='Print verbose information. '
                   'This is the same as JAGUAR_VERBOSE environment variable.')
@click.pass_context
def jaguar(ctx, server, conf, verbose):
    """The Jaguar CLI"""

    if not server and not conf:
        raise click.UsageError('Neither Jaguar server nor Jaguar '
                               'configuration directory is specified.')

    if not server:
        config = ConfigParser.RawConfigParser()
        name = expanduser(join(conf, 'jaguar.conf'))
        files = config.read(name)
        if len(files) > 0:
            try:
                server = config.get('client', 'server')
            except ConfigParser.Error:
                raise click.BadParameter('Failed to get Jaguar server from '
                                         'configuration file: ' + name)
        else:
            raise click.FileError(name, 'Check if the file exists.')

    server_url_parts = server.split(':')
    if len(server_url_parts) != 2:
        raise click.BadParameter('Wrong Jaguar server format: ' + server +
                                 '. Jaguar server must be a URL in the format of '
                                 ' host:port.')
    server_url = 'http://' + server
    server_host = server_url_parts[0]
    server_port = server_url_parts[1]
    user = getuser()

    ctx.obj = Config(server_url, server_host, server_port, user, verbose)

