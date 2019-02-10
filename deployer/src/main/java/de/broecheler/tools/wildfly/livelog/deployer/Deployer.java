package de.broecheler.tools.wildfly.livelog.deployer;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.xnio.Options.SASL_POLICY_NOANONYMOUS;

public class Deployer {

    private static final int WEB_APP_PORT = 8080;
    private static final String WAR_NAME = "livelog.war";
    private static final String DEPLOYMENT = "deployment";
    private static final String ADDRESS = "address";
    private static final String OPERATION = "operation";
    private static final String OUTCOME = "outcome";
    private static final String SUCCESS = "success";

    public static void main(String[] args) throws Exception {
        new Deployer().run(args);
    }

    private void run(String[] args) throws Exception {
        CommandLineArgs commandLineArgs = CommandLineArgs.create(args);
        if (commandLineArgs.isHelpRequested()) {
            commandLineArgs.printHelp();
        } else if (commandLineArgs.isExtractRequested()) {
            extractWar();
        } else if (deploy(commandLineArgs)) {
            openWebAppInBrowser(commandLineArgs.getHostname(), WEB_APP_PORT);
        }
    }

    private boolean deploy(CommandLineArgs commandLineArgs) throws IOException {
        System.out.println("Deploying to " + commandLineArgs.getProtocol() + "://" + commandLineArgs.getHostname() + ":" + commandLineArgs.getManagementPort());
        byte[] warBytes = getWarBytes();
        ModelNode response = requestDeployment(commandLineArgs, warBytes);
        System.out.println(response);
        return response.get(OUTCOME).asString().equals(SUCCESS);
    }

    private void extractWar() throws IOException {
        System.out.println("Extracting file " + WAR_NAME);
        try (FileOutputStream fos = new FileOutputStream(WAR_NAME)) {
            fos.write(getWarBytes());
        }
    }

    private ModelNode createRemoveRequest(String deploymentName) {
        ModelNode request = new ModelNode();
        request.get(ADDRESS).add(DEPLOYMENT, deploymentName);
        request.get(OPERATION).set("remove");
        return request;
    }

    private ModelNode createDeployRequest(String deploymentName, byte[] artifact) {
        ModelNode request = new ModelNode();
        request.get(ADDRESS).add(DEPLOYMENT, deploymentName);
        request.get(OPERATION).set("add");
        ModelNode content = request.get("content").get(0);
        content.get("bytes").set(artifact);
        request.get("enabled").set(true);
        return request;
    }

    private ModelNode requestDeployment(CommandLineArgs commandLineArgs, byte[] warBytes) throws IOException {

        Map<String, String> saslOptions = new HashMap<>();
        saslOptions.put(SASL_POLICY_NOANONYMOUS.getName(), Boolean.TRUE.toString());

        try (final ModelControllerClient client = ModelControllerClient.Factory.create(commandLineArgs.getProtocol(), commandLineArgs.getHostname(), commandLineArgs.getManagementPort(),
                new UserPasswordCallbackHandler(commandLineArgs.getUsername(), commandLineArgs.getPassword()), saslOptions)) {

            // "full-replace-deployment" does not work when the deployment does not already exist, therefore we need two steps
            ModelNode undeployRequest = createRemoveRequest(WAR_NAME);
            client.execute(undeployRequest);
            ModelNode deployRequest = createDeployRequest(WAR_NAME, warBytes);
            return client.execute(deployRequest);
        }
    }

    private byte[] getWarBytes() throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(); InputStream input = this.getClass().getClassLoader().getResourceAsStream(WAR_NAME)) {
            int nRead;
            byte[] data = new byte[100 * 1024];
            while ((nRead = input.read(data, 0, data.length)) != -1) {
                output.write(data, 0, nRead);
            }
            output.flush();
            return output.toByteArray();
        }
    }

    private void openWebAppInBrowser(String host, int port) throws IOException, URISyntaxException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI("http://" + host + ":" + port + "/livelog"));
        }
    }

}
