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
import json
import requests

@jaguar.group()
@click.pass_obj
def application(config):
    """Jaguar application operations."""
    pass

@application.command()
@click.option('--id', default=0,
              metavar='<APP_ID>',
              help='ID of the application.')
@click.pass_obj
def list(config, id):
    """List registered applications.

    Examples:

    $ jaguar application list

    $ jaguar application list --id 50
    """
    try:
        if id > 0:
            url = config.server_url + '/v1/applications/' + str(id)
        else:
            url = config.server_url + '/v1/applications'
        headers = {'username':config.user}
        rest = Rest(url, headers)
        display(rest.get())

    except RestError as error:
        quit(error.message)

@application.command()
@click.argument('id', metavar='<ID>', nargs=1, required=True)
@click.option('--enable/--disable', default=True,
              help='Specify if policy evaluation of this application '
                   'should be enabled or disabled. Default value '
                   'is True.')
@click.pass_obj
def update(config, id, enable):
    """Update an application"""
    try:
        url = config.server_url + '/v1/applications/' + str(id)
        headers = {'username':config.user, 'Content-Type':'application/json'}
        rest = Rest(url, headers)
        # check if the application exists
        response = rest.get()
        if response.status_code != requests.codes.ok:
            quit('Error: Application ' + str(id) + ' does not exist.')
        result = response.json()
        # update the field
        result['enabled'] = enable
        # post the data back
        data = json.dumps(result)
        rest = Rest(url, headers, data)
        display(rest.put())
    except RestError as error:
        quit(error.message)

@application.command()
@click.argument('name', metavar='<NAME>', nargs=1, required=True)
@click.option('--provider',
              metavar='<APP_MANAGER>',
              default='SLIDER',
              help='The third-party application manager. Default value '
                   'is SLIDER.')
@click.option('--enable/--disable', default=True,
              help='Specify if policy evaluation of this application '
                   'should be enabled or disabled. Default value '
                   'is True.')
@click.pass_obj
def register(config, name, provider, enable):
    """Register an application for monitor and evaluation."""
    try:
        url = config.server_url + '/v1/applications'
        headers = {'username':config.user, 'Content-Type':'application/json'}
        data = json.dumps({'name':name, 'provider':provider, 'enabled':enable})
        rest = Rest(url, headers, data)
        display(rest.post())
    except RestError as error:
        quit(error.message)

def abort_if_false(ctx, param, value):
    if not value:
        ctx.abort()

@application.command()
@click.argument('id', metavar='<ID>', type=click.INT, nargs=1, required=True)
@click.option('--yes', is_flag=True, callback=abort_if_false,
              expose_value=False,
              help='Unregister an application without confirmation.',
              prompt='Are you sure you want to unregister this application?')
@click.pass_obj
def unregister(config, id):
    """Unregister an application."""
    try:
        url = config.server_url + '/v1/applications/' + str(id)
        headers = {'username':config.user}
        rest = Rest(url, headers)
        display(rest.delete())
    except RestError as error:
        quit(error.message)
