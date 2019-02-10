'use strict';

requirejs.config({baseUrl: 'modules'});


require(['split'], function (Splitter) {

    window.onerror = (message, source, lineno, colno) => {
        var error = document.createElement("p");
        error.innerText = message + ' (' + source + ' ' + lineno + ':' + colno + ')';
        document.getElementById('jsErrors').appendChild(error);
    };

    const splitter = Splitter(
        ['#logFileSummaryContainer', '#logFileContainer'], {
            direction: 'vertical',
            minSize: 50,
            sizes: [20, 80],
            // resize event for preventing a second scroll bar on appearing on ace when making the editor smaller
            onDrag: sizes => {
                window.dispatchEvent(new Event('resize'))
            }
        });

    const autoScrollCheckBox = document.getElementById('autoScrollCb');
    autoScrollCheckBox.onchange = (e) => logFileView.autoScrollEnabled = e.target.checked;

    const warningsCb = document.getElementById('warningsCb');
    warningsCb.onchange = (e) => logFileSummaryView.warningsEnabled = e.target.checked;

    const logFileSummaryView = new LogFileSummaryView(document.getElementById('logFileSummaryView'));

    const logFileView = new LogFileView('logFileView');
    logFileView.onUserScrolled = () => {
        autoScrollCheckBox.checked = false;
        logFileView.autoScrollEnabled = false;
    };

    logFileView.onClickableLogLineAdded = (lines) => logFileSummaryView.addLinkableLogLine(lines);
    logFileView.autoScrollEnabled = autoScrollCheckBox.checked;

    const clearButton = document.getElementById("clearButton");
    clearButton.onclick = () => {
        logFileView.clear();
        logFileSummaryView.clear();
    };

    // LogFileView loads the ace mode for hilighling the log asynchronously. If we
    // attach the websocket too early, then the messages can not be parsed since this relies
    // on the regexps in the ace mode. Therefore we must wait a bit here:
    waitFor(() => logFileView.isFullyLoaded(), () => {
        const serverConnectionDiv = document.getElementById('serverConnection');
        const wsUri = 'ws://' + window.location.host + window.location.pathname + '/stream';
        const webSocketClient = new WebSocketClient(wsUri);
        webSocketClient.addLogLineListener((logLines) => logFileView.addLogLines(logLines));
        webSocketClient.connectionStateListener = (connected) => {
            serverConnectionDiv.style.display = connected ? 'none' : 'block';
            if (connected) {
                logFileView.clear();
                logFileSummaryView.clear();
            }
        };
    });

});


function waitFor(conditionFct, toRunFct) {
    if (conditionFct()) {
        toRunFct();
    }
    else {
        setTimeout(() => {
            waitFor(conditionFct, toRunFct);
        }, 100);
    }
}

