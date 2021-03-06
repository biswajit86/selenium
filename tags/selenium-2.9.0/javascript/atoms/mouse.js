// Copyright 2011 WebDriver committers
// Copyright 2011 Google Inc.
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

/**
 * @fileoverview The file contains an abstraction of a mouse for
 * simulating the mouse actions.
 *
 */

goog.provide('bot.Mouse');
goog.provide('bot.Mouse.Button');

goog.require('bot');
goog.require('bot.Error');
goog.require('bot.ErrorCode');
goog.require('bot.dom');
goog.require('bot.events');
goog.require('bot.userAgent');
goog.require('goog.dom');
goog.require('goog.dom.Range');
goog.require('goog.dom.TagName');
goog.require('goog.events.EventType');
goog.require('goog.math.Coordinate');
goog.require('goog.style');
goog.require('goog.userAgent');



/**
 * A mouse that provides atomic mouse actions. This mouse currently only
 * supports having one button pressed at a time.
 *
 * @constructor
 */
bot.Mouse = function() {
  /**
   * @type {!Element}
   * @private
   */
  this.element_ = bot.getDocument().documentElement;

  /**
   * @type {?bot.Mouse.Button}
   * @private
   */
  this.buttonPressed_ = null;

  /**
   * @type {Element}
   * @private
   */
  this.elementPressed_ = null;

  /**
   * @type {!goog.math.Coordinate}
   * @private
   */
  this.clientXY_ = new goog.math.Coordinate(0, 0);

  /**
   * @type {boolean}
   * @private
   */
  this.nextClickIsDoubleClick_ = false;
};


/**
 * Enumeration of mouse buttons that can be pressed.
 *
 * @enum {number}
 */
bot.Mouse.Button = {
  LEFT: 0,
  MIDDLE: 1,
  RIGHT: 2
};


/**
 * Whether extra handling needs to be considered when clicking on a link or a
 * submit button.
 *
 * @type {boolean}
 * @private
 * @const
 */
bot.Mouse.EXPLICIT_FOLLOW_LINK_ = goog.userAgent.IE ||
    // Normal firefox
    (goog.userAgent.GECKO && !bot.isFirefoxExtension()) ||
    // Firefox extension prior to Firefox 4
    (goog.userAgent.GECKO && bot.isFirefoxExtension() &&
        !bot.userAgent.isVersion(4));


/**
 * Index to indicate no button pressed in bot.Mouse.MOUSE_BUTTON_VALUE_MAP_.
 *
 * @type {number}
 * @private
 * @const
 */
bot.Mouse.NO_BUTTON_VALUE_INDEX_ = 3;


/**
 * Maps mouse events to an array of button argument value for each mouse button.
 * The array is indexed by the bot.Mouse.Button values. It encodes this table,
 * where each cell contains the (left/middle/right/none) button values.
 *               click/    mouseup/   mouseout/  mousemove  contextmenu
 *               dblclick/ mousedown  mouseover
 * IE            0 0 0 X   1 4 2 X    0 0 0 0    1 4 2 0    X X 0 X
 * WEBKIT        0 1 2 X   0 1 2 X    0 1 2 0    0 1 2 0    X X 2 X
 * GECKO/OPERA   0 1 2 X   0 1 2 X    0 0 0 0    0 0 0 0    X X 2 X
 *
 * @type {!Object.<string, !Array.<?number>>}
 * @private
 * @const
 */
