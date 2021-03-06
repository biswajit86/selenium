/*
 * Copyright 2006 Shinya Kasatani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var CommandBuilders = {};

CommandBuilders.builders = [];

CommandBuilders.add = function(commandType, func) {
	this.builders.push({builder: func, commandType: commandType});
}

CommandBuilders.getRecorder = function(window) {
	return Recorder.get(window);
}

CommandBuilders.callBuilder = function(builder, window) {
	var command = builder.builder.call(this, window);
	['name', 'target', 'value'].forEach(function(name) {
			if (command[name] == null) command[name] = '';
		});
	command.window = window;
	return command;
}

/*
 * add builders
 */

CommandBuilders.add('action', function(window) {
		var path = window.location.href;
		var base = '';
		var r = /^(\w+:\/\/[\w\.-]+(:\d+)?)\/.*/.exec(path);
		if (r) {
			path = path.substr(r[1].length);
			base = r[1] + '/';
		}
		return {
			command: "open",
			target: path
		};
	});

CommandBuilders.add('accessor', function(window) {
		var result = { accessor: "textPresent", booleanAccessor: true };
		var selection = String(window.getSelection());
		if (selection) {
			result.target = selection;
		} else {
			var element = this.getRecorder(window).clickedElement;
			if (element) {
				result.target = exactMatchPattern(getText(element));
			} else {
				result.disabled = true;
			}
		}
		return result;
	});

CommandBuilders.add('accessor', function(window) {
		var result = { accessor: "title" };
		if (window.document) {
			result.target = exactMatchPattern(window.document.title);
		} else {
			result.disabled = true;
		}
		return result;
	});

CommandBuilders.add('accessor', function(window) {
		var result = { accessor: "value" };
		var element = this.getRecorder(window).clickedElement;
		if (element && element.hasAttribute && element.tagName &&
			('input' == element.tagName.toLowerCase() || 
			 'textarea' == element.tagName.toLowerCase() || 
			 (element.value && (element.value instanceof String)))) {
			result.target = this.getRecorder(window).clickedElementLocator;
			var type = element.getAttribute("type");
			if ('input' == element.tagName.toLowerCase() && 
				(type == 'checkbox' || type == 'radio')) {
				result.value = element.checked ? 'on' : 'off';
			} else {
				result.value = exactMatchPattern(element.value);
			}
		} else {
			result.disabled = true;
		}
		return result;
	});

CommandBuilders.add('accessor', function(window) {
		var result = { accessor: "text" };
		var element = this.getRecorder(window).clickedElement;
		if (element) {
			result.target = this.getRecorder(window).clickedElementLocator;
			result.value = exactMatchPattern(getText(element));
		} else {
			result.disabled = true;
		}
		return result;
	});


CommandBuilders.add('accessor', function(window) {
		var element = this.getRecorder(window).clickedElement;
		var result = { accessor: "table", disabled: true };
		if (!element) return result;
		while (true) {
			var tagName = element.tagName.toLowerCase();
			if (tagName == 'td' || tagName == 'th') {
				break;
			}
			if (element.parentNode && element.parentNode.tagName) {
				element = element.parentNode;
			} else {
				return result;
			}
		}
		var parentTable = null;
		var temp = element.parentNode;
		while (temp != null) {
			if (temp.tagName.toLowerCase() == 'table') {
				parentTable = temp;
				break;
			}
			temp = temp.parentNode;
		}
		if (parentTable) {
			var locator = this.getRecorder(window).findLocator(parentTable);
			if (locator) {
				result.target = locator + '.' + element.parentNode.rowIndex + '.' + element.cellIndex;
				result.value = exactMatchPattern(getText(element));
				result.disabled = false;
			}
		}
		return result;
	});

CommandBuilders.add('accessor', function(window) {
		var result = { accessor: "elementPresent", booleanAccessor: true };
		var element = this.getRecorder(window).clickedElement;
		if (element) {
			result.target = this.getRecorder(window).clickedElementLocator;
		} else {
			result.disabled = true;
		}
		return result;
	});
