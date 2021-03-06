package com.thoughtworks.selenium.embedded.jetty;

import com.thoughtworks.selenium.launchers.MacOperaBrowserLauncher;
import com.thoughtworks.selenium.BrowserLauncher;

/**
 * @author Paul Hammant
 * @version $Revision: 1.8 $
 */
public class MacOperaIntegrationTest extends RealDealIntegrationTest {
    protected BrowserLauncher getBrowserLauncher() {
        return new MacOperaBrowserLauncher();
    }
}