bot.Mouse.MOUSE_BUTTON_VALUE_MAP_ = (function() {
  var buttonValueMap = {};
  if (goog.userAgent.IE) {
    buttonValueMap[goog.events.EventType.CLICK] = [0, 0, 0, null];
    buttonValueMap[goog.events.EventType.CONTEXTMENU] = [null, null, 0, null];
    buttonValueMap[goog.events.EventType.MOUSEUP] = [1, 4, 2, null];
    buttonValueMap[goog.events.EventType.MOUSEOUT] = [0, 0, 0, 0];
    buttonValueMap[goog.events.EventType.MOUSEMOVE] = [1, 4, 2, 0];
  } else if (goog.userAgent.WEBKIT) {
    buttonValueMap[goog.events.EventType.CLICK] = [0, 1, 2, null];
    buttonValueMap[goog.events.EventType.CONTEXTMENU] = [null, null, 2, null];
    buttonValueMap[goog.events.EventType.MOUSEUP] = [0, 1, 2, null];
    buttonValueMap[goog.events.EventType.MOUSEOUT] = [0, 1, 2, 0];
    buttonValueMap[goog.events.EventType.MOUSEMOVE] = [0, 1, 2, 0];
  } else {
    buttonValueMap[goog.events.EventType.CLICK] = [0, 1, 2, null];
    buttonValueMap[goog.events.EventType.CONTEXTMENU] = [null, null, 2, null];
    buttonValueMap[goog.events.EventType.MOUSEUP] = [0, 1, 2, null];
    buttonValueMap[goog.events.EventType.MOUSEOUT] = [0, 0, 0, 0];
    buttonValueMap[goog.events.EventType.MOUSEMOVE] = [0, 0, 0, 0];
  }

  buttonValueMap[goog.events.EventType.DBLCLICK] =
      buttonValueMap[goog.events.EventType.CLICK];
  buttonValueMap[goog.events.EventType.MOUSEDOWN] =
      buttonValueMap[goog.events.EventType.MOUSEUP];
  buttonValueMap[goog.events.EventType.MOUSEOVER] =
      buttonValueMap[goog.events.EventType.MOUSEOUT];
  return buttonValueMap;
})();


/**
 * Whether synthesized events are trusted to trigger click actions.
 *
 * @type {boolean}
 * @private
 * @const
 */
bot.Mouse.CAN_SYNTHESISED_EVENTS_FOLLOW_LINKS_ =
  (goog.userAgent.GECKO &&
   bot.isFirefoxExtension() &&
   bot.userAgent.isVersion(4));


/**
 * Whether synthesized events can cause new windows to open.
 *
 * @type {boolean}
 * @const
 * @private
 */
bot.Mouse.SYNTHESISED_EVENTS_CAN_OPEN_JAVASCRIPT_WINDOWS_ =
  goog.userAgent.GECKO && bot.isFirefoxExtension();


/**
 * On some browsers, a mouse down event on an OPTION or SELECT element cause
 * the SELECT to open, blocking further JS execution. This is undesirable, and
 * so needs to be detected.
 *
 * @param {!Element} element The element to check.
 * @return {boolean} Whether the element blocks js execution.
 * @private
 */
bot.Mouse.prototype.blocksOnMouseDown_ = function(element) {
  var isFirefox3 = goog.userAgent.GECKO && !bot.userAgent.isVersion(4);

  return (goog.userAgent.WEBKIT || isFirefox3) &&
      ((bot.dom.isElement(element, goog.dom.TagName.OPTION) ||
      bot.dom.isElement(element, goog.dom.TagName.SELECT)));
};


/**
 * Press a mouse button on an element that the mouse is interacting with.
 *
 * @param {!bot.Mouse.Button} button Button.
 * @return {boolean} Whether to focus on the pressed element.
*/
bot.Mouse.prototype.pressButton = function(button) {
  if (!goog.isNull(this.buttonPressed_)) {
    throw new bot.Error(bot.ErrorCode.UNKNOWN_ERROR,
        'Cannot press more then one button or an already pressed button.');
  }
  this.buttonPressed_ = button;
  this.elementPressed_ = this.element_;

  var performFocus = true;
  // TODO(user): This is a nasty way to avoid locking the browser
  if (!this.blocksOnMouseDown_(this.element_)) {
    performFocus = this.fireEvent_(goog.events.EventType.MOUSEDOWN);
  }
  return performFocus;
};


/**
 * Releases the pressed mouse button. Throws exception if no button pressed.
 *
 */
