"use strict";

class LogFileView {

    constructor(elementId) {
        this.editor = ace.edit("logFileView");
        this._autoScrollEnabled = false;
        this._init(this.editor);
    }

    _init(editor) {
        ace.config.setModuleUrl('ace/mode/serverlog', 'mode-serverlog.js');
        editor.session.setMode('ace/mode/serverlog');

        editor.setReadOnly(true);
        editor.renderer.setShowGutter(false); // no line numbers

        editor.on('focus', () => this._notifyUserScrolled());
        // 'scroll' also fires when scrolling programmatically so use 'mousewheel' to detect when use scrolls
        editor.on('mousewheel', () => this._notifyUserScrolled());
        // user dragging the scrollbar
        editor.renderer.scrollBarV.element.addEventListener('mousedown', () => this._notifyUserScrolled());
        editor.renderer.scrollBarH.element.addEventListener('mousedown', () => this._notifyUserScrolled());
    }

    set autoScrollEnabled(enabled) {
        this._autoScrollEnabled = enabled;
        if (enabled) {
            this._scrollToEnd();
        }
    }

    get autoScrollEnabled() {
        this._autoScrollEnabled;
    }

    set onUserScrolled(callback) {
        this._onUserScrolledCallback = callback;
    }

    set onClickableLogLineAdded(callback) {
        this._onClickableLogLineAddedCallback = callback;
    }

    addLogLines(logLines) {
        const doc = this._getDocument();
        var textLines = [];
        var currentRow = doc.getLength() - 2;
        var specialLines = [];

        const scrollToFunction = (function (editor, row) {
            return function () {
                editor.scrollToLine(row, false, false);
            }
        });

        for (let logLine of logLines) {
            currentRow += 1;
            textLines.push(logLine.text);

            let cssClass = this._getCssClass(logLine.text);
            if (!cssClass) {
                continue;
            }
            specialLines.push({
                scrollTo: scrollToFunction(this.editor, currentRow),
                row: currentRow,
                text: logLine.text,
                'cssClass': cssClass
            });
        }
        doc.insertFullLines(doc.getLength() - 1, textLines);
        this._ensureFunction(this._onClickableLogLineAddedCallback)(specialLines);
        if (this._autoScrollEnabled) {
            this._scrollToEnd();
        }
    }

    _getCssClass(linetext) {
        if (!this._highLightRules) {
            // for some reason editor.session.getMode() does not give the serverlog rules when invoked in ctor
            this._highLightRules = this._getHighlightRules();
        }
        for (let rule of this._highLightRules) {
            if (rule.regex.test(linetext)) {
                return 'ace_' + rule.token;
            }
        }
        return null;
    }

    _getHighlightRules() {
        var rules = this.editor.session.getMode().HighlightRules;
        return (new rules()).getRules()['start'];
    }

    isFullyLoaded() {
        return this._getHighlightRules().filter(rule => rule.token === 'warn').length === 1;
    }

    clear() {
        const doc = this._getDocument();
        doc.removeLines(0, doc.getLength() - 1);
    }

    _scrollToEnd() {
        this.editor.scrollToLine(this.editor.session.getDocument().getLength() - 1, false, false);
    }

    _notifyUserScrolled() {
        this._ensureFunction(this._onUserScrolledCallback)();
    }

    _ensureFunction(functionToEnsure) {
        return functionToEnsure ? functionToEnsure : (/* anyArgs*/) => null;
    }

    _getDocument() {
        return this.editor.session.getDocument();
    }
}