/*
Copyright 2011 WebDriver committers
Copyright 2011 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.openqa.selenium.remote.server.handler;

import org.openqa.selenium.remote.server.JsonParametersAware;
import org.openqa.selenium.remote.server.Session;
import org.openqa.selenium.remote.server.rest.ResultType;

import java.util.Map;

import static org.openqa.selenium.remote.server.rest.ResultType.SUCCESS;

public class SetAlertText extends WebDriverHandler implements JsonParametersAware {
  private String text;

  public SetAlertText(Session session) {
    super(session);
  }

  public void setJsonParameters(Map<String, Object> allParameters) throws Exception {
    text = (String) allParameters.get("text");
  }

  public ResultType call() throws Exception {
    getDriver().switchTo().alert().sendKeys(text);
    return SUCCESS;
  }

  @Override
  public String toString() {
    return "[set alert value]";
  }
}
