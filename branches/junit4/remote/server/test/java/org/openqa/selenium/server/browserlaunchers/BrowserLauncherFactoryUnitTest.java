package org.openqa.selenium.server.browserlaunchers;

import org.junit.Test;
import org.openqa.selenium.server.BrowserConfigurationOptions;
import org.openqa.selenium.server.RemoteControlConfiguration;

/**
 * {@link org.openqa.selenium.server.browserlaunchers.BrowserLauncherFactory} unit test class
 */
public class BrowserLauncherFactoryUnitTest {
    @Test public void allSupportedBrowsersDefineAppropriateConstructor() {
        for (Class<? extends BrowserLauncher> c : BrowserLauncherFactory.getSupportedLaunchers().values()) {
            try {
                c.getConstructor(BrowserConfigurationOptions.class, RemoteControlConfiguration.class, String.class, String.class);
            } catch (Exception e) {
                throw new RuntimeException(c.getSimpleName(), e);
            }
        }
    }
}

