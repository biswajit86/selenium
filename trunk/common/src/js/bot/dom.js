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
 * @fileoverview DOM manipulation and querying routines.
 *
*
 */


goog.provide('bot.dom');

goog.require('bot');
goog.require('goog.array');
goog.require('goog.style');



/**
 * Determines whether or not the element has an attribute or property of the
 * given name, regardless of the value of the attribute.
 *
 * @param {!Element} element The element to use
 * @param {string} attributeName The name of the attribute
 * @return {boolean} Whether the attribute is present.
 */
bot.dom.hasAttribute = function(element, attributeName) {
  if (goog.isFunction(element['hasAttribute'])) {

    // But it might be an element property....
    if (element.hasAttribute(attributeName)) {
      return true;
    }
  }

  // hasAttributes method is missing. IE 8 and above have an
  // attributes array so use that if present.
  if (goog.isArrayLike(element['attributes'])) {
    // We want to indicate that the attribute is present, regardless
    // of value.
    if (element.attributes[attributeName] ||
        element.attributes[attributeName] == false) {
      return true;
    }
  }

  // No attributes array, or may be a property. Query the object
  return attributeName in element;
};

/**
 * Used to determine whether we should return a boolean value from getAttribute.
 *
 * @const
 * @private
 */
bot.dom.BOOLEAN_ATTRIBUTES_ = [
  'checked',
  'disabled',
  'readOnly',
  'selected'
];

/**
 * Get the value of the given attribute or property of the
 * element. This method will endeavour to return consistent values
 * between browsers. For example, boolean values for attributes such
 * as "selected" or "checked" will always be returned as "true" or
 * "false".
 *
 * @param {!Element} element The element to use.
 * @param {string} attributeName The name of the attribute to return.
 * @return {string|boolean} The value of the attribute or "null" if entirely
 *     missing.
 */
bot.dom.getAttribute = function(element, attributeName) {
  var lattr = attributeName.toLowerCase();

  // TODO(user): What's the right thing to do here?
  if ('style' == lattr) {
    return '';
  }

  // Commonly looked up attributes that are aliases
  if ('class' == lattr) {
    attributeName = 'className';
  }

  if ('readonly' == lattr) {
    attributeName = 'readOnly';
  }

  if (!bot.dom.hasAttribute(element, attributeName)) {
    return null;
  }

  var value = element[attributeName] === undefined ?
      element.getAttribute(attributeName) : element[attributeName];

  // Handle common boolean values
  if (goog.array.contains(bot.dom.BOOLEAN_ATTRIBUTES_, attributeName)) {
    value = !!value && value != 'false';
  }

  return value;
};
