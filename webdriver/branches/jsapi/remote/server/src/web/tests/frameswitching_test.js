function testShouldContinueToReferToTheSameFrameOnceItHasBeenSelected(driver) {
  driver.get(TEST_PAGES.framesetPage);
  driver.switchToFrame(2);
  var checkbox = driver.findElement(
      webdriver.By.xpath("//input[@name='checky']"));
  checkbox.toggle();
  checkbox.submit();
  assertThat(driver.findElement(webdriver.By.xpath("//p")).getText(),
             equals("Success!"));
}


function testShouldAutomaticallyUseTheFirstFrameOnAPage(driver) {
  driver.get(TEST_PAGES.framesetPage);
  // Notice that we've not switched to the 0th frame
  var pageNumber = driver.findElement(
      webdriver.By.xpath("//span[@id='pageNumber']"));
  assertThat(pageNumber.getText(), equals("1"));
}


function testShouldFocusOnTheReplacementWhenAFrameFollowsALinkToA_TopTargettedPage(driver) {
  driver.get(TEST_PAGES.framesetPage);
  driver.switchToFrame(0);
  driver.findElement(webdriver.By.linkText("top")).click();
  assertThat(driver.getTitle(), equals("XHTML Test Page"));
  assertThat(
      driver.findElement(webdriver.By.xpath('/html/head/title')).getText(),
      equals('XHTML Test Page'));
}


function testShouldNotAutomaticallySwitchFocusToAnIFrameWhenAPageContainingThemIsLoaded(driver) {
  driver.get(TEST_PAGES.iframePage);
  driver.findElement(webdriver.By.id("iframe_page_heading"));
}


function testShouldAllowAUserToSwitchFromAnIframeBackToTheMainContentOfThePage(
    driver) {
  driver.get(TEST_PAGES.iframePage);
  driver.switchToFrame(0);
  driver.switchToDefaultContent();
  driver.findElement(webdriver.By.id('nested_form'));
  driver.expectErrorFromPreviousCommand(
      'Should have switched back to main content');
  driver.findElement(webdriver.By.id("iframe_page_heading"));
}


function testShouldAllowTheUserToSwitchToAnIFrameAndRemainFocusedOnIt(driver) {
  webdriver.logging.setLevel(webdriver.logging.Level.DEBUG);
  driver.get(TEST_PAGES.iframePage);
  driver.switchToFrame(0);
  driver.findElement(webdriver.By.id('submitButton')).click();
  var hello = driver.findElement(webdriver.By.id('greeting')).getText();
  assertThat(hello, equals('Success!'));
}


function testShouldBeAbleToSelectAFrameByName(driver) {
  driver.get(TEST_PAGES.framesetPage);
  driver.switchToFrame("second");
  assertThat(driver.findElement(webdriver.By.id("pageNumber")).getText(),
             equals("2"));
}


function testShouldSelectChildFramesByUsingADotSeparatedString(driver) {
  driver.get(TEST_PAGES.framesetPage);
  driver.switchToFrame("fourth.child2");
  assertThat(driver.findElement(webdriver.By.id("pageNumber")).getText(),
             equals("11"));
}


function testShouldSwitchToChildFramesTreatingNumbersAsIndex(driver) {
  driver.get(TEST_PAGES.framesetPage);
  driver.switchToFrame("fourth.1");
  assertThat(driver.findElement(webdriver.By.id("pageNumber")).getText(),
             equals("11"));
}


function testShouldBeAbleToFlipToAFrameIdentifiedByItsId(driver) {
  driver.get(TEST_PAGES.framesetPage);
  driver.switchToFrame("fifth");
  driver.findElement(webdriver.By.id("username"));
}


function testShouldThrowAnExceptionWhenAFrameCannotBeFound(driver) {
  driver.get(TEST_PAGES.xhtmlTestPage);
  driver.switchToFrame("Nothing here");
  driver.expectErrorFromPreviousCommand();
}


function testShouldThrowAnExceptionWhenAFrameCannotBeFoundByIndex(driver) {
  driver.get(TEST_PAGES.xhtmlTestPage);
  driver.switchToFrame(27);
  driver.expectErrorFromPreviousCommand();
}


function testShouldBeAbleToFindElementsInIframesByName(driver) {
  driver.get(TEST_PAGES.iframePage);
  driver.switchToFrame("iframe1");
  driver.findElement(webdriver.By.name("id-name1"));
}


function testShouldBeAbleToFindElementsInIframesByXPath(driver) {
  driver.get(TEST_PAGES.iframePage);
  driver.switchToFrame("iframe1");
  driver.findElement(webdriver.By.xpath("//*[@id = 'changeme']"));
}


function testGetCurrentUrl(driver) {
  driver.get(TEST_PAGES.framesetPage);

  driver.switchToFrame("second");
  assertThat(driver.getCurrentUrl(),
             equals("http://localhost:3000/common/page/2?title=Fish"));

  driver.get(TEST_PAGES.iframePage);
  assertThat(driver.getCurrentUrl(),
             equals("http://localhost:3000/common/iframes.html"));

  driver.switchToFrame("iframe1");
  assertThat(driver.getCurrentUrl(),
             equals("http://localhost:3000/common/formPage.html"));
}


function testScriptsExecuteInSelectedFrame(driver) {
  var script = 'return window.location.href';
  driver.get(TEST_PAGES.framesetPage);
  assertThat(driver.executeScript(script),
             equals('http://localhost:3000/common/page/1'));
  driver.switchToFrame('second');
  assertThat(driver.executeScript(script),
             equals('http://localhost:3000/common/page/2?title=Fish'));
}

