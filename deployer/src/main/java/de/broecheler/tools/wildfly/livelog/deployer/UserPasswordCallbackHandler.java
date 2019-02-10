package de.broecheler.tools.wildfly.livelog.deployer;

import javax.security.auth.callback.*;
import javax.security.sasl.RealmCallback;
import java.util.Optional;
import java.util.function.Consumer;

class UserPasswordCallbackHandler implements CallbackHandler {

    private final Optional<String> username;
    private final Optional<String> password;

    UserPasswordCallbackHandler(Optional<String> username, Optional<String> password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            boolean callbackFound = tryHandle(callback, RealmCallback.class, this::handleRealmCallback) ||
                    tryHandle(callback, NameCallback.class, this::handleNameCallback) ||
                    tryHandle(callback, PasswordCallback.class, this::handlePasswordCallback);
            if (!callbackFound) {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    private <T extends Callback> boolean tryHandle(Callback callback, Class<T> callbackType, Consumer<T> handler) {
        if (callbackType.isAssignableFrom(callback.getClass())) {
            handler.accept((T) callback);
            return true;
        }
        return false;
    }

    private void handleRealmCallback(RealmCallback callback) {
        String defaultRealm = callback.getDefaultText();
        System.out.println("Logging into " + defaultRealm);
        callback.setText(defaultRealm);
    }

    private void handleNameCallback(NameCallback callback) {
        if (username.isPresent()) {
            System.out.println("User: " + username.get());
            callback.setName(username.get());
        } else {
            throw new DeployerException("No username provided for deploying the artifact");
        }
    }

    private void handlePasswordCallback(PasswordCallback callback) {
        if (password.isPresent()) {
            callback.setPassword(password.get().toCharArray());
        } else {
            throw new DeployerException("No password provided for deploying the artifact");
        }
    }
}
