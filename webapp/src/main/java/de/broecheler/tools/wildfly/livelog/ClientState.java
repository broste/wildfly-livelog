package de.broecheler.tools.wildfly.livelog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;


@Stateful
public class ClientState {
    static final Logger LOG = LoggerFactory.getLogger(ClientState.class);

    @Resource
    private ManagedScheduledExecutorService scheduler;

    private ScheduledFuture<?> scheduledLogFileTailerTask;
    private TextFileTailerTask textFileTailerTask;
    private Session webSocketSession;

    @TransactionAttribute(NOT_SUPPORTED)
    public void onOpen(Session session) {
        LOG.debug("onOpen REMOVE");
        webSocketSession = session;
        String serverLog = System.getProperty("jboss.server.log.dir") + "/server.log";
        LOG.info("Using logfile {}", serverLog);
        textFileTailerTask = new TextFileTailerTask(serverLog, session);
        scheduledLogFileTailerTask = scheduler.scheduleWithFixedDelay(textFileTailerTask, 0, 1000, TimeUnit.MILLISECONDS);
    }

    @Remove
    @TransactionAttribute(NOT_SUPPORTED)
    public void onClose() {
        LOG.debug("onClose REMOVE");
        scheduledLogFileTailerTask.cancel(true);
        textFileTailerTask.shutdown();
    }

    static class TextFileTailerTask implements Runnable, ITextFileTailerListener {

        private final TextFileTailer textFileTailer;
        private final Session webSocketSession;

        TextFileTailerTask(String file, Session webSocketSession) {
            textFileTailer = new TextFileTailer(file, this);
            this.webSocketSession = webSocketSession;
        }

        @Override
        public void run() {
            textFileTailer.checkFile();
        }


        @Override
        public void onNewTextAvailable(String newText) {
            sendText(newText);
        }

        void shutdown() {
            textFileTailer.shutdown();
        }

        private void sendText(String newText) {
            try {
                webSocketSession.getBasicRemote().sendText(newText);
            } catch (IOException e) {
                LOG.error("Failed to send text to client", e);
            }
        }

        @Override
        public void onFileReset() {
            // FIXME: Handle reset properly by sending a dedicated ‚mesage interpreted by the client.
            sendText("*** File resetted ***");
        }
    }
}