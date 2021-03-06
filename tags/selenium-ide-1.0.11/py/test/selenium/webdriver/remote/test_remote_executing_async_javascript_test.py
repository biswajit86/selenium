#!/usr/bin/python
#
# Copyright 2008-2010 WebDriver committers
# Copyright 2008-2010 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
import os
import socket
import subprocess
import time
import urllib
import signal
from selenium import webdriver
from selenium.test.selenium.webdriver.common import executing_async_javascript_test 
from selenium.test.selenium.webdriver.common.webserver import SimpleWebServer

SERVER_ADDR = "localhost"
DEFAULT_PORT = 4444

def wait_for_server(url, timeout):
    start = time.time()
    while time.time() - start < timeout:
        try:
            urllib.urlopen(url)
            return 1
        except IOError:
            time.sleep(0.2)

    return 0

def setup_module(module):
    _socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    url = "http://%s:%d/wd/hub" % (SERVER_ADDR, DEFAULT_PORT)
    try:
        _socket.connect((SERVER_ADDR, DEFAULT_PORT))
        print ("The remote driver server is already running or something else"
               "is using port %d, continuing..." % DEFAULT_PORT)
    except:
        print ("Starting the remote driver server")
        RemoteExecutingAsyncJavaScriptTests.server_proc = subprocess.Popen(
            "java -jar build/java/server/src/org/openqa/selenium/server/server-standalone.jar",
            shell=True)

        assert wait_for_server(url, 10), "can't connect"
        print "Server should be online"

    webserver = SimpleWebServer()
    webserver.start()
    RemoteExecutingAsyncJavaScriptTests.webserver = webserver
    RemoteExecutingAsyncJavaScriptTests.driver = webdriver.Remote(desired_capabilities = webdriver.DesiredCapabilities.FIREFOX)

class RemoteExecutingAsyncJavaScriptTests(executing_async_javascript_test.ExecutingAsyncJavaScriptTests):
    pass


def teardown_module(module):
    try:
        RemoteExecutingAsyncJavaScriptTests.driver.quit()
    except AttributeError:
        pass
    try:
        RemoteExecutingAsyncJavaScriptTests.webserver.stop()
    except AttributeError:
        pass    
    # FIXME: This does not seem to work, the server process lingers
    try:
        os.kill(RemoteExecutingAsyncJavaScriptTests.server_proc.pid, signal.SIGTERM)
        time.sleep(5)
    except:
        pass


