/*
 * Copyright 2007 ThoughtWorks, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.thoughtworks.webdriver;

import java.util.List;


/**
 * The main interface to use for testing, which represents an idealised web
 * browser. The methods in this class fall into three categories:
 * <p/>
 * <ul>
 * <li>Control of the browser itself</li>
 * <li>Selection of {@link WebElement}s</li>
 * <li>Debugging aids</li>
 * </ul>
 * <p/>
 * Key methods are {@link WebDriver#get(String)}, which is used to load a new
 * web page, and the various methods similar to
 * {@link WebDriver#selectElement(String)}, which is used to find
 * {@link WebElement}s.
 * <p/>
 * Currently, you will need to instantiate implementations of this class
 * directly. It is hoped that you write your tests against this interface so
 * that you may "swap in" a more fully featured browser when there is a
 * requirement for one. Given this approach to testing, it is best to start
 * writing your tests using the {@link com.thoughtworks.webdriver.htmlunit.HtmlUnitDriver} implementation.
 * <p/>
 * Note that all methods that use XPath to locate elements will throw a
 * {@link RuntimeException} should there be an error thrown by the underlying
 * XPath engine.
 *
 * @see com.thoughtworks.webdriver.ie.InternetExplorerDriver
 * @see com.thoughtworks.webdriver.htmlunit.HtmlUnitDriver
 */
public interface WebDriver {
    // Navigation
    /**
     * Load a new web page in the current browser window. This is done using an
     * HTTP GET operation, and the method will block until the load is complete.
     * This will follow redirects issued either by the server or as a
     * meta-redirect from within the returned HTML. Should a meta-redirect
     * "rest" for any duration of time, it is best to wait until this timeout is
     * over, since should the underlying page change whilst your test is
     * executing the results of future calls against this interface will be
     * against the freshly loaded page. Synonym for {@link com.thoughtworks.webdriver.WebDriver.Navigation#to(String)} 
     *
     * @param url The URL to load. It is best to use a fully qualified URL
     */
    WebDriver get(String url);

    /**
     * Get a string representing the current URL that the browser is looking at.
     *
     * @return The URL of the page currently loaded in the browser
     */
    String getCurrentUrl();

    // General properties
    /**
     * The title of the current page.
     *
     * @return The title of the current page, or null if one is not already set
     */
    String getTitle();

    /**
     * Is the browser visible or not?
     *
     * @return True if the browser can be seen, or false otherwise
     */
    boolean getVisible();

    /**
     * Make the browser visible or not. Note that for some implementations, this
     * is a no-op.
     *
     * @param visible Set whether or not the browser is visible
     */
    WebDriver setVisible(boolean visible);

  /**
     * Find all elements within the current page using the given mechanism.
     *
     * @param by The locating mechanism to use
     * @return A list of all {@link WebElement}s, or an empty list if nothing matches
     * @see com.thoughtworks.webdriver.By
     */
    List<WebElement> findElements(By by);


    /**
     * Find the first {@link WebElement} using the given method.
     *
     * @param by The locating mechanism
     * @return The first matching element on the current page
     * @throws NoSuchElementException If no matching elements are found
     */
    WebElement findElement(By by);

    // Misc
    /**
     * Get the source of the last loaded page. If the page has been modified
     * after loading (for example, by Javascript) there is no guarentee that the
     * returned text is that of the modified page. Please consult the
     * documentation of the particular driver being used to determine whether
     * the returned text reflects the current state of the page or the text last
     * sent by the web server.
     *
     * @return The source of the current page
     */
    String getPageSource();

    /**
     * Close the current window, quitting the browser if it's the last window
     * currently open.
     *
     * @return The currently active WebDriver, or null if there are no windows open
     */
    WebDriver close();

    /**
     * Send future commands to a different frame or window.
     *
     * @return A TargetLocator which can be used to select a frame or window
     * @see com.thoughtworks.webdriver.WebDriver.TargetLocator
     */
    TargetLocator switchTo();

  /**
   * An abstraction allowing the driver to access the browser's history and to
   * navigate to a given URL.
   *
   * @return A {@link com.thoughtworks.webdriver.WebDriver.Navigation} that allows
   * the selection of what to do next
   */
    Navigation navigate();

  /**
     * Used to locate a given frame or window.
     */
    public interface TargetLocator {
        /**
         * Select a frame by its (zero-based) index. That is, if a page has three frames, the first frame would be at index "0",
         * the second at index "1" and the third at index "2". Once the frame has been selected, all subsequent calls on the WebDriver interface are made to that frame.
         *
         * @param frameIndex
         * @return A driver focused on the given frame
         */
        WebDriver frame(int frameIndex);

        /**
         * Select a frame by its name. To select sub-frames, simply separate the frame names by dots. As an example
         * "main.child" will select the frame with the name "main" and then it's child "child". If a frame name is a
         * number, then it will be treated as selecting a frame as if using
         * {@link com.thoughtworks.webdriver.WebDriver.TargetLocator#frame(int)}
         *
         * @param frameName
         * @return A driver focused on the given frame
         */
        WebDriver frame(String frameName);

        /**
         * Switch the focus of future commands for this driver to the window with the given name
         *
         * @param windowName
         * @return A driver focused on the given window
         */
        WebDriver window(String windowName);

        /**
         * Selects either the first frame on the page, or the main document when a page contains iframes.
         */
		WebDriver defaultContent();

		Alert alert();
    }

    public interface Navigation {
      /**
       * Move back a single "item" in the browser's history.
       *@return The driver, on the previous page (or the same one if there is no history)
       */
      WebDriver back();

      /**
       * Move a single "item" forward in the browser's history. Does nothing if
       * we are on the latest page viewed.
       *
       * @return The driver, on the next page (or the same one if there is no history)
       */
      WebDriver forward();


      /**
       * Load a new web page in the current browser window. This is done using an
       * HTTP GET operation, and the method will block until the load is complete.
       * This will follow redirects issued either by the server or as a
       * meta-redirect from within the returned HTML. Should a meta-redirect
       * "rest" for any duration of time, it is best to wait until this timeout is
       * over, since should the underlying page change whilst your test is
       * executing the results of future calls against this interface will be
       * against the freshly loaded page.
       *
       * @param url The URL to load. It is best to use a fully qualified URL
       * @return The driver, pointing at the loaded page
       */
      WebDriver to(String url);
    }
}