bot.Mouse.prototype.releaseButton = function() {
  if (goog.isNull(this.buttonPressed_)) {
    throw new bot.Error(bot.ErrorCode.UNKNOWN_ERROR,
        'Cannot release a button when no button is pressed.');
  }

  this.fireEvent_(goog.events.EventType.MOUSEUP);

  // TODO(user): Middle button can also trigger click.
  if (this.buttonPressed_ == bot.Mouse.Button.LEFT &&
      this.element_ == this.elementPressed_) {
    this.clickElement_();
    this.maybeDoubleClickElement_();

  // TODO(user): In Linux, this fires after mousedown event.
  } else if (this.buttonPressed_ == bot.Mouse.Button.RIGHT) {
    this.fireEvent_(goog.events.EventType.CONTEXTMENU);
  }
  this.buttonPressed_ = null;
  this.elementPressed_ = null;
};


/**
 * A helper function to fire mouse click events.
 *
 * @private
 */
bot.Mouse.prototype.clickElement_ = function() {
  // bot.events.fire(element, 'click') can trigger all onclick events, but may
  // not follow links (FORM.action or A.href).
  //     TAG      IE   GECKO  WebKit Opera
  // A(href)      No    No     Yes    Yes
  // FORM(action) No    Yes    Yes    Yes
  var targetLink = null;
  var targetButton = null;
  if (bot.Mouse.EXPLICIT_FOLLOW_LINK_) {
    for (var e = this.element_; e; e = e.parentNode) {
      if (bot.dom.isElement(e, goog.dom.TagName.A)) {
        targetLink = /**@type {!Element}*/ (e);
        break;
      } else if (bot.Mouse.isFormSubmitElement_(e)) {
        targetButton = e;
        break;
      }
    }
  }

  var selectable = bot.dom.isSelectable(this.element_);
  var originallySelected = selectable && bot.dom.isSelected(this.element_);

  // NOTE(user): Clicking on a form submit button is a little broken:
  // (1) When clicking a form submit button in IE, firing a click event or
  // calling Form.submit() will not by itself submit the form, so we call
  // Element.click() explicitly, but as a result, the coordinates of the click
  // event are not provided. Also, when clicking on an <input type=image>, the
  // coordinates click that are submitted with the form are always (0, 0).
  // (2) When clicking a form submit button in GECKO, while the coordinates of
  // the click event are correct, those submitted with the form are always (0,0)
  // .
  // TODO(user): See if either of these can be resolved, perhaps by adding
  // hidden form elements with the coordinates before the form is submitted.
  if (goog.userAgent.IE && targetButton) {
    targetButton.click();
    return;
  }

  var performDefault = this.fireEvent_(goog.events.EventType.CLICK);
  if (!performDefault) {
    return;
  }

  if (targetLink && bot.Mouse.shouldFollowHref_(targetLink)) {
    bot.Mouse.followHref_(targetLink);
  }

  if (!selectable || !bot.dom.isInteractable(this.element_)) {
    return;
  }

  if (originallySelected) {
    // If this is a radio button, a click should not disable it.
    if (bot.dom.isElement(this.element_, goog.dom.TagName.INPUT) &&
        this.element_.type && this.element_.type.toLowerCase() == 'radio') {
      return;
    }

    // If this is an <option> in a single-select, click should not disable it.
    if (bot.dom.isElement(this.element_, goog.dom.TagName.OPTION)) {
      // Parent <select> guaranteed to exist for <option> to be interactable.
      var select = (/** @type {!Element} */
          goog.dom.getAncestor(this.element_, function(node) {
            return bot.dom.isElement(node, goog.dom.TagName.SELECT);
          }));
      if (!select.multiple) {
        return;
      }
    }
  }

  // TODO(user): Should not call stuff in bot.action since we're not
  // including it in mouse.js
  bot.action.setSelected(this.element_, !originallySelected);
};


/**
 * A helper function to fire mouse double click events.
 *
 * @private
 */
