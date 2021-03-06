package org.openqa.selenium.opera;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.openqa.selenium.TestSuiteBuilder;

import static org.openqa.selenium.testing.drivers.Browser.opera;

public class OperaDriverTestSuite extends TestSuite {

  public static Test suite() throws Exception {
    return new TestSuiteBuilder()
        .addSourceDir("java/client/test")
        .using(opera)
        .keepDriverInstance()
        .includeJavascriptTests()
        .create();
  }
}
