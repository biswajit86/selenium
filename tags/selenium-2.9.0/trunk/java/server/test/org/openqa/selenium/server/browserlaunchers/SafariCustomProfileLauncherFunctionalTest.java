package org.openqa.selenium.server.browserlaunchers;

import org.openqa.selenium.server.RemoteControlConfiguration;

/**
 * {@link SafariCustomProfileLauncher} integration test class.
 */
public class SafariCustomProfileLauncherFunctionalTest extends LauncherFunctionalTestCase {

  public void testLauncherWithDefaultConfiguration() throws Exception {
    launchBrowser(new SafariCustomProfileLauncher(BrowserOptions.newBrowserOptions(),
        new RemoteControlConfiguration(), "CUST", null));
  }

  public void testLauncherWithHonorSystemProxyEnabled() throws Exception {
    final RemoteControlConfiguration configuration;

    configuration = new RemoteControlConfiguration();
    configuration.setHonorSystemProxy(true);
    launchBrowser(new SafariCustomProfileLauncher(BrowserOptions.newBrowserOptions(),
        configuration, "CUST", null));
  }

  public void testLaunchTwoBrowsersInARowWithDefaultConfiguration() throws Exception {
    final RemoteControlConfiguration configuration = new RemoteControlConfiguration();

    launchBrowser(new SafariCustomProfileLauncher(BrowserOptions.newBrowserOptions(),
        configuration, "CUST", null));
    launchBrowser(new SafariCustomProfileLauncher(BrowserOptions.newBrowserOptions(),
        configuration, "CUST", null));
  }

}
