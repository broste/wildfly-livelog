"use strict";

const CONNECTION_SETUP_TIMEOUT_MS = 3000;

class WebSocketClient {

    constructor(url) {
        this.url = url;
        this._connectToServer();
        this._logLineListeners = [];
        this._isConnected = false;
    }

    set connectionStateListener(listener) {
        this._connectionStateListener = listener;
    }

    addLogLineListener(listener) {
        this._logLineListeners.push(listener);
    }

    set errorListener(listener) {
        this._errorListener = listener;
    }

    _onerror(event) {
        _log('onerror ' + event);
        this._ensureListener(this._errorListener)(event);
    }

    _onopen(event) {
        _log('onopen ' + event);
        this._isConnected = true;
        clearTimeout(this._connectionSetupTimerId);
        this._ensureListener(this._connectionStateListener)(true);
    }

    _onclose(event) {
        _log('onclose ' + event);
        this._ensureListener(this._connectionStateListener)(false);
        if (this._isConnected) {
            this._isConnected = false;
            this._connectToServer();
        }

    }

    _onmessage(event) {
        if (this._logLineListeners.length !== 0) {
            // lines will always be complete as the server only submits complete lines and websocket messages are also always received as complete messages
            let lines = event.data.split('\n');
            let logLines = [];
            for (let i = 0; i < lines.length; i++) {
                const line = lines[i];
                if (i === lines.length-1 && line === '') {
                    continue;
                }
                const logLevel = this._getLogLevel(line);
                logLines.push(new LogLine(logLevel, line));
            }
            this._logLineListeners.forEach(listener => listener(logLines));
        }
    }

    _getLogLevel(line) {
        const level = line.split(/\s+/)[2];
        return LogLevel.levels.includes(level) ? level : null;
    }

    _ensureListener(callback) {
        return callback ? callback : (/* any args*/) => null;
    }

    _connectToServer() {
        _log('connecting to server');
        if (this._websocket) {
            this._websocket.onmessage = null;
            this._websocket.onerror = null;
            this._websocket.onclose = null;
            this._websocket.onopen = null;
            this._websocket.close();
        }
        this._connectionSetupTimerId = setTimeout(() => this._connectToServer(), CONNECTION_SETUP_TIMEOUT_MS);
        this._websocket = new WebSocket(this.url);
        this._websocket.onopen = (e) => this._onopen(e);
        this._websocket.onclose = (e) => this._onclose(e);
        this._websocket.onmessage = (e) => this._onmessage(e);
        this._websocket.onerror = (e) =>  this._onerror(e);
    }
}

function _log(message) {
    console.log(new Date().toISOString() + ' ' + message);
}
