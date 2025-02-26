package com.hackmatters.mule.vault.provider.api.connection.parameters;

import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.extension.api.annotation.param.ExclusiveOptionals;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

/**
 * Grouping of properties used for TLS authentication.
 * Only one of the two parameters is needed
 */
@ExclusiveOptionals(isOneRequired = true)
public class TLSAuthProperties {

    @DisplayName("JKS Properties")
    @Parameter
    @Optional
    private JKSProperties jksProperties;

    @DisplayName("PEM Properties")
    @Parameter
    @Optional
    private PEMProperties pemProperties;

    public TLSAuthProperties() {
        super();
    }

    public TLSAuthProperties(ConfigurationParameters parameters) {
        super();
        jksProperties = new JKSProperties(parameters);
        pemProperties = new PEMProperties(parameters);
    }

    public JKSProperties getJksProperties() {
        return jksProperties;
    }

    public void setJksProperties(JKSProperties jksProperties) {
        this.jksProperties = jksProperties;
    }

    public PEMProperties getPemProperties() {
        return pemProperties;
    }

    public void setPemProperties(PEMProperties pemProperties) {
        this.pemProperties = pemProperties;
    }
}