bot.Mouse.prototype.maybeDoubleClickElement_ = function() {
  // Trigger an additional double click event if it is the second click.
  if (this.nextClickIsDoubleClick_) {
    this.fireEvent_(goog.events.EventType.DBLCLICK);
  }
  this.nextClickIsDoubleClick_ = !this.nextClickIsDoubleClick_;
};


/**
 * Given a coordinates (x,y) related to an element, move mouse to (x,y) of the
 * element. The top-left point of the element is (0,0).
 *
 * @param {!Element} element The destination element.
 * @param {!goog.math.Coordinate} coords Mouse position related to the target.
 */
bot.Mouse.prototype.move = function(element, coords) {
  var pos = goog.style.getClientPosition(element);
  this.clientXY_.x = coords.x + pos.x;
  this.clientXY_.y = coords.y + pos.y;

  if (element != this.element_) {
    this.fireEvent_(goog.events.EventType.MOUSEOUT, element);
    var prevElement = this.element_;
    this.element_ = element;
    this.fireEvent_(goog.events.EventType.MOUSEOVER, prevElement);
  }

  this.fireEvent_(goog.events.EventType.MOUSEMOVE);

  this.nextClickIsDoubleClick_ = false;
};


/**
 * A helper function to fire mouse events.
 *
 * @param {goog.events.EventType} type Event type.
 * @param {Element=} opt_related The related element of this event.
 * @return {boolean} Whether the event fired successfully or was cancelled.
 * @private
 */
bot.Mouse.prototype.fireEvent_ = function(type, opt_related) {
  // TODO(user): Event if the element is not interactable, the mouse event
  // should still fire on another element (offset parent?).
  if (!bot.dom.isInteractable(this.element_)) {
    return false;
  }

  if (goog.events.EventType.MOUSEOVER == type ||
      goog.events.EventType.MOUSEOUT == type) {
    if (!opt_related) {
      throw new bot.Error(bot.ErrorCode.INVALID_ELEMENT_STATE,
          'Event type requires related target: ' + type);
    }
  }

  var args = {
    clientX: this.clientXY_.x,
    clientY: this.clientXY_.y,
    button: this.getButtonValue_(type),
    bubble: true,
    alt: false,
    control: false,
    shift: false,
    meta: false,
    related: opt_related || null
  };

  return bot.events.fire(this.element_, type, args);
};


/**
 * Given an event type and a mouse button, sets the mouse button value used
 * for that event on the current browser. The mouse button value is 0 for any
 * event not covered by bot.Mouse.MOUSE_BUTTON_VALUE_MAP_.
 *
 * @param {string} eventType Type of mouse event.
 * @return {number} The mouse button ID value to the current browser.
 * @private
*/
bot.Mouse.prototype.getButtonValue_ = function(eventType) {
  if (!(eventType in bot.Mouse.MOUSE_BUTTON_VALUE_MAP_)) {
    return 0;
  }

  var buttonIndex = goog.isNull(this.buttonPressed_) ?
      bot.Mouse.NO_BUTTON_VALUE_INDEX_ : this.buttonPressed_;
  var buttonValue = bot.Mouse.MOUSE_BUTTON_VALUE_MAP_[eventType][buttonIndex];
  if (goog.isNull(buttonValue)) {
    throw new bot.Error(bot.ErrorCode.UNKNOWN_ERROR,
        'Event does not permit the specified mouse button.');
  }
  return buttonValue;
};


/**
 * @param {Node} element The element to check.
 * @return {boolean} Whether the element is a submit element in form.
 * @private
 */
bot.Mouse.isFormSubmitElement_ = function(element) {
  if (bot.dom.isElement(element, goog.dom.TagName.INPUT)) {
    var type = element.type.toLowerCase();
    if (type == 'submit' || type == 'image') {
      return true;
    }
  }

  if (bot.dom.isElement(element, goog.dom.TagName.BUTTON)) {
    var type = element.type.toLowerCase();
    if (type == 'submit') {
      return true;
    }
  }
  return false;
};

