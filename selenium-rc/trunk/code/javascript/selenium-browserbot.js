/*
 * Copyright 2004 ThoughtWorks, Inc
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
 
/*
 * This script provides the Javascript API to drive the test application contained within
 * a Browser Window.
 * TODO:
 *    Add support for more events (keyboard and mouse)
 *    Allow to switch "user-entry" mode from mouse-based to keyboard-based, firing different
 *          events in different modes.
 */

// The window to which the commands will be sent.  For example, to click on a
// popup window, first select that window, and then do a normal click command.

var browserName=navigator.appName;
var isIE = (browserName =="Microsoft Internet Explorer");
// Get the Gecko version as an 8 digit date. 
var geckoResult = /^Mozilla\/5\.0 .*Gecko\/(\d{8}).*$/.exec(navigator.userAgent);
var geckoVersion = geckoResult == null ? null : geckoResult[1];



/*
 * The 'invoke' method will call the required function, and then
 * remove itself from the window object. This allows a calling app
 * to provide a callback listener for the window load event, without the
 * calling app needing to worry about cleaning up afterward.
 * TODO: This could be more generic, but suffices for now.
 */
function SelfRemovingLoadListener(fn, frame) {
    var self = this;

    this.invoke=function () {
        try {
            // we've moved to a new page - clear the current one
            browserbot.currentPage = null;
            fn();
        } finally {
            removeLoadListener(frame, self.invoke);
        }
    }
}

BrowserBot = function(frame, executionContext) {
    this.frame = frame;
    this.executionContext = executionContext;
    this.currentPage = null;
    this.currentWindowName = null;
    
    this.modalDialogReturnValue = null;
    this.recordedAlerts = new Array();
    this.recordedConfirmations = new Array();
    this.nextConfirmResult = true;

}

BrowserBot.prototype.expectModalDialogAndReturn = function(returnValue) {
    this.modalDialogReturnValue = returnValue;
}

BrowserBot.prototype.cancelNextConfirmation = function() {
    this.nextConfirmResult = false;
}

BrowserBot.prototype.hasAlerts = function() {
   return (this.recordedAlerts.length > 0) ;
}

BrowserBot.prototype.getNextAlert = function() {
   return this.recordedAlerts.shift();
}

BrowserBot.prototype.hasConfirmations = function() {
    return (this.recordedConfirmations.length > 0) ;
}
 
BrowserBot.prototype.getNextConfirmation = function() {
    return this.recordedConfirmations.shift();
}


BrowserBot.prototype.getFrame = function() {
    return this.frame;
}

BrowserBot.prototype.getContentWindow = function() {
    return this.executionContext.getContentWindow(this.getFrame());
   
}

BrowserBot.prototype.selectWindow = function(target) {
    // we've moved to a new page - clear the current one
    this.currentPage = null;
    this.currentWindowName = null;
    if (target != "null") {
        // If window exists
        if (this.getTargetWindow(target)) {
            this.currentWindowName = target;
        }
    }
}

BrowserBot.prototype.openLocation = function(target, onloadCallback) {
    // We're moving to a new page - clear the current one
    this.currentPage = null;
    this.callOnNextPageLoad(onloadCallback);
    this.executionContext.open(target,this.getFrame());
}

BrowserBot.prototype.getCurrentPage = function() {
    if (this.currentPage == null) {
        var testWindow = this.getContentWindow().window;
        if (this.currentWindowName != null) {
            testWindow = this.getTargetWindow(this.currentWindowName);
        }
        modifyWindowToRecordPopUpDialogs(testWindow, this);
        modifyWindowToClearPageCache(testWindow, this);
        this.currentPage =  new PageBot(testWindow);
    }
    
    return this.currentPage;

    // private functions below - is there a better way?

    function modifyWindowToRecordPopUpDialogs(window, browserBot) {

    // Andy D. - I have modified this slightly since the last checkin.
    // I wanted to explore the possibility of mocking these things out all together.
    // I realise that this is not an ideal testing solution, but it might help the client to get SOMETHING rather than NOTHING


    // Andy D. - This is where the modal dialog stuff starts
    // in place of the modaldialog which causes our selenium thread to hang, we open a regular window,
    // and then execute the commands against it, until it is closed.
    // then we return the value that was most recently stored in window.returnValue (as far as I can determine,
    // the average user of these things will go:  window.returnValue = bob; window.close();

        window.showModalDialog = function(url, args, features) {
            var returnValue = browserBot.modalDialogReturnValue;
            browserBot.modalDialogReturnValue = null;
            return returnValue;

/*            // open a new window
            var modalWindow = window.open(url, "modalDialog");

            // change variables to point to the new window (but save references to the old ones)
            var pushedFrame = browserBot.frame;
            browserBot.frame = modalWindow;

            var pushedPage = browserBot.currentPage;
            browserBot.currentPage = null;

            var pushedExecutionContext = browserBot.executionContext;
            browserBot.executionContext = getWindowExecutionContext();
            
            var localBrowserBot = browserBot;
            
            function modalClosed() {
                // change variables back to the old references
                localBrowserBot.executionContext = pushedExecutionContext;
                localBrowserBot.currentPage = pushedPage;
                localBrowserBot.frame = pushedFrame;
            }

            modalWindow.attachEvent("onunload", modalClosed);
*/            
        };

        window.alert = function(alert){browserBot.recordedAlerts.push(alert);};
        window.confirm = function(message){
            browserBot.recordedConfirmations.push(message);
            var result = browserBot.nextConfirmResult;
            browserBot.nextConfirmResult = true;
            return result
        };
    }
     
     function modifyWindowToClearPageCache(window, browserBot) {
        //SPIKE factor this better via TDD
        function clearPageCache() {
          browserbot.currentPage = null;
        }

       if (window.addEventListener) {
          testWindow.addEventListener("unload",clearPageCache, true);
       }
       else if (window.attachEvent) {
          testWindow.attachEvent("onunload",clearPageCache);
       }
       // End SPIKE
     }
    

}

