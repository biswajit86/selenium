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

package org.openqa.selenium.firefox;

import org.openqa.selenium.firefox.internal.Cleanly;
import org.openqa.selenium.firefox.internal.FileHandler;
import org.openqa.selenium.firefox.internal.ProfileReaper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class FirefoxProfile {
    private static final String EXTENSION_NAME = "fxdriver@googlecode.com";
    private File profileDir;
    private File extensionsDir;
    private File userPrefs;
    private Preferences additionalPrefs = new Preferences();
    private int port;
    private static AtomicInteger nextId = new AtomicInteger(new Random().nextInt());

    // Profile management "stuff"
    private Set<File> profileTempDirs = new HashSet<File>();
    private ProfileReaper reaper;

    protected FirefoxProfile(ProfileReaper reaper) {
      this.reaper = reaper;
    }

    public FirefoxProfile(File profileDir) {
      this(ProfileReaper.getInstance());
      commonConstruction(profileDir);
    }

    public FirefoxProfile() {
      this(ProfileReaper.getInstance());
      profileDir = getUniqueProfileDir();

      if (profileDir == null || !profileDir.mkdirs()) {
        throw new RuntimeException("Cannot create custom profile directory");
      }

      commonConstruction(profileDir);
    }

    private void commonConstruction(File profileDir) {
      this.profileDir = profileDir;
      this.extensionsDir = new File(profileDir, "extensions");
      this.userPrefs = new File(profileDir, "user.js");

      port = FirefoxDriver.DEFAULT_PORT;

      if (!profileDir.exists()) {
        throw new RuntimeException(MessageFormat.format("Profile directory does not exist: {0}",
            profileDir.getAbsolutePath()));
      }
    }

  protected File getUniqueProfileDir() {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        if (!tmpDir.exists())
            throw new RuntimeException("Unable to find default temp directory: " + tmpDir);

    String probablyUniqueName =
        "webdriver-custom-" + System.currentTimeMillis() + nextId.getAndIncrement();

    File profileDir = new File(tmpDir, probablyUniqueName);

    rememberToClean(profileDir);

    return profileDir;
  }

  protected void addWebDriverExtensionIfNeeded(boolean forceCreation) throws IOException {
        File extensionLocation = new File(extensionsDir, EXTENSION_NAME);
        if (!forceCreation && extensionLocation.exists())
            return;

        boolean isDev = Boolean.getBoolean("webdriver.firefox.development");
        if (isDev) {
            installDevelopmentExtension();
        } else {
            addExtension(FirefoxProfile.class, "webdriver-extension.zip");
        }

        deleteExtensionsCacheIfItExists();
    }

    public void addExtension(Class<?> loadResourcesUsing, String loadFrom) throws IOException {
      // Is loadFrom a file?
      File file = new File(loadFrom);
      if (file.exists()) {
        addExtension(file);
        return;
      }

      // Try and load it from the classpath
      InputStream resource = loadResourcesUsing.getResourceAsStream(loadFrom);
      if (resource == null && !loadFrom.startsWith("/")) {
        resource = loadResourcesUsing.getResourceAsStream("/" + loadFrom);
      }
      if (resource == null) {
        resource = FirefoxProfile.class.getResourceAsStream(loadFrom);
      }
      if (resource == null && !loadFrom.startsWith("/")) {
        resource = FirefoxProfile.class.getResourceAsStream("/" + loadFrom);
      }
      if (resource == null) {
        throw new FileNotFoundException("Cannot locate resource with name: " + loadFrom);
      }

      File root;
      if (FileHandler.isZipped(loadFrom)) {
        root = FileHandler.unzip(resource);
      } else {
        throw new RuntimeException("Will only install zipped extensions for now");
      }

      addExtension(root);
    }

  /**
   * Attempt to add an extension to install into this instance.
   *
   * @param extensionToInstall
   * @throws IOException
   */
  public void addExtension(File extensionToInstall) throws IOException {
    if (!extensionToInstall.isDirectory() &&
        !FileHandler.isZipped(extensionToInstall.getAbsolutePath())) {
      throw new IOException("Can only install from a zip file, an XPI or a directory");
    }

    File root = obtainRootDirectory(extensionToInstall);

    String id = readIdFromInstallRdf(root);

    File extensionDirectory = new File(extensionsDir, id);

    if (extensionDirectory.exists() && !FileHandler.delete(extensionDirectory)) {
      throw new IOException("Unable to delete existing extension directory: " + extensionDirectory);
    }

    FileHandler.createDir(extensionDirectory);
    FileHandler.makeWritable(extensionDirectory);
    FileHandler.copyDir(root, extensionDirectory);
  }

  private String readIdFromInstallRdf(File root) {
    try {
      File installRdf = new File(root, "install.rdf");

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(installRdf);

      XPath xpath = XPathFactory.newInstance().newXPath();
      xpath.setNamespaceContext(new NamespaceContext() {
        public String getNamespaceURI(String prefix) {
          if ("em".equals(prefix)) {
            return "http://www.mozilla.org/2004/em-rdf#";
          }

          return XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(String uri) {
          throw new UnsupportedOperationException("getPrefix");
        }

        public Iterator<?> getPrefixes(String uri) {
          throw new UnsupportedOperationException("getPrefixes");
        }
      });

      Node idNode = (Node) xpath.compile("//em:id").evaluate(doc, XPathConstants.NODE);

      if (idNode == null) {
        throw new RuntimeException(
            "Cannot locate node containing extension id: " + installRdf.getAbsolutePath());
      }

      String id = idNode.getTextContent();

      if (id == null || "".equals(id.trim())) {
        throw new FileNotFoundException("Cannot install extension with ID: " + id);
      }
      return id;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private File obtainRootDirectory(File extensionToInstall) throws IOException {
    File root = extensionToInstall;
    if (!extensionToInstall.isDirectory()) {
      BufferedInputStream bis =
          new BufferedInputStream(new FileInputStream(extensionToInstall));
      try {
        root = FileHandler.unzip(bis);
      } finally {
        bis.close();
      }
    }
    return root;
  }

  protected void installDevelopmentExtension() throws IOException {
      if (!FileHandler.createDir(extensionsDir))
        throw new IOException("Cannot create extensions directory: " + extensionsDir.getAbsolutePath());

      String home = findFirefoxExtensionRootInSourceCode();

      File writeTo = new File(extensionsDir, EXTENSION_NAME);
        if (writeTo.exists() && !FileHandler.delete(writeTo)) {
            throw new IOException("Cannot delete existing extensions directory: " +
                                  extensionsDir.getAbsolutePath());
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(writeTo);
            writer.write(home);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Cleanly.close(writer);
        }
    }

  private String findFirefoxExtensionRootInSourceCode() {
    String[] possiblePaths = {
        "firefox/src/extension",
        "../firefox/src/extension",
    };

    File current;
    for (String potential : possiblePaths) {
      current = new File(potential);
      if (current.exists()) {
        return current.getAbsolutePath();
      }
    }

    throw new RuntimeException("Unable to locate firefox driver extension in developer source");
  }

  public File getProfileDir() {
        return profileDir;
    }

    //Assumes that we only really care about the preferences, not the comments
    private Map<String, String> readExistingPrefs(File userPrefs) {
        Map<String, String> prefs = new HashMap<String, String>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(userPrefs));
            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith("user_pref(\"")) {
                    line = reader.readLine();
                    continue;
                }
                line = line.substring("user_pref(\"".length());
                line = line.substring(0, line.length() - ");".length());
                String[] parts = line.split(",");
                parts[0] = parts[0].substring(0, parts[0].length() - 1);
                prefs.put(parts[0].trim(), parts[1].trim());

                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Cleanly.close(reader);
        }

        return prefs;
    }

    public File getExtensionsDir() {
        return extensionsDir;
    }

    /**
     * Set a preference for this particular profile. The value will be properly quoted
     * before use. Note that if a value looks as if it is a quoted string (that is, starts
     * with a quote character and ends with one too) an IllegalArgumentException is thrown:
     * Firefox fails to start properly when some values are set to this.
     *
     * @param key The key
     * @param value The new value.
     */
    public void setPreference(String key, String value) {
        additionalPrefs.setPreference(key, value);
    }

    /**
     * Set a preference for this particular profile.
     *
     * @param key The key
     * @param value The new value.
     */
    public void setPreference(String key, boolean value) {
        additionalPrefs.setPreference(key, String.valueOf(value));
    }

    /**
     * Set a preference for this particular profile.
     *
     * @param key The key
     * @param value The new value.
     */
    public void setPreference(String key, int value) {
        additionalPrefs.setPreference(key, String.valueOf(value));
    }

    protected Preferences getAdditionalPreferences() {
      return additionalPrefs;
    }

    public void updateUserPrefs() {
        if (port == 0) {
            throw new RuntimeException("You must set the port to listen on before updating user.js");
        }

        Map<String, String> prefs = new HashMap<String, String>();

        if (userPrefs.exists()) {
            prefs = readExistingPrefs(userPrefs);
            if (!userPrefs.delete())
                throw new RuntimeException("Cannot delete existing user preferences");
        }

        additionalPrefs.addTo(prefs);

        // Normal settings to facilitate testing
        prefs.put("app.update.auto", "false");
        prefs.put("app.update.enabled", "false");
        prefs.put("browser.download.manager.showWhenStarting", "false");
        prefs.put("browser.EULA.override", "true");
        prefs.put("browser.EULA.3.accepted", "true");
        prefs.put("browser.link.open_external", "2");
        prefs.put("browser.link.open_newwindow", "2");
        prefs.put("browser.safebrowsing.enabled", "false");
        prefs.put("browser.search.update", "false");
        prefs.put("browser.sessionstore.resume_from_crash", "false");
        prefs.put("browser.shell.checkDefaultBrowser", "false");
        prefs.put("browser.startup.page", "0");
        prefs.put("browser.tabs.warnOnClose", "false");
        prefs.put("browser.tabs.warnOnOpen", "false");
        prefs.put("dom.disable_open_during_load", "false");
        prefs.put("extensions.update.enabled", "false");
        prefs.put("extensions.update.notifyUser", "false");
        prefs.put("security.warn_entering_secure", "false");
        prefs.put("security.warn_submit_insecure", "false");
        prefs.put("security.warn_entering_secure.show_once", "false");
        prefs.put("security.warn_entering_weak", "false");
        prefs.put("security.warn_entering_weak.show_once", "false");
        prefs.put("security.warn_leaving_secure", "false");
        prefs.put("security.warn_leaving_secure.show_once", "false");
        prefs.put("security.warn_submit_insecure", "false");
        prefs.put("security.warn_viewing_mixed", "false");
        prefs.put("security.warn_viewing_mixed.show_once", "false");
        prefs.put("signon.rememberSignons", "false");
        prefs.put("startup.homepage_welcome_url", "\"about:blank\"");

        // Which port should we listen on?
        prefs.put("webdriver_firefox_port", Integer.toString(port));

        // Settings to facilitate debugging the driver
        prefs.put("javascript.options.showInConsole", "true"); // Logs errors in chrome files to the Error Console.
        prefs.put("browser.dom.window.dump.enabled", "true");  // Enables the use of the dump() statement

        writeNewPrefs(prefs);
    }

    public void deleteExtensionsCacheIfItExists() {
        File cacheFile = new File(extensionsDir, "../extensions.cache");
        if (cacheFile.exists())
            cacheFile.delete();
    }

    protected void writeNewPrefs(Map<String, String> prefs) {
        Writer writer = null;
        try {
            writer = new FileWriter(userPrefs);
            for (Map.Entry<String, String> entry : prefs.entrySet()) {
                writer.append("user_pref(\"").append(entry.getKey()).append("\", ").append(entry.getValue()).append(");\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Cleanly.close(writer);
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isRunning() {
        File macAndLinuxLockFile = new File(profileDir, ".parentlock");
        File windowsLockFile = new File(profileDir, "parent.lock");

        return macAndLinuxLockFile.exists() || windowsLockFile.exists();
    }

    public File init() throws IOException {
        addWebDriverExtensionIfNeeded(false);
        return profileDir;
    }

    public void clean() {
      reaper.clean(this.profileTempDirs);
    }

    public FirefoxProfile createCopy(int port) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File to = new File(tmpDir, "webdriver-" + System.currentTimeMillis());
        to.mkdirs();

        FileHandler.copyDir(profileDir, to);
        FirefoxProfile profile = new FirefoxProfile(to);
        additionalPrefs.addTo(profile);
        profile.setPort(port);
        profile.updateUserPrefs();

        rememberToClean(to);

        return profile;
    }

  private void rememberToClean(File dir) {
    reaper.deleteOnExit(dir);
    profileTempDirs.add(dir);
  }
}
