#include "webdriver/chrome_driver_plugin.h"

#include "webdriver/http_handler.h"
#include "webdriver/http_responses.h"
#include "webdriver/http_server.h"
#include "webdriver/javascript_executor.h"
#include "webdriver/logging.h"
#include "webdriver/webdriver_utils.h" //for kMaxSize_tDigits

#include "interactions.h"

#include <stdio.h>

using namespace std;

namespace webdriver {
ChromeDriverPlugin::ChromeDriverPlugin(size_t session_id,
                                       HttpServer *http_server,
                                       JavascriptExecutor *javascript_executor) :
    session_id_(session_id),
    context_("foo"),
    http_server_(http_server),
    javascript_executor_(javascript_executor),
    is_ready_(false)
#if defined(WIN32)
    ,current_handle_(0)
#endif
{}

bool ChromeDriverPlugin::ExecuteJavascript(string script) {
  if (javascript_executor_ == NULL) {
    return false;
  }
  return javascript_executor_->execute(script);
}

void ChromeDriverPlugin::DeleteFields() {
  is_ready_ = false;
  delete javascript_executor_;
  delete http_server_;
}

bool ChromeDriverPlugin::IsReady() {
  return (javascript_executor_ != NULL && javascript_executor_->is_ready());
}

#if defined(WIN32)
void ChromeDriverPlugin::GiveWindow(HWND handle) {
  //Sometimes the tab's HWND seems to be its direct parent,
  //sometimes grandparent, sometimes great-grandparent, so we also check
  //that we have the class we expect.
  //ALL OF THIS MAY CHANGE IN CHROME WITHOUT WARNING!!!
  HWND window = handle;
  const wchar_t *desired_class_name = L"Chrome_RenderWidgetHostHWND";
  wchar_t *class_name_w = new wchar_t[wcslen(desired_class_name) + 1];
  
  //If we go more than 10 deep in our searching for the window,
  //something is probably quite wrong...
  for (size_t i = 0; i < 10 && window != 0; ++i) {
    GetClassName(window, class_name_w, wcslen(desired_class_name) + 1);
    if (!wmemcmp(class_name_w, desired_class_name, wcslen(desired_class_name))) {
      break;
    }
    window = GetParent(window);
  }
  delete[] class_name_w;
  current_handle_ = window;
  char buf[1000];
  sprintf(buf, "Current handle: %x\n", current_handle_);
  WEBDRIVER_LOG(buf);
}
#endif

JavascriptExecutor *ChromeDriverPlugin::javascript_executor() {
  return javascript_executor_;
}

const size_t ChromeDriverPlugin::session_id() {
  return session_id_;
}

void ChromeDriverPlugin::SendGeneralFailure() {
  http_server_->send(kFailureResponse);
}

//The actual construction of HTTP messages is ugly.  See my comment in
//webdriver/http_responses.h -danielwh

void ChromeDriverPlugin::SendNotFound() {
  char *response_data = new char[strlen(kNotFoundResponseData) +
      kMaxSize_tDigits + strlen(context_) + 1];
  sprintf(response_data, kNotFoundResponseData, session_id_, context_);
  char *response = new char[strlen(kNotFoundResponse) + kMaxSize_tDigits + 
      strlen(response_data) + 1];
  sprintf(response, kNotFoundResponse, strlen(response_data), response_data);
      
  http_server_->send(response);
  
  delete[] response;
  delete[] response_data;
}

void ChromeDriverPlugin::SendStringValue(string value) {
  stringstream s;
  s << "\"" << value << "\"";
  SendValue(s.str());
}

void ChromeDriverPlugin::SendValue(string value) {
  string escaped_value = EscapeChar(value, '\n');
  char *response_data = new char[
      strlen(kSendValueResponseData) + kMaxSize_tDigits +
      escaped_value.length() + strlen(context_) + 1];
  sprintf(response_data, kSendValueResponseData,
      session_id_, escaped_value.c_str(), context_);
  char *response = new char[strlen(kOkResponse) +
      kMaxSize_tDigits + strlen(response_data) + 1];
  sprintf(response, kOkResponse, strlen(response_data), response_data);

  http_server_->send(response);
  
  delete[] response;
  delete[] response_data;
}

void ChromeDriverPlugin::CreateSession(string capabilities) {
  //TODO(danielwh): Don't use hard-coded port
  
  capabilities_ = capabilities;
  char *create_session_response = new char[
      strlen(kAcceptCreateSessionResponse) + 4 + kMaxSize_tDigits +
      strlen(context_) + 1];
  sprintf(create_session_response, kAcceptCreateSessionResponse, 7601, session_id_, context_);
  
  http_server_->send(create_session_response);
  
  delete[] create_session_response;
}

void ChromeDriverPlugin::ConfirmSession() {
  char *confirm_session_response_data = new char[
      strlen(kConfirmCreateSessionResponseData) + kMaxSize_tDigits + 
      capabilities_.length() + 1];
  sprintf(confirm_session_response_data, kConfirmCreateSessionResponseData,
      session_id_, capabilities_.c_str());
  char *confirm_session_response = new char[
      strlen(kOkResponse) + kMaxSize_tDigits + 
      strlen(confirm_session_response_data) + 1];
  sprintf(confirm_session_response, kOkResponse,
      strlen(confirm_session_response_data), confirm_session_response_data);
      
  http_server_->send(confirm_session_response);
  
  delete[] confirm_session_response_data;
  delete[] confirm_session_response;
}

void ChromeDriverPlugin::DeleteSession() {
  http_server_->send(kNoContentReseponse);
}

void ChromeDriverPlugin::ConfirmUrlLoaded() {
  http_server_->send(kNoContentReseponse);
}

void ChromeDriverPlugin::ReturnGetTitleSuccess(string title) {
  SendStringValue(title);
} 

void ChromeDriverPlugin::ReturnGetTitleFailure() {
  SendNotFound();
}

void ChromeDriverPlugin::ReturnGetElementFound(string internal_element_ids) {
  SendValue(internal_element_ids);
}

void ChromeDriverPlugin::ReturnGetElementNotFound(string identifier_string) {
  char *response_data = new char[strlen(kElementNotFoundResponseData) +
      kMaxSize_tDigits + strlen(context_) + identifier_string.length() + 1];
  sprintf(response_data, kElementNotFoundResponseData, session_id_, context_, identifier_string.c_str());
  char *response = new char[strlen(kNotFoundResponse) + kMaxSize_tDigits + 
      strlen(response_data) + 1];
  sprintf(response, kNotFoundResponse, strlen(response_data), response_data);

  http_server_->send(response);
  
  delete[] response;
  delete[] response_data;
}

void ChromeDriverPlugin::ReturnSendElementKeys(bool success, char *to_type) {
  if (success) {
    sendKeys(current_handle_, CharStringToWCharString(to_type), 10);
    http_server_->send(kNoContentReseponse);
  } else {
    //TODO(danielwh): Fail somehow
    //http_server_->send(kNoContentReseponse);
  }
}

void ChromeDriverPlugin::ReturnClearElement(bool success) {
  if (success) {
    http_server_->send(kNoContentReseponse);
  } else {
    //TODO(danielwh): Fail somehow
    //http_server_->send(kNoContentReseponse);
  }
}

void ChromeDriverPlugin::ReturnClickElement(bool success, int32 x, int32 y) {
  if (success) {
    clickAt(current_handle_, x, y);
    http_server_->send(kNoContentReseponse);
  } else {
    //TODO(danielwh): Fail somehow
    //http_server_->send(kNoContentReseponse);
  }
}

void ChromeDriverPlugin::ReturnGetElementAttributeSuccess(std::string value) {
  SendStringValue(value);
}

void ChromeDriverPlugin::ReturnGetElementAttributeFailure() {
  SendNotFound();
}

void ChromeDriverPlugin::ReturnGetElementTextSuccess(string text) {
  SendStringValue(text);
}

void ChromeDriverPlugin::ReturnGetElementTextFailure() {
  SendNotFound();
}

void ChromeDriverPlugin::ReturnIsElementSelected(bool selected) {
  SendValue(string(selected ? "true" : "false"));
}

//TODO(danielwh): Window switching
/*void ChromeDriverPlugin::ReturnSwitchWindow(bool success) {
  if (selected) {
    http_server_->send(kNoContentReseponse);
  } else {
    SendNotFound();
  }
}*/

void ChromeDriverPlugin::ReturnSubmitElement(bool success) {
  if (success) {
    http_server_->send(kNoContentReseponse);
  } else {
    //TODO(danielwh): fail somehow
  }
}

} //namespace webdriver
