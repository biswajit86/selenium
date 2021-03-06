/*
Copyright 2007-2009 WebDriver committers
Copyright 2007-2009 Google Inc.

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

package org.openqa.selenium.lift.find;

import static org.openqa.selenium.lift.match.AttributeMatcher.attribute;

import static org.hamcrest.Matchers.equalTo;

import org.hamcrest.Factory;

/**
 * {@link Finder} for HTML div tags.
 * 
 * @author rchatley (Robert Chatley)
 */
public class DivFinder extends HtmlTagFinder {

  @Override
  protected String tagDescription() {
    return "div";
  }

  @Override
  protected String tagName() {
    return "div";
  }

  @Factory
  public static HtmlTagFinder div() {
    return new DivFinder();
  }

  public static HtmlTagFinder div(String id) {
    return div().with(attribute("id", equalTo(id)));
  }

}
