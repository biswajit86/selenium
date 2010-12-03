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

package org.openqa.selenium.internal.seleniumemulation;

import java.awt.Dimension;

import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebDriver;

public class GetElementWidth extends SeleneseCommand<Number> {
  private final ElementFinder finder;

  public GetElementWidth(ElementFinder finder) {
    this.finder = finder;
  }

  @Override
  protected Number handleSeleneseCommand(WebDriver driver, String locator, String ignored) {
    Dimension size = ((RenderedWebElement) finder.findElement(driver, locator)).getSize();
    return (int) size.getWidth();
  }
}