package org.openqa.selenium;

import static org.openqa.selenium.Ignore.Driver.ANDROID;
import static org.openqa.selenium.Ignore.Driver.CHROME;
import static org.openqa.selenium.Ignore.Driver.IPHONE;
import static org.openqa.selenium.Ignore.Driver.OPERA;
import static org.openqa.selenium.Ignore.Driver.SELENESE;
import static org.openqa.selenium.TestWaiter.waitFor;
import static org.openqa.selenium.WaitingConditions.elementToBeHidden;
import static org.openqa.selenium.WaitingConditions.elementTextToEqual;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates how to use WebDriver with a file input element.
 * 
 * @author jmleyba@gmail.com (Jason Leyba)
 */
@Ignore(value = {IPHONE, ANDROID}, reason = "File uploads not allowed on the iPhone")
public class UploadTest extends AbstractDriverTestCase {

  private static final String LOREM_IPSUM_TEXT = "lorem ipsum dolor sit amet";
  private static final String FILE_HTML = "<div>" + LOREM_IPSUM_TEXT + "</div>";

  private File testFile;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    testFile = createTmpFile(FILE_HTML);
  }

  @JavascriptEnabled
  @Ignore(value = {CHROME, SELENESE, OPERA},
      reason = "Chrome, Opera: File input elements are not supported yet")
  public void testFileUploading() throws Exception {
    driver.get(pages.uploadPage);
    driver.findElement(By.id("upload")).sendKeys(testFile.getAbsolutePath());
    driver.findElement(By.id("go")).submit();

    // Uploading files across a network may take a while, even if they're really small
    WebElement label = driver.findElement(By.id("upload_label"));
    waitFor(elementToBeHidden(label), 30, TimeUnit.SECONDS);

    driver.switchTo().frame("upload_target");

    WebElement body = driver.findElement(By.xpath("//body"));
    waitFor(elementTextToEqual(body, LOREM_IPSUM_TEXT));
  }

  private File createTmpFile(String content) throws IOException {
    File f = File.createTempFile("webdriver", "tmp");
    f.deleteOnExit();

    OutputStream out = new FileOutputStream(f);
    PrintWriter pw = new PrintWriter(out);
    pw.write(content);
    pw.flush();
    pw.close();
    out.close();

    return f;
  }
}
