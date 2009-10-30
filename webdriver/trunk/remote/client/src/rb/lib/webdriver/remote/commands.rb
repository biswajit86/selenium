class WebDriver::Remote::Bridge
  command :addCookie,                       :post,    "session/:session_id/:context/cookie"
  command :goBack,                          :post,    "session/:session_id/:context/back"
  command :clearElement,                    :post,    "session/:session_id/:context/element/:id/clear"
  command :clickElement,                    :post,    "session/:session_id/:context/element/:id/click"
  command :close,                           :delete,  "session/:session_id/:context/window"
  command :getCurrentUrl,                   :get,     "session/:session_id/:context/url"
  command :deleteAllCookies,                :delete,  "session/:session_id/:context/cookie"
  command :deleteCookie,                    :delete,  "session/:session_id/:context/cookie/:name"
  command :dragElement,                     :post,    "session/:session_id/:context/element/:id/drag"
  command :elementEquals,                   :get,     "session/:session_id/:context/element/:id/equals/:other"
  command :executeScript,                   :post,    "session/:session_id/:context/execute"
  command :findElement,                     :post,    "session/:session_id/:context/element"
  command :findElements,                    :post,    "session/:session_id/:context/elements"
  command :findChildElement,                :post,    "session/:session_id/:context/element/:id/element/:using"
  command :findChildElements,               :post,    "session/:session_id/:context/element/:id/elements/:using"
  command :goForward,                       :post,    "session/:session_id/:context/forward"
  command :get,                             :post,    "session/:session_id/:context/url"
  command :getActiveElement,                :post,    "session/:session_id/:context/element/active"
  command :getAllCookies,                   :get,     "session/:session_id/:context/cookie"
  # command :getCookie # TODO: getCookie
  command :getCurrentWindowHandle,          :get,     "session/:session_id/:context/window_handle"
  command :getElementAttribute,             :get,     "session/:session_id/:context/element/:id/attribute/:name"
  command :getElementLocation,              :get,     "session/:session_id/:context/element/:id/location"
  command :getElementSize,                  :get,     "session/:session_id/:context/element/:id/size"
  command :getElementText,                  :get,     "session/:session_id/:context/element/:id/text"
  command :getElementValue,                 :get,     "session/:session_id/:context/element/:id/value"
  command :getSpeed,                        :get,     "session/:session_id/:context/speed"
  command :getElementTagName,               :get,     "session/:session_id/:context/element/:id/name"
  command :getTitle,                        :get,     "session/:session_id/:context/title"
  command :getElementValueOfCssProperty,    :get,     "session/:session_id/:context/element/:id/css/:property_name"
  command :getVisible,                      :get,     "session/:session_id/:context/visible"
  command :getWindowHandles,                :get,     "session/:session_id/:context/window_handles"
  command :hoverOverElement,                :post,    "session/:session_id/:context/element/:id/hover"
  command :isElementDisplayed,              :get,     "session/:session_id/:context/element/:id/displayed"
  command :isElementEnabled,                :get,     "session/:session_id/:context/element/:id/enabled"
  command :isElementSelected,               :get,     "session/:session_id/:context/element/:id/selected"
  command :newSession,                      :post,    "session"
  command :getPageSource,                   :get,     "session/:session_id/:context/source"
  command :quit,                            :delete,  "session/:session_id"
  command :refresh,                         :post,    "session/:session_id/:context/refresh"
  command :screenshot,                      :get,     "session/:session_id/:context/screenshot"
  command :sendKeysToElement,               :post,    "session/:session_id/:context/element/:id/value"
  command :setElementSelected,              :post,    "session/:session_id/:context/element/:id/selected"
  command :setSpeed,                        :post,    "session/:session_id/:context/speed"
  command :setVisible,                      :post,    "session/:session_id/:context/visible"
  command :submitElement,                   :post,    "session/:session_id/:context/element/:id/submit"
  command :switchToFrame,                   :post,    "session/:session_id/:context/frame/:id"
  # command :switchToFrameByIndex # TODO: switchToFrameByIndex
  # command :switchToDefaultContent # TODO: switchToDefaultContent
  command :switchToWindow,                  :post,    "session/:session_id/:context/window/:name"
  command :toggleElement,                   :post,    "session/:session_id/:context/element/:id/toggle"
end