/**
 * Indicates whether we should manually follow the href of the element we're
 * clicking.
 *
 * Versions of firefox from 4+ will handle links properly when this is used in
 * an extension. Versions of Firefox prior to this may or may not do the right
 * thing depending on whether a target window is opened and whether the click
 * has caused a change in just the hash part of the URL.
 *
 * @param {!Element} element The element to consider.
 * @return {boolean} Whether following an href should be skipped.
 * @private
 */
bot.Mouse.shouldFollowHref_ = function(element) {
  if (!(element && element.href)) {
    return false;
  }

  if (goog.userAgent.IE ||
      (goog.userAgent.GECKO && !bot.isFirefoxExtension())) {
    return true;
  }

  if (bot.Mouse.CAN_SYNTHESISED_EVENTS_FOLLOW_LINKS_) {
    return false;
  }

  var href = element.href + ''; // coerce to a string.
  if (element.target || href.toLowerCase().indexOf('javascript') == 0) {
    return !bot.Mouse.SYNTHESISED_EVENTS_CAN_OPEN_JAVASCRIPT_WINDOWS_;
  }

  var owner = goog.dom.getWindow(goog.dom.getOwnerDocument(element));
  var sourceUrl = owner.location.href;
  var destinationUrl = bot.Mouse.resolveUrl_(owner.location, element.href);
  var isOnlyHashChange =
      sourceUrl.split('#')[0] === destinationUrl.split('#')[0];

  return !isOnlyHashChange;
};


/**
 * Explicitly follows the href of an anchor.
 *
 * @param {!Element} anchorElement An anchor element.
 * @private
 */
bot.Mouse.followHref_ = function(anchorElement) {
  var targetHref = anchorElement.href;
  var owner = goog.dom.getWindow(goog.dom.getOwnerDocument(anchorElement));

  // IE7 and earlier incorrect resolve a relative href against the top window
  // location instead of the window to which the href is assigned. As a result,
  // we have to resolve the relative URL ourselves. We do not use Closure's
  // goog.Uri to resolve, because it incorrectly fails to support empty but
  // undefined query and fragment components and re-encodes the given url.
  if (goog.userAgent.IE && !goog.userAgent.isVersion(8)) {
    targetHref = bot.Mouse.resolveUrl_(owner.location, targetHref);
  }

  if (anchorElement.target) {
    owner.open(targetHref, anchorElement.target);
  } else {
    owner.location.href = targetHref;
  }
};


/**
 * Regular expression for splitting up a URL into components.
 * @type {!RegExp}
 * @private
 * @const
 */
bot.Mouse.URL_REGEXP_ = new RegExp(
    '^' +
    '([^:/?#.]+:)?' +   // protocol
    '(?://([^/]*))?' +  // host
    '([^?#]+)?' +       // pathname
    '(\\?[^#]*)?' +     // search
    '(#.*)?' +          // hash
    '$');


/**
 * Resolves a potentially relative URL against a base location.
 * @param {!Location} base Base location against which to resolve.
 * @param {string} rel Url to resolve against the location.
 * @return {string} Resolution of url against base location.
 * @private
 */
bot.Mouse.resolveUrl_ = function(base, rel) {
  var m = rel.match(bot.Mouse.URL_REGEXP_);
  if (!m) {
    return '';
  }
  var target = {
    protocol: m[1] || '',
    host: m[2] || '',
    pathname: m[3] || '',
    search: m[4] || '',
    hash: m[5] || ''
  };

  if (!target.protocol) {
    target.protocol = base.protocol;
    if (!target.host) {
      target.host = base.host;
      if (!target.pathname) {
        target.pathname = base.pathname;
        target.search = target.search || base.search;
      } else if (target.pathname.charAt(0) != '/') {
        var lastSlashIndex = base.pathname.lastIndexOf('/');
        if (lastSlashIndex != -1) {
          var directory = base.pathname.substr(0, lastSlashIndex + 1);
          target.pathname = directory + target.pathname;
        }
      }
    }
  }

  return target.protocol + '//' + target.host + target.pathname +
      target.search + target.hash;
};

