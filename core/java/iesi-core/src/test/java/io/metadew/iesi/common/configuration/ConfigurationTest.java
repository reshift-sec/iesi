package io.metadew.iesi.common.configuration;

import io.metadew.iesi.common.configuration.guard.GuardConfiguration;
import io.metadew.iesi.common.configuration.metadata.MetadataConfiguration;
import io.metadew.iesi.common.configuration.plugin.PluginConfiguration;
import io.metadew.iesi.common.crypto.FrameworkCrypto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ConfigurationTest {

    @Test
    void initTest() {
        Configuration configuration = Configuration.getInstance();
        io.metadew.iesi.common.configuration.framework.FrameworkConfiguration conf = io.metadew.iesi.common.configuration.framework.FrameworkConfiguration.getInstance();
        MetadataConfiguration metadataRepositoryConfiguration = MetadataConfiguration.getInstance();
        assertTrue(true);

        PluginConfiguration pluginConfiguration = PluginConfiguration.getInstance();
        GuardConfiguration guardConfiguration = GuardConfiguration.getInstance();
        FrameworkCrypto.getInstance();
        System.out.println(FrameworkCrypto.getInstance());

        assertTrue(true);
    }
}
