/*
Copyright 2007-2009 WebDriver committers
Copyright 2007-2009 Google Inc.
Portions copyright 2007 ThoughtWorks, Inc

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

package org.openqa.selenium;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.Matchers.not;
import static org.openqa.selenium.Ignore.Driver.HTMLUNIT;
import static org.openqa.selenium.Ignore.Driver.SAFARI;

public class VisibilityTest extends AbstractDriverTestCase {

  @JavascriptEnabled
  @Ignore(SAFARI)
  public void testShouldAllowTheUserToTellIfAnElementIsDisplayedOrNot() {
    driver.get(javascriptPage);

    assertThat(((RenderedWebElement) driver.findElement(By.id("displayed"))).isDisplayed(),
            is(true));
    assertThat(((RenderedWebElement) driver.findElement(By.id("none"))).isDisplayed(), is(false));
    assertThat(((RenderedWebElement) driver.findElement(By.id("suppressedParagraph")))
            .isDisplayed(), is(false));
    assertThat(((RenderedWebElement) driver.findElement(By.id("hidden"))).isDisplayed(), is(false));
  }

  @JavascriptEnabled
  @Ignore(SAFARI)
  public void testVisibilityShouldTakeIntoAccountParentVisibility() {
    driver.get(javascriptPage);

    RenderedWebElement childDiv = (RenderedWebElement) driver.findElement(By.id("hiddenchild"));
    RenderedWebElement hiddenLink = (RenderedWebElement) driver.findElement(By.id("hiddenlink"));

    assertFalse(childDiv.isDisplayed());
    assertFalse(hiddenLink.isDisplayed());
  }

  @JavascriptEnabled
  @Ignore( { SAFARI, HTMLUNIT })
  public void testShouldCountElementsAsVisibleIfStylePropertyHasBeenSet() {
    driver.get(javascriptPage);

    RenderedWebElement shown = (RenderedWebElement) driver.findElement(By.id("visibleSubElement"));

    assertTrue(shown.isDisplayed());
  }

  @JavascriptEnabled
  @Ignore( { SAFARI })
  public void testHiddenInputElementsAreNeverVisible() {
    driver.get(javascriptPage);

    RenderedWebElement shown = (RenderedWebElement) driver.findElement(By.name("hidden"));

    assertFalse(shown.isDisplayed());
  }

  @JavascriptEnabled
  @Ignore(SAFARI)
  public void testShouldNotBeAbleToClickOnAnElementThatIsNotDisplayed() {
    driver.get(javascriptPage);
    WebElement element = driver.findElement(By.id("unclickable"));

    try {
      element.click();
      fail("You should not be able to click on an invisible element");
    } catch (UnsupportedOperationException e) {
      // This is expected
    }
  }

  @JavascriptEnabled
  @Ignore(SAFARI)
  public void testShouldNotBeAbleToToggleAnElementThatIsNotDisplayed() {
    driver.get(javascriptPage);
    WebElement element = driver.findElement(By.id("untogglable"));

    try {
      element.toggle();
      fail("You should not be able to toggle an invisible element");
    } catch (UnsupportedOperationException e) {
      // This is expected
    }
  }

  @JavascriptEnabled
  @Ignore(SAFARI)
  public void testShouldNotBeAbleToSelectAnElementThatIsNotDisplayed() {
    driver.get(javascriptPage);
    WebElement element = driver.findElement(By.id("untogglable"));

    try {
      element.setSelected();
      fail("You should not be able to select an invisible element");
    } catch (UnsupportedOperationException e) {
      // This is expected
    }
  }

  @JavascriptEnabled
  @Ignore(SAFARI)
  public void testShouldNotBeAbleToTypeAnElementThatIsNotDisplayed() {
    driver.get(javascriptPage);
    WebElement element = driver.findElement(By.id("unclickable"));

    try {
      element.sendKeys("You don't see me");
      fail("You should not be able to send keyboard input to an invisible element");
    } catch (UnsupportedOperationException e) {
      // This is expected
    }

    assertThat(element.getValue(), is(not("You don't see me")));
  }

  @JavascriptEnabled
  @Ignore
  public void testShouldNotAllowAnElementWithZeroHeightToBeCountedAsDisplayed() {
    driver.get(javascriptPage);

    RenderedWebElement zeroHeight = (RenderedWebElement) driver.findElement(By.id("zeroheight"));

    assertFalse(zeroHeight.isDisplayed());
  }

  @JavascriptEnabled
  @Ignore
  public void testShouldNotAllowAnElementWithZeroWidthToBeCountedAsDisplayed() {
    driver.get(javascriptPage);

    RenderedWebElement zeroWidth = (RenderedWebElement) driver.findElement(By.id("zerowidth"));

    assertFalse(zeroWidth.isDisplayed());
  }

}