BrowserBot.prototype.getTargetWindow = function(windowName) {
    var evalString = "this.getContentWindow().window." + windowName;
    var targetWindow = eval(evalString);
    if (!targetWindow) {
        throw new Error("Window does not exist");
    }
    return targetWindow;
}

BrowserBot.prototype.callOnNextPageLoad = function(onloadCallback) {
    if (onloadCallback) {
        var el = new SelfRemovingLoadListener(onloadCallback, this.frame);
        addLoadListener(this.frame, el.invoke);
    }
}


PageBot = function(pageWindow) {
    this.currentWindow = pageWindow;
    this.currentDocument = pageWindow.document;
    this.location = pageWindow.location.pathname;
    this.title = function() {return this.currentDocument.title};

    this.locators = new Array();
    this.locators.push(this.findIdentifiedElement);
    this.locators.push(this.findElementByDomTraversal);
    this.locators.push(this.findElementByXPath);

}

/*
 * Finds an element on the current page, using various lookup protocols
 */
PageBot.prototype.findElement = function(locator) {
    var element = this.findElementInDocument(locator, this.currentDocument);
    
    if (element != null) {
        return element;
    } else {
        for (var i = 0; i < this.currentDocument.frames.length; i++) {
            //alert("looking for " + locator + " in " + this.currentDocument.frames[i].location.href);
            element = this.findElementInDocument(locator, this.currentDocument.frames[i].document);
            if (element != null) {
                return element;
            }
        }
    }

    // Element was not found by any locator function.
    throw new Error("Element " + locator + " not found");
}

PageBot.prototype.findElementInDocument = function(locator, inDocument) {
    // Try the locators one at a time.
    for (var i = 0; i < this.locators.length; i++) {
        var locatorFunction = this.locators[i];
        var element = locatorFunction.call(this, locator, inDocument);
        if (element != null) {
            return element;
        }
    }
}

/*
 * In IE, getElementById() also searches by name.
 * To provied consistent functionality with Firefox, we
 * search by name attribute if an element with the id isn't found.
 */
PageBot.prototype.findIdentifiedElement = function(identifier, inDocument) {
    // Since we try to get an id with _any_ string, we need to handle
    // cases where this causes an exception.
    try {
        var element = inDocument.getElementById(identifier);
        if (element == null
            && !isIE // IE checks this without asking
            && document.evaluate )// DOM3 XPath
        {
             var xpath = "//*[@name='" + identifier + "']";
             element = document.evaluate(xpath, inDocument, null, 0, null).iterateNext();
        }
    } catch (e) {
        return null;
    }

    return element;
}

/**
 * Finds an element using by evaluating the "document.*" string against the
 * current document object. Dom expressions must begin with "document."
 */
PageBot.prototype.findElementByDomTraversal = function(domTraversal, inDocument) {
    if (domTraversal.indexOf("document.") != 0) {
        return null;
    }

    // Trim the leading 'document'
    domTraversal = domTraversal.substr(9);
    var locatorScript = "inDocument." + domTraversal;
    var element = eval(locatorScript);

    if (!element) {
         return null;
    }

    return element;
}

/**
 * Finds an element identified by the xpath expression. Expressions _must_
 * begin with "//".
 */
