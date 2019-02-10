package de.broecheler.tools.wildfly.livelog.testapp.api;

public class LogEntry {

    private LogLevel level;
    private String message;

    public LogEntry(LogLevel level, String message) {
        this.level = level;
        this.message = message;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
