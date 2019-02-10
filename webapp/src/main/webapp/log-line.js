"use strict";

class LogLevel {

    static get DEBUG() { return "DEBUG"; }
    static get INFO() { return "INFO"; }
    static get WARN() { return "WARN"; }
    static get ERROR() { return "ERROR"; }

    static get levels() { return [LogLevel.WARN, LogLevel.ERROR, LogLevel.INFO]; }

    static cssClass(logLevel) {
        switch (logLevel) {
            case LogLevel.INFO:
                return 'logInfo';
            case LogLevel.WARN:
                return 'logWarn';
            case LogLevel.ERROR:
                return 'logError';
            default: return '';
        }
    }
}

class LogLine {

    constructor(level, text) {
        this._level = level;
        this._text = text;
    }

    get level() {
        return this._level;
    }

    get text() {
        return this._text;
    }

}

