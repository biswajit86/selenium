
goog.provide('bot.locators.strategies.id');

goog.require('bot.dom');
goog.require('goog.array');
goog.require('goog.dom');

/**
 * Find an element by using the value of the ID attribute
 * @param {Window} win The DOM window to search in.
 * @param {string} target The id to search for.
 * @param {Node=} opt_root The node from which to start the search
 * @return {?Element} The first matching element found in the DOM, or null if no
 *     such element could be found.
 */
bot.locators.strategies.id.single = function(win, target, opt_root) {
  var doc = goog.dom.getOwnerDocument(win);
  var root = opt_root || doc;

  var e = doc.getElementById(target);
  if (!e) {
    return null;
  }

  // On IE getting by ID returns the first match by id _or_ name.
  // In IE 7- getElementById is case insensitive. 
  if (e.getAttribute('id') == target && goog.dom.contains(root, e)) {
    return e;
  }

  var elements = doc.getElementsByTagName('*');

  var length = elements.length;
  for (var i = 0; i < length; i++) {
    var element = elements[i];

    // Lifted from selenium's original finder.
    if (element.tagName.toLowerCase() == 'form') {
      if (element.attributes['id'].nodeValue == target &&
          goog.dom.contains(root, element)) {
        return element;
      }
    } else if (bot.dom.getAttribute(element, 'id') == target &&
               goog.dom.contains(root, element)) {
      return element;
    }
  }

  return null;
};

/**
 * Find many elements by using the value of the ID attribute
 * @param {Window} win The DOM window to search in.
 * @param {string} target The id to search for.
 * @param {Node=} opt_root The node from which to start the search
 * @return {goog.array.ArrayLike} All matching elements, or an empty list
 */
bot.locators.strategies.id.many = function(win, target, opt_root) {
  var doc = goog.dom.getOwnerDocument(win);
  var root = opt_root || doc;

  var elements = doc.getElementsByTagName('*');

  return goog.array.filter(elements, function(e) {
    return bot.dom.getAttribute(e, 'id') == target && goog.dom.contains(root, e);
  });
};
