// Copyright 2011 Software Freedom Conservatory
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#ifndef WEBDRIVER_IE_FINDELEMENTCOMMANDHANDLER_H_
#define WEBDRIVER_IE_FINDELEMENTCOMMANDHANDLER_H_

#include <ctime>
#include "../Browser.h"
#include "../IECommandHandler.h"
#include "../IECommandExecutor.h"

namespace webdriver {

class FindElementCommandHandler : public IECommandHandler {
 public:
  FindElementCommandHandler(void) {
  }

  virtual ~FindElementCommandHandler(void) {
  }

 protected:
  void ExecuteInternal(const IECommandExecutor& executor,
                       const LocatorMap& locator_parameters,
                       const ParametersMap& command_parameters,
                       Response* response) {
    ParametersMap::const_iterator using_parameter_iterator = command_parameters.find("using");
    ParametersMap::const_iterator value_parameter_iterator = command_parameters.find("value");
    if (using_parameter_iterator == command_parameters.end()) {
      response->SetErrorResponse(400, "Missing parameter: using");
      return;
    } else if (value_parameter_iterator == command_parameters.end()) {
      response->SetErrorResponse(400, "Missing parameter: value");
      return;
    } else {
      std::string mechanism = using_parameter_iterator->second.asString();
      std::string value = value_parameter_iterator->second.asString();

      int timeout = executor.implicit_wait_timeout();
      clock_t end = clock() + (timeout / 1000 * CLOCKS_PER_SEC);
      if (timeout > 0 && timeout < 1000) {
        end += 1 * CLOCKS_PER_SEC;
      }

      int status_code = SUCCESS;
      Json::Value found_element;
      do {
        status_code = executor.LocateElement(ElementHandle(),
                                             mechanism,
                                             value,
                                             &found_element);
        if (status_code == SUCCESS) {
          response->SetSuccessResponse(found_element);
          return;
        }
        if(status_code == EINVALIDSELECTOR) {
          response->SetErrorResponse(status_code,
            "The xpath expression '" + value + "' cannot be evaluated or does not" +
            "result in a WebElement");
          return;
        }
        if (status_code == EUNHANDLEDERROR) {
          response->SetErrorResponse(status_code, 
            "Unknown finder mechanism: " + mechanism);
          return;
        }
        // Release the thread so that the browser doesn't starve.
        ::Sleep(FIND_ELEMENT_WAIT_TIME_IN_MILLISECONDS);
      } while (clock() < end);

      response->SetErrorResponse(status_code, 
        "Unable to find element with " + mechanism + " == " + value);
      return;
    }
  }
};

} // namespace webdriver

#endif // WEBDRIVER_IE_FINDELEMENTCOMMANDHANDLER_H_
