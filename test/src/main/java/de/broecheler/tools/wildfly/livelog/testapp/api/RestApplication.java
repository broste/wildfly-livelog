package de.broecheler.tools.wildfly.livelog.testapp.api;


import javax.ws.rs.ApplicationPath;

/**
 * REST API for selenium tests to generate log entries
 */
@ApplicationPath("/api")
public class RestApplication extends javax.ws.rs.core.Application {
}
