package de.broecheler.tools.wildfly.livelog.testapp.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/log")
public class LogResource {

    private static final Logger LOG = LoggerFactory.getLogger(LogResource.class);

    @GET
    public String ping() {
        return "pong";
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public void createLog(LogEntry entry) {
        switch (entry.getLevel()) {
            case DEBUG:
                LOG.debug(entry.getMessage());
                break;
            case INFO:
                LOG.info(entry.getMessage());
                break;
            case WARN:
                LOG.warn(entry.getMessage());
                break;
            case ERROR:
                LOG.error(entry.getMessage());
                break;
        }
    }

}
