# Copyright 2004 ThoughtWorks, Inc
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
require 'jsrpc'

puts "Go to http://localhost:4802/jsrpc/jsrpc_example.html"

browser = Selenium::Browser.new("../javascript").proxy

# Pull a Javascript by-reference object into this script
someArea = browser.document.getElementById("someArea")
puts "The string representation of the JSRMI by-ref object someArea is #{someArea}. That's useful for debugging!"

# Set the value of the text area
someArea.value = "Hello from Ruby #{Time.new}"
puts someArea.value

# Call a function on a by-ref object (the browser itself) with a by-ref argument (the text area)
puts browser.logValueOf(someArea)