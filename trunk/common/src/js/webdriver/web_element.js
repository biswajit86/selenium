/** @license
Copyright 2010 WebDriver committers
Copyright 2010 Google Inc.

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

/**
 * @fileoverview Atoms-based implementation of the webelement interface
 */


goog.require('bot.dom');
goog.require('goog.dom.TagName');


goog.provide('webdriver.element');


/**
 * List of input types that support the "selected" or "checked" property.
 * @type {!Array.<string>}
 * @const
 * @private
 */
webdriver.element.SELECTABLE_TYPES_ = [
  'checkbox',
  'radio'
];


/**
 * @param {!Element} element The element to check.
 * @return {boolean} Whether the element could be checked or selected.
 * @private
 */
webdriver.element.isSelectable_ = function(element) {
  var tagName = element.tagName.toUpperCase();

  if (tagName == goog.dom.TagName.OPTION) {
    return true;
  }

  if (tagName == goog.dom.TagName.INPUT) {
    var type = element.type;

    if (goog.array.contains(webdriver.element.SELECTABLE_TYPES_, type)) {
      return true;
    }
  }

  return false;
};


/**
 * @param {!Element} element The element to use.
 * @return {boolean} Whether the element is checked or selected.
 */
webdriver.element.isSelected = function(element) {
  if (!webdriver.element.isSelectable_(element)) {
    return false;
  }

  var propertyName = 'selected';
  var tagName = element.tagName.toUpperCase();
  var type = element.type && element.type.toLowerCase();

  if ('checkbox' == type || 'radio' == type) {
    propertyName = 'checked';
  }

  return !!element[propertyName];
};


/**
 * Get the value of the given property or attribute. If the "attribute" is for
 * a boolean property, we return null in the case where the value is false. If
 * the attribute name is "style" an attempt to convert that style into a string
 * is done.
 *
 * @param {!Element} element The element to use.
 * @param {!string} attribute The name of the attribute to look up.
 * @return {string} The string value of the attribute or property, or null.
 */
webdriver.element.getAttribute = function(element, attribute) {
  var value = null;
  var name = attribute.toLowerCase();

  if ('style' == attribute.toLowerCase()) {
    value = element.style;

    if (value && !goog.isString(value)) {
      value = value.cssText;
    }
  } else if ('selected' == name || 'checked' == name && webdriver.element.isSelectable_(element)) {
    value = webdriver.element.isSelected(element) ? "true" : null;
  } else if (bot.dom.hasProperty(element, attribute)) {
    value = bot.dom.getProperty(element, attribute);
  } else if (bot.dom.hasAttribute(element, attribute)) {
    value = bot.dom.getAttribute(element, attribute);
  }

  return value ? value.toString() : null;
};
