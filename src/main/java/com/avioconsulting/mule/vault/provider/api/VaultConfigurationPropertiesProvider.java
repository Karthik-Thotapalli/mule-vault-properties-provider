package com.hackmatters.mule.vault.provider.api;

import com.hackmatters.mule.vault.provider.api.exception.EmptyEnvironmentVariableException;
import com.hackmatters.mule.vault.provider.api.exception.SecretNotFoundException;
import com.hackmatters.mule.vault.provider.api.exception.UnknownVaultException;
import com.hackmatters.mule.vault.provider.api.exception.VaultAccessException;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provider to read Vault properties from the Vault server.
 */
public class VaultConfigurationPropertiesProvider implements ConfigurationPropertiesProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(VaultConfigurationPropertiesProvider.class);

    private final static String VAULT_PROPERTIES_PREFIX = "vault::";
    private final static Pattern VAULT_PATTERN = Pattern.compile(VAULT_PROPERTIES_PREFIX + "([^.}]*).(.*)");
    private final static Pattern ENV_PATTERN = Pattern.compile("\\$\\[([^\\]]*)\\]");

    private final Vault vault;

    Map<String, Map<String,String>> cachedData;

    /**
     * Constructs a VaultConfigurationPropertiesProvider. Vault must not be null.
     * @param vault
     */
    public VaultConfigurationPropertiesProvider(final Vault vault) {
        this.vault = vault;
        cachedData = new HashMap<>();
    }

    /**
     * Retrieves the property value from Vault. It stores the retrieved path in a Map so a Dynamic Secrets can be used.
     *
     * @param path     the path to the secret
     * @param property the property to retrieve from the secret
     * @return         the value of the property or null if the property is not found
     */
    private String getProperty(String path, String property) throws SecretNotFoundException, VaultAccessException, UnknownVaultException {



        try {
            Map<String, String> data = null;
            if (cachedData.containsKey(path)) {
                LOGGER.trace("Getting data from cache");
                data = cachedData.get(path);
            } else {
                LOGGER.trace("Getting data from Vault");
                data = vault.logical().read(path).getData();
                cachedData.put(path, data);
            }

            if (data != null) {
                return data.get(property);
            }

        } catch (VaultException ve) {
            if (ve.getHttpStatusCode() == 404) {
                LOGGER.error("Error getting data from Vault, secret not found", ve);
                throw new SecretNotFoundException("The secret at " + path + " was not found", ve);
            } else if (ve.getHttpStatusCode() == 403) {
                LOGGER.error("Error getting data from Vault, access denied", ve);
                throw new VaultAccessException("Access to the secret at " + path + " is denied", ve);
            } else {
                LOGGER.error("Error getting data from Vault", ve);
                throw new UnknownVaultException("Unknown Vault exception", ve);
            }

        }

        return null;
    }

    /**
     * Get a configuration property value from Vault.
     *
     * @param configurationAttributeKey  the key to lookup
     * @return                           the String value of the property
     */
    @Override
    public Optional<ConfigurationProperty> getConfigurationProperty(String configurationAttributeKey) {

        if (configurationAttributeKey.startsWith(VAULT_PROPERTIES_PREFIX)) {
            Matcher matcher = VAULT_PATTERN.matcher(configurationAttributeKey);
            if (matcher.find()) {

                final String effectiveKey = configurationAttributeKey.substring(VAULT_PROPERTIES_PREFIX.length());

                // The Vault path is everything after the prefix and before the first period
                final String vaultPath = matcher.group(1);

                // The secret key is everything after the first period
                final String secretKey = matcher.group(2);

                try {
                    final String value = getProperty(expandedValue(vaultPath), expandedValue(secretKey));

                    if (value != null) {
                        return Optional.of(new ConfigurationProperty() {

                            @Override
                            public Object getSource() {
                                return "Vault provider source";
                            }

                            @Override
                            public Object getRawValue() {
                                return value;
                            }

                            @Override
                            public String getKey() {
                                return effectiveKey;
                            }
                        });
                    }
                } catch (Exception e) {
                    return Optional.empty();
                }

                return Optional.empty();

            }
        }
        return Optional.empty();
    }

    @Override
    public String getDescription() {
        return "Vault properties provider";
    }

    /**
     * Retrieve values from the environment when the pattern \$\[[^\]]*\] is used in a property value and replace the pattern
     * with the value. Example matches: $[ENV] or $[environment]
     *
     * @param value the text to search for the pattern and replace with values
     * @return the inserted text with environment variables looked up
     * @throws EmptyEnvironmentVariableException when the environment variable is not set
     */
    private String expandedValue(final String value) throws EmptyEnvironmentVariableException {
        String result = value;
        Matcher envMatcher = ENV_PATTERN.matcher(value);
        while (envMatcher.find()) {
            String envVariableName = envMatcher.group(1);
            String envValue = System.getenv(envVariableName);

            if (envValue == null) {
                envValue = System.getProperty(envVariableName);
                LOGGER.debug("Retrieved environment value from property rather than environment");
            }

            if (envValue != null) {
                result = result.replaceAll("\\$\\[" + envVariableName + "\\]", envValue);
            } else {
                throw new EmptyEnvironmentVariableException("Environment variable [" + envVariableName + "] is not set");
            }
        }

        return result;
    }
}
