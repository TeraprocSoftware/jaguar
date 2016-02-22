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
import sys
import json

def is_json(json_str):
    try:
        json.loads(json_str)
    except ValueError:
        return False
    return True

def quit(message):
    if message:
        click.echo(message, err=True)
    sys.exit(1)

def display(response):
    click.echo(response.status_code)
    if response.text:
        parsed = json.loads(response.text)
        click.echo(json.dumps(parsed, indent=2, sort_keys=True))
