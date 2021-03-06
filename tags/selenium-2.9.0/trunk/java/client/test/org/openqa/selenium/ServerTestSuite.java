/*
 * Created on May 18, 2006
 *
 */
package org.openqa.selenium;

import org.openqa.selenium.os.WindowsUtils;
import org.openqa.selenium.server.LinuxHTMLRunnerFunctionalTest;
import org.openqa.selenium.server.WindowsHTMLRunnerFunctionalTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class ServerTestSuite extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(ServerTestSuite.class.getName());
    if (WindowsUtils.thisIsWindows()) {
      suite.addTestSuite(WindowsHTMLRunnerFunctionalTest.class);
    } else {
      suite.addTestSuite(LinuxHTMLRunnerFunctionalTest.class);
    }
    return suite;
  }
}
