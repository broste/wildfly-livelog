package de.broecheler.tools.wildfly.livelog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/stream")
public class WebSocketServer {

    static final Logger LOG = LoggerFactory.getLogger(ClientState.class);

    @Inject
    ClientState clientState;

    @OnMessage
    public String onMessage(String message) {
        LOG.debug("Received : {}", message);
        return "";
    }

    @OnOpen
    public void onOpen(Session session) {
        LOG.debug("WebSocket opened: {}", session.getId());
        clientState.onOpen(session);
    }

    @OnClose
    public void onClose(CloseReason reason) {
        LOG.debug("Closing a WebSocket due to {}", reason);
        clientState.onClose();
    }


}
