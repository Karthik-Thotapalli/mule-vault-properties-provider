package com.hackmatters.mule.vault.provider;

import com.hackmatters.mule.vault.util.AwsCheck;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.junit.Assume.assumeTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class VaultIamAuthenticationTestCase extends MuleArtifactFunctionalTestCase {

    @BeforeClass
    public static void runCheckBeforeTest() {
        assumeTrue(AwsCheck.isExecutingOnAws());
    }

    @Override
    protected String getConfigFile() {
        return "mule_config/test-mule-iam-auth-config.xml";
    }

    @Test
    public void testVaultIAMAuthentication() throws Exception {
        assumeTrue(AwsCheck.isExecutingOnAws());
        String payloadValue = ((String) flowRunner("testFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue, is("test_value1"));
    }

}
