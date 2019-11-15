package com.nexmo.client.messaging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.nexmo.client.messaging.MessageSubmissionResponse;
import com.nexmo.client.HttpConfig;
import com.nexmo.client.HttpWrapper;
import com.nexmo.client.NexmoClientCreationException;
import com.nexmo.client.NexmoUnableToReadPrivateKeyException;
import com.nexmo.client.auth.AuthCollection;
import com.nexmo.client.auth.JWTAuthMethod;
import com.nexmo.client.auth.SignatureAuthMethod;
import com.nexmo.client.auth.TokenAuthMethod;
import com.nexmo.client.sms.messages.TextMessage;

import org.apache.http.client.HttpClient;


public class MessagingClient {

    private HttpWrapper httpWrapper;
    private SendMessageMethod send;

    public MessagingClient(Builder builder) {
        this.httpWrapper = new HttpWrapper(builder.httpConfig, builder.authCollection);
        this.httpWrapper.setHttpClient(builder.httpClient);
        this.send = new SendMessageMethod(this.httpWrapper);

    }

    public static Builder builder() {
        return new Builder();
    }

    public MessageSubmissionResponse submitMessage(TextMessage msg) {
        return this.send.execute(msg);
    }

    public static class Builder {
        private AuthCollection authCollection;
        private HttpConfig httpConfig = HttpConfig.defaultConfig();
        private HttpClient httpClient;
        private String applicationId;
        private String apiKey;
        private String apiSecret;
        private String signatureSecret;
        private byte[] privateKeyContents;

        /**
         * @param httpConfig Configuration options for the {@link HttpWrapper}
         *
         * @return The {@link Builder} to keep building.
         */
        public Builder httpConfig(HttpConfig httpConfig) {
            this.httpConfig = httpConfig;
            return this;
        }

        /**
         * @param httpClient Custom implementation of {@link HttpClient}.
         *
         * @return The {@link Builder} to keep building.
         */
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * When setting an applicationId, it is also expected that the {@link #privateKeyContents} will also be set.
         *
         * @param applicationId Used to identify each application.
         *
         * @return The {@link Builder} to keep building.
         */
        public Builder applicationId(String applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        /**
         * When setting an apiKey, it is also expected that {@link #apiSecret(String)} and/or {@link
         * #signatureSecret(String)} will also be set.
         *
         * @param apiKey The API Key found in the dashboard for your account.
         *
         * @return The {@link Builder} to keep building.
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * When setting an apiSecret, it is also expected that {@link #apiKey(String)} will also be set.
         *
         * @param apiSecret The API Secret found in the dashboard for your account.
         *
         * @return The {@link Builder} to keep building.
         */
        public Builder apiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
            return this;
        }

        /**
         * When setting a signatureSecret, it is also expected that {@link #apiKey(String)} will also be set.
         *
         * @param signatureSecret The Signature Secret found in the dashboard for your account.
         *
         * @return The {@link Builder} to keep building.
         */
        public Builder signatureSecret(String signatureSecret) {
            this.signatureSecret = signatureSecret;
            return this;
        }

        /**
         * When setting the contents of your private key, it is also expected that {@link #applicationId(String)} will
         * also be set.
         *
         * @param privateKeyContents The contents of your private key used for JWT generation.
         *
         * @return The {@link Builder} to keep building.
         */
        public Builder privateKeyContents(byte[] privateKeyContents) {
            this.privateKeyContents = privateKeyContents;
            return this;
        }

        /**
         * When setting the contents of your private key, it is also expected that {@link #applicationId(String)} will
         * also be set.
         *
         * @param privateKeyContents The contents of your private key used for JWT generation.
         *
         * @return The {@link Builder} to keep building.
         */
        public Builder privateKeyContents(String privateKeyContents) {
            return privateKeyContents(privateKeyContents.getBytes());
        }

        /**
         * When setting the path of your private key, it is also expected that {@link #applicationId(String)} will also
         * be set.
         *
         * @param privateKeyPath The path to your private key used for JWT generation.
         *
         * @return The {@link Builder} to keep building.
         *
         * @throws NexmoUnableToReadPrivateKeyException if the private key could not be read from the file system.
         */
        public Builder privateKeyPath(Path privateKeyPath) throws NexmoUnableToReadPrivateKeyException {
            try {
                return privateKeyContents(Files.readAllBytes(privateKeyPath));
            } catch (IOException e) {
                throw new NexmoUnableToReadPrivateKeyException("Unable to read private key at " + privateKeyPath, e);
            }
        }

        /**
         * When setting the path of your private key, it is also expected that {@link #applicationId(String)} will also
         * be set.
         *
         * @param privateKeyPath The path to your private key used for JWT generation.
         *
         * @return The {@link Builder} to keep building.
         *
         * @throws NexmoUnableToReadPrivateKeyException if the private key could not be read from the file system.
         */
        public Builder privateKeyPath(String privateKeyPath) throws NexmoUnableToReadPrivateKeyException {
            return privateKeyPath(Paths.get(privateKeyPath));
        }

        /**
         * @return a new {@link NexmoClient} from the stored builder options.
         *
         * @throws NexmoClientCreationException if credentials aren't provided in a valid pairing or there were issues
         *                                      generating an {@link JWTAuthMethod} with the provided credentials.
         */
        public MessagingClient build() {
            this.authCollection = generateAuthCollection(this.applicationId,
                    this.apiKey,
                    this.apiSecret,
                    this.signatureSecret,
                    this.privateKeyContents
            );
            return new MessagingClient(this);
        }

        private AuthCollection generateAuthCollection(String applicationId, String key, String secret, String signature, byte[] privateKeyContents) {
            AuthCollection authMethods = new AuthCollection();

            try {
                validateAuthParameters(applicationId, key, secret, signature, privateKeyContents);
            } catch (IllegalStateException e) {
                throw new NexmoClientCreationException("Failed to generate authentication methods.", e);
            }

            if (key != null && secret != null) {
                authMethods.add(new TokenAuthMethod(key, secret));
            }

            if (key != null && signature != null) {
                authMethods.add(new SignatureAuthMethod(key, signature));
            }

            if (applicationId != null && privateKeyContents != null) {
                authMethods.add(new JWTAuthMethod(applicationId, privateKeyContents));
            }

            return authMethods;
        }

        private void validateAuthParameters(String applicationId, String key, String secret, String signature, byte[] privateKeyContents) {
            if (key != null && secret == null && signature == null) {
                throw new IllegalStateException(
                        "You must provide an API secret or signature secret in addition to your API key.");
            }

            if (secret != null && key == null) {
                throw new IllegalStateException("You must provide an API key in addition to your API secret.");
            }

            if (signature != null && key == null) {
                throw new IllegalStateException("You must provide an API key in addition to your signature secret.");
            }

            if (applicationId == null && privateKeyContents != null) {
                throw new IllegalStateException("You must provide an application ID in addition to your private key.");
            }

            if (applicationId != null && privateKeyContents == null) {
                throw new IllegalStateException("You must provide a private key in addition to your application id.");
            }
        }
    }

}