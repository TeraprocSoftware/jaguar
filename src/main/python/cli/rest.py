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

import requests
from requests.exceptions import RequestException
from error import RestError

class Rest(object):

    def __init__(self, url, headers, data=None):
        self.url = url
        self.headers = headers
        self.data = data

    def _action(self, func, **kwargs):
        try:
            return func(self.url, **kwargs)
        except RequestException as error:
            raise RestError('Failed to connect to the Jaguar server: '
                            + str(error.message))
        except Exception as error:
            raise RestError(str(error.message))

    def get(self):
        return self._action(requests.get, headers=self.headers)

    def post(self):
        if not self.data:
            raise RestError('Failed to send post request to Jaguar '
                            'server as the payload is empty.')
        return self._action(requests.post, headers=self.headers,
                            data=self.data)

    def put(self):
        if not self.data:
            raise RestError('Failed to send put request to Jaguar '
                            'server as the payload is empty.')
        return self._action(requests.put, headers=self.headers,
                            data=self.data)

    def delete(self):
        return self._action(requests.delete, headers=self.headers)
