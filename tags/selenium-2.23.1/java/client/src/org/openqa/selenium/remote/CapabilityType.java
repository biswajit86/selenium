/*
Copyright 2007-2010 Selenium committers

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

package org.openqa.selenium.remote;

/**
 * Commonly seen remote webdriver capabilities.
 */
public interface CapabilityType {
  String BROWSER_NAME = "browserName";
  String PLATFORM = "platform";
  String SUPPORTS_JAVASCRIPT = "javascriptEnabled";
  String TAKES_SCREENSHOT = "takesScreenshot";
  String VERSION = "version";
  String SUPPORTS_ALERTS = "handlesAlerts";
  String SUPPORTS_SQL_DATABASE = "databaseEnabled";
  String SUPPORTS_LOCATION_CONTEXT = "locationContextEnabled";
  String SUPPORTS_APPLICATION_CACHE = "applicationCacheEnabled";
  String SUPPORTS_BROWSER_CONNECTION = "browserConnectionEnabled";
  String SUPPORTS_FINDING_BY_CSS = "cssSelectorsEnabled";
  String PROXY = "proxy";
  String SUPPORTS_WEB_STORAGE = "webStorageEnabled";
  String ROTATABLE = "rotatable";
  // Enable this capability to accept all SSL certs by defaults.
  String ACCEPT_SSL_CERTS = "acceptSslCerts";
  String HAS_NATIVE_EVENTS = "nativeEvents";

  String LOGGING_PREFS = "loggingPrefs";

  interface ForSeleniumServer {
    String AVOIDING_PROXY = "avoidProxy";
    String ONLY_PROXYING_SELENIUM_TRAFFIC = "onlyProxySeleniumTraffic";
    String PROXYING_EVERYTHING = "proxyEverything";
    String PROXY_PAC = "proxy_pac";
    String ENSURING_CLEAN_SESSION = "ensureCleanSession";
  }
}
