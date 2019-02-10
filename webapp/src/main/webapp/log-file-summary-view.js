"use strict";

class LogFileSummaryView {

    constructor(contentElement) {
        this._contentElement = contentElement;
        this._document = contentElement.ownerDocument;

        this._listEl = this._document.createElement('ul');
        this._contentElement.appendChild(this._listEl);
        this._warningsEnabled = true;
    }

    set warningsEnabled(enabled) {
        this._warningsEnabled = enabled;
        const warningItems = this._listEl.querySelectorAll('li.ace_warn');
        warningItems.forEach(item => this._setElementVisible(item, enabled));
    }

    addLinkableLogLine(lines) {
        const document = this._contentElement.ownerDocument;

        const wrapInClosureForOldFirefox = function (scrollTo) {
            return function () {
                scrollTo();
            }
        };

        for (let line of lines) {
            // We need to wrap the text in a span so that the background color is applied to the whole text length.
            // If we apply it to the li only the background color will only cover the visible area, i.e.
            // when we scroll the color will not cover all the text.
            var span = document.createElement('span');
            span.innerText = line.text;
            span.onclick = wrapInClosureForOldFirefox(line.scrollTo);

            var li = document.createElement('li');
            // make sure the color covers at least all the area in view:
            li.className += ' ' + line.cssClass;
            li.appendChild(span);

            if (line.cssClass === 'ace_warn') {
                this._setElementVisible(li, this._warningsEnabled);
            }

            if (this._listEl.hasChildNodes()) {
                this._listEl.insertBefore(li, this._listEl.firstChild);
            }
            else {
                this._listEl.appendChild(li);
            }
        }
    }

    clear() {
        this._listEl.innerHTML = '';
    }

    _setElementVisible(element, visible) {
        return element.style.display = visible ? 'block' : 'none';
    }
}