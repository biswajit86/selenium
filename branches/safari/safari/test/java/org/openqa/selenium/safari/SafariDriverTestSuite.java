package org.openqa.selenium.safari;

import static org.openqa.selenium.Ignore.Driver.IPHONE;
import static org.openqa.selenium.Ignore.Driver.REMOTE;

import org.openqa.selenium.Platform;
import org.openqa.selenium.TestSuiteBuilder;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

public class SafariDriverTestSuite extends TestCase {
  public static Test suite() throws Exception {
    return new TestSuiteBuilder()
        .addSourceDir("safari")
        .addSourceDir("remote")
        .addSourceDir("common")
        .usingDriver(SafariDriver.class)
        .exclude(IPHONE)
        .exclude(REMOTE)
        .includeJavascriptTests()
        .create();
  }
  public static void main(String[] args) throws Exception {
    TestSuiteBuilder builder = new TestSuiteBuilder();
    builder.usingDriver(SafariDriver.class);
    builder.create().run(new TestResult());
  }
}

