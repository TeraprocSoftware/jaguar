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
from jaguar import jaguar
from rest import Rest
from error import RestError
from utils import display
from utils import quit
from utils import is_json

@jaguar.group()
@click.pass_obj
def policy(config):
    """Jaguar policy operations."""
    pass

@policy.command()
@click.argument('app-id', type=click.INT, metavar='<APP_ID>')
@click.option('--id', default=0,
              metavar='<POLICY_ID>',
              help='ID of the policy.')
@click.option('--type', type=click.Choice(['group', 'instance']),
              metavar='<POLICY_TYPE>',
              help='Type of the policy. Valid types are '
                   '\'group\' and \'instance\'')
@click.pass_obj
def list(config, app_id, id, type):
    """List available application policies.

    Examples:

    List all group policies that belong to application 50:

        $ jaguar policy list 50 --type group

    List instance policy 101 that belongs to application 50:

        $ jaguar policy list 50 --type instance --id 101
    """
    try:
        url = config.server_url + '/v1/applications/' \
              + str(app_id) + '/policies/'
        if type:
            url += type + '/'
            if id > 0:
                url += str(id)

        rest = Rest(url, {'username':config.user})
        display(rest.get())

    except RestError as error:
        quit(error.message)

@policy.command()
@click.argument('app-id', type=click.INT, metavar='<APP_ID>')
@click.argument('type', type=click.Choice(['group', 'instance']),
                metavar='<POLICY_TYPE>')
@click.argument('file', type=click.File('r'), metavar='<POLICY_FILE>')
@click.pass_obj
def create(config, app_id, type, file):
    """Create an application policy.

    Examples:
    """
    try:
        data = ''
        while True:
            chunk = file.read(1024)
            if not chunk:
                break
            data += chunk
        if not is_json(data):
            raise click.BadParameter('The policy file ' + file.name \
                                     + ' is not a valid json file.')
        url = config.server_url + '/v1/applications/' + str(app_id) \
              + '/policies/' + type
        headers = {'username':config.user, 'Content-Type':'application/json'}
        rest = Rest(url, headers, data)
        display(rest.post())
    except RestError as error:
        quit(error.message)

@policy.command()
@click.argument('app-id', type=click.INT, metavar='<APP_ID>')
@click.argument('type', type=click.Choice(['group', 'instance']),
                metavar='<POLICY_TYPE>')
@click.argument('id', type=click.INT, metavar='<POLICY_ID>')
@click.argument('file', type=click.File('r'), metavar='<POLICY_FILE>')
@click.pass_obj
def update(config, app_id, type, id, file):
    """Update an application policy.
    """
    try:
        data = ''
        while True:
            chunk = file.read(1024)
            if not chunk:
                break
            data += chunk
        if not is_json(data):
            raise click.BadParameter('The policy file ' + file.name \
                                     + ' is not a valid json file.')
        url = config.server_url + '/v1/applications/' + str(app_id) \
              + '/policies/' + type + '/' + str(id)
        headers = {'username':config.user, 'Content-Type':'application/json'}
        rest = Rest(url, headers, data)
        display(rest.put())
    except RestError as error:
        quit(error.message)

def abort_if_false(ctx, param, value):
    if not value:
        ctx.abort()

@policy.command()
@click.argument('app-id', type=click.INT, metavar='<APP_ID>')
@click.argument('type', type=click.Choice(['group', 'instance']),
                metavar='<POLICY_TYPE>')
@click.argument('id', type=click.INT, metavar='<POLICY_ID>')
@click.option('--yes', is_flag=True, callback=abort_if_false,
              expose_value=False,
              help='Delete an application policy without confirmation.',
              prompt='Are you sure you want to delete this application'
                     ' policy?')
@click.pass_obj
def delete(config, app_id, type, id):
    """Delete an application policy.
    """
    try:
        url = config.server_url + '/v1/applications/' + str(app_id) \
              + '/policies/' + type + '/' + str(id)
        headers = {'username':config.user, 'Content-Type':'application/json'}
        rest = Rest(url, headers)
        display(rest.delete())
    except RestError as error:
        quit(error.message)