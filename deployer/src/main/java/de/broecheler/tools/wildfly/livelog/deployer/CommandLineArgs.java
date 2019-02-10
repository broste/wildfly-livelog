package de.broecheler.tools.wildfly.livelog.deployer;

import org.apache.commons.cli.*;

import java.util.Optional;

class CommandLineArgs {

    private static final String HELP = "help";
    private static final String PORT = "port";
    private static final String SERVER = "server";
    private static final String EXTRACT = "extract";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String PROTOCOL = "protocol";

    private static final String DEFAULT_PORT = "9990";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PROTOCOL = "http-remoting";

    private Options options;
    private CommandLine commandLine;

    private CommandLineArgs(Options options, CommandLine commandLine) {
        this.options = options;
        this.commandLine = commandLine;
    }

    boolean isHelpRequested() {
        return commandLine.hasOption(HELP);
    }

    boolean isExtractRequested() {
        return commandLine.hasOption(EXTRACT);
    }

    String getHostname() {
        return commandLine.getOptionValue(SERVER, DEFAULT_HOST);
    }

    int getManagementPort() {
        return Integer.parseInt(commandLine.getOptionValue(PORT, DEFAULT_PORT));
    }

    Optional<String> getUsername() {
        return Optional.ofNullable(commandLine.getOptionValue(USER));
    }

    Optional<String> getPassword() {
        return Optional.ofNullable(commandLine.getOptionValue(PASSWORD));
    }

    String getProtocol() {
        String protocol = commandLine.getOptionValue(PROTOCOL);
        return protocol != null ? protocol : DEFAULT_PROTOCOL;
    }

    void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar wildfly-livelog-deployer.jar", options);
    }

    static CommandLineArgs create(String[] args) throws ParseException {
        Options options = createCommandLineOptions();
        CommandLine commandLine = new DefaultParser().parse(options, args);
        return new CommandLineArgs(options, commandLine);
    }

    private static Options createCommandLineOptions() {
        return new Options()
                .addOption(Option.builder("h")
                        .longOpt(HELP)
                        .desc("Display this help")
                        .build())
                .addOption(Option.builder("s")
                        .longOpt(SERVER)
                        .hasArg()
                        .desc("hostname of the server to deploy to (default: " + DEFAULT_HOST + ")")
                        .build())
                .addOption(Option.builder("p")
                        .longOpt(PORT)
                        .desc("management port of the server to deploy to (default: " + DEFAULT_PORT + ")")
                        .hasArg()
                        .build())
                .addOption(Option.builder("t")
                        .longOpt(PROTOCOL)
                        .desc("protocol for connecting to the server to deploy to (default: " + DEFAULT_PROTOCOL + ")")
                        .hasArg()
                        .build())
                .addOption(Option.builder("u")
                        .longOpt(USER)
                        .desc("name of the management user for logging into jboss")
                        .hasArg()
                        .build())
                .addOption(Option.builder("c")
                        .longOpt(PASSWORD)
                        .desc("password of the management user for logging into jboss")
                        .hasArg()
                        .build())
                .addOption(Option.builder("x")
                        .longOpt(EXTRACT)
                        .desc("extract the WAR to current working directory instead of deploying it")
                        .build());
    }
}
