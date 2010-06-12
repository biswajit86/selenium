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

package org.openqa.selenium.remote.server;

import org.junit.Test;
import static org.junit.Assert.*;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DriverSessionTest {
  @Test public void shouldRegisterCorrectDefaultsOnMac() {
    DriverFactory factory = new DriverFactory();
    new DriverSessions(Platform.MAC, factory);

    assertTrue(factory.hasMappingFor(DesiredCapabilities.chrome()));
    assertTrue(factory.hasMappingFor(DesiredCapabilities.firefox()));
    assertTrue(factory.hasMappingFor(DesiredCapabilities.htmlUnit()));
    assertFalse(factory.hasMappingFor(DesiredCapabilities.internetExplorer()));
  }

  @Test public void shouldRegisterCorrectDefaultsOnLinux() {
    DriverFactory factory = new DriverFactory();
    new DriverSessions(Platform.LINUX, factory);

    assertTrue(factory.hasMappingFor(DesiredCapabilities.chrome()));
    assertTrue(factory.hasMappingFor(DesiredCapabilities.firefox()));
    assertTrue(factory.hasMappingFor(DesiredCapabilities.htmlUnit()));
    assertFalse(factory.hasMappingFor(DesiredCapabilities.internetExplorer()));
  }

  @Test public void shouldRegisterCorrectDefaultsOnWindows() {
    DriverFactory factory = new DriverFactory();
    new DriverSessions(Platform.VISTA, factory);

    assertTrue(factory.hasMappingFor(DesiredCapabilities.chrome()));
    assertTrue(factory.hasMappingFor(DesiredCapabilities.firefox()));
    assertTrue(factory.hasMappingFor(DesiredCapabilities.htmlUnit()));
    assertTrue(factory.hasMappingFor(DesiredCapabilities.internetExplorer()));
  }

  @Test public void shouldBeAbleToRegisterOwnDriver() {
    DriverFactory factory = new DriverFactory();
    DriverSessions sessions = new DriverSessions(Platform.VISTA, factory);

    Capabilities capabilities = new DesiredCapabilities("foo", "1", Platform.ANY);
    sessions.registerDriver(capabilities, AbstractDriver.class);

    assertTrue(factory.hasMappingFor(capabilities));
  }

  public static abstract class AbstractDriver implements WebDriver {}
}
