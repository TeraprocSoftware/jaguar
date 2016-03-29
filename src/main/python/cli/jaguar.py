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
from getpass import getuser
from utils import parse_conf_dir
from utils import dir_must_exist

class Config(object):
    def __init__(self, server, conf, user, verbose):
        self.server = dict()
        self.server['endpoint'] = server
        self.conf = conf
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
    conf = dir_must_exist(parse_conf_dir(conf))
    user = getuser()
    ctx.obj = Config(server, conf, user, verbose)