PageBot.prototype.findElementByXPath = function(xpath, inDocument) {
    if (xpath.indexOf("//") != 0) {
        return null;
    }

    // If the document doesn't support XPath, mod it with html-xpath.
    // This only works for IE.
    if (!inDocument.evaluate) {
        addXPathSupport(inDocument);
    }

    // If we still don't have XPath bail.
    // TODO implement subset of XPath for browsers without native support.
    if (!inDocument.evaluate) {
        throw new Error("XPath not supported");
    }

    // Trim any trailing "/": not valid xpath, and remains from attribute locator.
    if (xpath.charAt(xpath.length - 1) == '/') {
        xpath = xpath.slice(0, xpath.length - 1);
    }

    return inDocument.evaluate(xpath, inDocument, null, 0, null).iterateNext();
}

/**
 * Returns an attribute based on an attribute locator. This is made up of an element locator
 * suffixed with @attribute-name.
 */
PageBot.prototype.findAttribute = function(locator) {
    // Split into locator + attributeName
    var attributePos = locator.lastIndexOf("@");
    var elementLocator = locator.slice(0, attributePos);
    var attributeName = locator.slice(attributePos + 1);

    // Find the element.
    var element = this.findElement(elementLocator);

    // Handle missing "class" attribute in IE.
    if (isIE && attributeName == "class") {
        attributeName = "className";
    }

    // Get the attribute value.
    var attributeValue = element.getAttribute(attributeName);

    return attributeValue ? attributeValue.toString() : null;
}

/*
 * Selects the first option with a matching label from the select box element
 * provided. If no matching element is found, nothing happens.
 */
PageBot.prototype.selectOptionWithLabel = function(element, stringValue) {
    triggerEvent(element, 'focus', false);
    for (var i = 0; i < element.options.length; i++) {
        var option = element.options[i];
        if (option.text == stringValue) {
            if (!option.selected) {
                option.selected = true;
                triggerEvent(element, 'change', true);
            }
        }
    }
    triggerEvent(element, 'blur', false);
}

PageBot.prototype.replaceText = function(element, stringValue) {
    triggerEvent(element, 'focus', false);
    triggerEvent(element, 'select', true);
    element.value=stringValue;
    if (isIE) {
        triggerEvent(element, 'change', true);
    }
    triggerEvent(element, 'blur', false);
}

PageBot.prototype.clickElement = function(element) {

    triggerEvent(element, 'focus', false);

    var wasChecked = element.checked;
    if (isIE) {
        element.click();
    }
    else {
        // Add an event listener that detects if the default action has been prevented.
        // (This is caused by a javascript onclick handler returning false)
        var preventDefault = false;
        element.addEventListener("click", function(evt) {preventDefault = evt.getPreventDefault()}, false);

        // Trigger the click event.
        triggerMouseEvent(element, 'click', true);

        // In FireFox < 1.0 Final, and Mozilla <= 1.7.3, just sending the click event is enough.
        // But in newer versions, we need to do it ourselves.
        var needsProgrammaticClick = (geckoVersion > '20041025');
        // Perform the link action if preventDefault was set.
        if (needsProgrammaticClick && !preventDefault) {
            // Try the element itself, as well as it's parent - this handles clicking images inside links.
            if (element.href) {
                this.currentWindow.location.href = element.href;
            }
            else if (element.parentNode.href) {
                this.currentWindow.location.href = element.parentNode.href;
            }
        }
    }

    if (this.windowClosed()) {
        return;
    }
    // Onchange event is not triggered automatically in IE.
    if (isIE && isDefined(element.checked) && wasChecked != element.checked) {
        triggerEvent(element, 'change', true);
    }

    triggerEvent(element, 'blur', false);
}

PageBot.prototype.windowClosed = function(element) {
    return this.currentWindow.closed;
}

PageBot.prototype.bodyText = function() {
    return getText(this.currentDocument.body)
}

PageBot.prototype.getAllButtons = function() {
    var elements = this.currentDocument.getElementsByTagName('input');
    var result = '';

    for (var i = 0; i < elements.length; i++) {
            if (elements[i].type == 'button' || elements[i].type == 'submit' || elements[i].type == 'reset') {
                    result += elements[i].id;

                    result += ',';
            }
    }

    return result;
}


PageBot.prototype.getAllFields = function() {
    var elements = this.currentDocument.getElementsByTagName('input');
    var result = '';

    for (var i = 0; i < elements.length; i++) {
            if (elements[i].type == 'text') {
                    result += elements[i].id;

                    result += ',';
            }
    }

    return result;
}

PageBot.prototype.getAllLinks = function() {
    var elements = this.currentDocument.getElementsByTagName('a');
    var result = '';

    for (var i = 0; i < elements.length; i++) {
            result += elements[i].id;

            result += ',';
    }

    return result;
}

PageBot.prototype.setContext = function(strContext) {
     //set the current test title
     context.innerHTML=strContext;
}



function isDefined(value) {
    return typeof(value) != undefined;
}



