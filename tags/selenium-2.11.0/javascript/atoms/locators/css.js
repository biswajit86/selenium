// Copyright 2010 WebDriver committers
// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// TODO(user): Add support for using sizzle to locate elements

goog.provide('bot.locators.css');

goog.require('goog.array');
goog.require('goog.dom');
goog.require('goog.dom.NodeType');
goog.require('goog.object');
goog.require('goog.string');
goog.require('goog.userAgent');


/**
 * Find an element by using a CSS selector
 *
 * @param {string} target The selector to search for.
 * @param {!(Document|Element)} root The document or element to perform the
 *     search under.
 * @return {Element} The first matching element found in the DOM, or null if no
 *     such element could be found.
 */
bot.locators.css.single = function(target, root) {
  if (!goog.isFunction(root['querySelector']) &&
      // IE8 in non-compatibility mode reports querySelector as an object.
      goog.userAgent.IE && goog.userAgent.isVersion(8) &&
      !goog.isObject(root['querySelector'])) {
    throw Error('CSS selection is not supported');
  }

  if (!target) {
    throw Error('No selector specified');
  }

  if (bot.locators.css.containsUnquotedComma_(target)) {
    throw Error('Compound selectors not permitted');
  }

  target = goog.string.trim(target);

  var element = root.querySelector(target);

  return element && element.nodeType == goog.dom.NodeType.ELEMENT ?
      (/**@type {Element}*/element) : null;
};


/**
 * Find all elements matching a CSS selector.
 *
 * @param {string} target The selector to search for.
 * @param {!(Document|Element)} root The document or element to perform the
 *     search under.
 * @return {!goog.array.ArrayLike} All matching elements, or an empty list.
 */
bot.locators.css.many = function(target, root) {
  if (!goog.isFunction(root['querySelectorAll']) &&
      // IE8 in non-compatibility mode reports querySelector as an object.
      goog.userAgent.IE && goog.userAgent.isVersion(8) &&
      !goog.isObject(root['querySelector'])) {
    throw Error('CSS selection is not supported');
  }

  if (!target) {
    throw Error('No selector specified');
  }

  if (bot.locators.css.containsUnquotedComma_(target)) {
    throw Error('Compound selectors not permitted');
  }

  target = goog.string.trim(target);

  return root.querySelectorAll(target);
};

/**
 * Check if the string contains a comma outside a quoted string
 *
 * @param {string} string to check for commas outside a quoted blcok
 * @return {boolean}
 */
bot.locators.css.containsUnquotedComma_ = function(str) {
  return str.split(/(,)(?=(?:[^']|'[^']*')*$)/).length > 1 && str.split(/(,)(?=(?:[^"]|"[^"]*")*$)/).length > 1
}
