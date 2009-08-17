package org.openqa.selenium.chrome;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.Speed;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.FileHandler;
import org.openqa.selenium.internal.FindsByClassName;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByLinkText;
import org.openqa.selenium.internal.FindsByName;
import org.openqa.selenium.internal.FindsByTagName;
import org.openqa.selenium.internal.FindsByXPath;

public class ChromeDriver implements WebDriver, SearchContext, JavascriptExecutor,
FindsById, FindsByClassName, FindsByLinkText, FindsByName, FindsByTagName, FindsByXPath {

  private Process clientProcess;
  private final ChromeCommandExecutor executor;
  
  public ChromeDriver() throws Exception {
    this.executor = new ChromeCommandExecutor(9701);
    startClient();
    //TODO(danielwh): Set up the session
    //execute("newSession", DesiredCapabilities.chrome());
  }  
  
  /**
   * By default will try to load Chrome from system property
   * webdriver.chrome.binary and the extension from
   * webdriver.chrome.extensiondir.  If the former fails, will try to guess the
   * path to Chrome.  If the latter fails, will try to unzip from the JAR we 
   * hope we're in.  If these fail, throws exceptions.
   */
  protected void startClient() {
    try {
       File extensionDir = getExtensionDir();
      if (!extensionDir.isDirectory()) {
        throw new FileNotFoundException("Could not find extension directory" +
            "(" + extensionDir + ").  Try setting webdriver.chrome.extensiondir."); 
      }
      File chromeFile = getChromeFile();
      if (!chromeFile.isFile()) {
        throw new FileNotFoundException("Could not find chrome binary(" +
            chromeFile.getCanonicalPath() + ").  " +
            "Try setting webdriver.chrome.binary.");
      }
      StringBuilder toRun = new StringBuilder();
      toRun.append(chromeFile.getCanonicalPath())
           .append(" --enable-extensions --load-extension=");
      if (System.getProperty("os.name").equals("Linux")) {
        toRun.append(extensionDir.getCanonicalPath());
      } else {
        toRun.append("\"").append(extensionDir.getCanonicalPath()).append("\"");
      }
      System.out.println("Execing: " + toRun);
      clientProcess = Runtime.getRuntime().exec(toRun.toString());
      //Ick, we sleep for a little bit in case the browser hasn't quite loaded
      Thread.sleep(1000);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  protected void stopClient() {
    System.setProperty("webdriver.chrome.extensiondir", "");
    if (clientProcess != null) {
      System.out.println("Killing browser");
      clientProcess.destroy();
      clientProcess = null;
    }
    try {
      executor.stopListening();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  Response execute(String commandName, Object... parameters) {
    Command command = new Command(commandName, parameters);
    try {
      return executor.execute(command);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (UnsupportedOperationException e) {
      stopClient();
      throw e;
    }
  }
  
  protected File getExtensionDir() throws IOException {
    File extensionDir = null;
    String extensionDirSystemProperty = System.getProperty(
        "webdriver.chrome.extensiondir");
    if (extensionDirSystemProperty != null &&
        extensionDirSystemProperty != "") {
      //Default to reading from the property
      extensionDir = new File(extensionDirSystemProperty);
    } else {
      //If property not set, try to unpack the zip from the jar
      extensionDir = FileHandler.unzip(this.getClass().getResourceAsStream(
          "/chrome-extension.zip"));
    }
    return extensionDir;
  }
  
  protected File getChromeFile() throws IOException {
    File chromeFile = null;
    String chromeFileSystemProperty = System.getProperty(
        "webdriver.chrome.binary");
    if (chromeFileSystemProperty != null) {
      chromeFile = new File(chromeFileSystemProperty);
    } else {
      StringBuilder chromeFileString = new StringBuilder();
      if (System.getProperty("os.name").equals("Windows XP")) {
        chromeFileString.append(System.getProperty("user.home"))
                        .append("\\Local Settings\\Application Data\\")
                        .append("Google\\Chrome\\Application\\chrome.exe");
      } else if (System.getProperty("os.name").equals("Windows Vista")) {
        chromeFileString.append("C:\\Users\\")
                        .append(System.getProperty("user.name"))
                        .append("\\AppData\\Local\\")
                        .append("Google\\Chrome\\Application\\chrome.exe");
      } else if (System.getProperty("os.name").equals("Linux")) {
        chromeFileString.append("/usr/bin/google-chrome");
      } else {
        throw new RuntimeException("Unsupported operating system.  " +
            "Could not locate Chrome.  Set webdriver.chrome.binary.");
      }
      chromeFile = new File(chromeFileString.toString());
    }
    return chromeFile;
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public WebElement findElement(By by) {
    return by.findElement((SearchContext)this);
  }

  @Override
  public List<WebElement> findElements(By by) {
    return by.findElements((SearchContext)this);
  }

  @Override
  public void get(String url) {
    execute("get", url);
  }

  @Override
  public String getCurrentUrl() {
    return execute("getCurrentUrl").getValue().toString();
  }

  @Override
  public String getPageSource() {
    return execute("getPageSource").getValue().toString();
  }

  @Override
  public String getTitle() {
    return execute("getTitle").getValue().toString();
  }

  @Override
  public String getWindowHandle() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<String> getWindowHandles() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Options manage() {
    return new ChromeOptions();
  }

  @Override
  public Navigation navigate() {
    return new ChromeNavigation();
  }

  @Override
  public void quit() {
    try {
      execute("quit");
    } finally {
      stopClient();
    }
  }

  @Override
  public TargetLocator switchTo() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object executeScript(String script, Object... args) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isJavascriptEnabled() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public WebElement findElementById(String using) {
    return getElementFrom(execute("findElement", "id", using));
  }

  @Override
  public List<WebElement> findElementsById(String using) {
    return getElementsFrom(execute("findElements", "id", using));
  }

  @Override
  public WebElement findElementByClassName(String using) {
    return getElementFrom(execute("findElement", "class name", using));
  }

  @Override
  public List<WebElement> findElementsByClassName(String using) {
    return getElementsFrom(execute("findElements", "class name", using));
  }

  @Override
  public WebElement findElementByLinkText(String using) {
    return getElementFrom(execute("findElement", "link text", using));
  }

  @Override
  public List<WebElement> findElementsByLinkText(String using) {
    return getElementsFrom(execute("findElements", "link text", using));
  }

  @Override
  public WebElement findElementByName(String using) {
    return getElementFrom(execute("findElement", "name", using));
  }

  @Override
  public List<WebElement> findElementsByName(String using) {
    return getElementsFrom(execute("findElements", "name", using));
  }

  @Override
  public WebElement findElementByTagName(String using) {
    return getElementFrom(execute("findElement", "tag name", using));
  }

  @Override
  public List<WebElement> findElementsByTagName(String using) {
    return getElementsFrom(execute("findElements", "tag name", using));
  }

  @Override
  public WebElement findElementByXPath(String using) {
    return getElementFrom(execute("findElement", "xpath", using));
  }

  @Override
  public List<WebElement> findElementsByXPath(String using) {
    return getElementsFrom(execute("findElements", "xpath", using));
  }

  @Override
  public WebElement findElementByPartialLinkText(String using) {
    return getElementFrom(execute("findElement", "partial link text", using));
  }
  
  @Override
  public List<WebElement> findElementsByPartialLinkText(String using) {
    return getElementsFrom(execute("findElements", "partial link text", using));
  }
  
  WebElement getElementFrom(Response response) {
    Object result = response.getValue();
    Object[] elements = (Object[])result;
    return new ChromeWebElement(this, (String)elements[0]);
  }

  List<WebElement> getElementsFrom(Response response) {
    Object result = response.getValue();
    List<WebElement> elements = new Vector<WebElement>();
    for (Object element : (Object[])result) {
      elements.add(new ChromeWebElement(this, (String)element));
    }
    return elements;
  }
  
  List<WebElement> findChildElements(ChromeWebElement parent, String by, String using) {
    return getElementsFrom(execute("findChildElements", parent, by, using));
  }
  
  private class ChromeOptions implements Options {

    @Override
    public void addCookie(Cookie cookie) {
      execute("addCookie", cookie);
    }

    @Override
    public void deleteAllCookies() {
      execute("deleteAllCookies");
    }

    @Override
    public void deleteCookie(Cookie cookie) {
      execute("deleteCookie", cookie.getName());
    }

    @Override
    public void deleteCookieNamed(String name) {
      execute("deleteCookie", name);
    }

    @Override
    public Set<Cookie> getCookies() {
      Object result = execute("getCookies").getValue();
      Set<Cookie> cookies = new HashSet<Cookie>();
      for (Object cookie : (Object[])result) {
        if (!(cookie instanceof JSONObject)) {
          System.err.println("Ignoring malformed cookie: " + cookie);
          continue;
        }
        JSONObject jsonCookie = (JSONObject)cookie;
        if (!jsonCookie.has("name") || !jsonCookie.has("value")) {
          System.err.println("Ignoring malformed cookie: " + cookie);
          continue;
        }
        try {
          cookies.add(new Cookie(jsonCookie.getString("name"), jsonCookie.getString("value")));
        } catch (JSONException e) {
          System.err.println("Ignoring malformed cookie: " + cookie);
          continue;
        }
      }
      return cookies;
    }

    @Override
    public Speed getSpeed() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void setSpeed(Speed speed) {
      // TODO Auto-generated method stub
      
    }
  }
  
  private class ChromeNavigation implements Navigation {
    public void back() {
      execute("goBack");
    }

    public void forward() {
      execute("goForward");
    }

    public void to(String url) {
      get(url);
    }

    public void to(URL url) {
      get(String.valueOf(url));
    }

    public void refresh() {
      execute("refresh");
    }
  }
}
