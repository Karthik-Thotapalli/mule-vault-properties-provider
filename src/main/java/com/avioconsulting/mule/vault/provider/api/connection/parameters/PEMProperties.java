package com.hackmatters.mule.vault.provider.api.connection.parameters;

import com.hackmatters.mule.vault.provider.api.VaultPropertiesProviderExtension;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Path;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.mule.runtime.api.component.ComponentIdentifier.builder;

/**
 * Properties used for TLS authentication via PEM files
 */
public class PEMProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(PEMProperties.class);

    private static final String PEM_PARAMETER_GROUP = "pem-properties";

    @DisplayName("Client PEM File")
    @Summary("An X.509 client certificate, for use with Vault's TLS Certificate auth backend")
    @Path
    @Parameter
    private String clientPemFile;

    @DisplayName("Client Key PEM File")
    @Summary("An RSA private key, for use with Vault's TLS Certificate auth backend")
    @Path
    @Parameter
    private String clientKeyPemFile;

    public PEMProperties() {
        super();
    }

    public PEMProperties(ConfigurationParameters parameters) {
        super();

        List<ConfigurationParameters> pemList = parameters
                .getComplexConfigurationParameter(builder()
                        .namespace(VaultPropertiesProviderExtension.EXTENSION_NAMESPACE)
                        .name(PEM_PARAMETER_GROUP).build());

        if (pemList.size() > 0) {
            ConfigurationParameters pemParameters = pemList.get(0);

            try {
                clientPemFile = pemParameters.getStringParameter("clientPemFile");
            } catch (Exception e) {
                LOGGER.error("keyStoreFile property is not set");
            }

            try {
                clientKeyPemFile = pemParameters.getStringParameter("clientKeyPemFile");
            } catch (Exception e) {
                LOGGER.error("keyStorePassword property is not set");
            }
        }
    }

    public String getClientPemFile() {
        return clientPemFile;
    }

    public void setClientPemFile(String clientPemFile) {
        this.clientPemFile = clientPemFile;
    }

    public String getClientKeyPemFile() {
        return clientKeyPemFile;
    }

    public void setClientKeyPemFile(String clientKeyPemFile) {
        this.clientKeyPemFile = clientKeyPemFile;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("clientPemFile: ");
        sb.append(clientPemFile);
        sb.append(", clientKeyPemFile: ");
        sb.append(clientKeyPemFile);
        return sb.toString();
    }
}