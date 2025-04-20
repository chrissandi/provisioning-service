package com.voxloud.provisioning.service.strategy;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceConfiguration;
import com.voxloud.provisioning.exception.ProvisioningException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class DeskConfigurationStrategy implements ConfigurationStrategy {

    @Override
    public String generateConfiguration(Device device, DeviceConfiguration config) {
        StringBuilder configBuilder = new StringBuilder();

        configBuilder.append("username=").append(config.getUsername()).append("\n");
        configBuilder.append("password=").append(config.getPassword()).append("\n");
        configBuilder.append("domain=").append(config.getDomain()).append("\n");
        configBuilder.append("port=").append(config.getPort()).append("\n");
        configBuilder.append("codecs=").append(config.getCodecs()).append("\n");

        if (config.getAdditionalProperties() != null) {
            for (Map.Entry<String, Object> entry : config.getAdditionalProperties().entrySet()) {
                configBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
        }

        return configBuilder.toString();
    }

    @Override
    public String generateConfigurationWithOverride(Device device, DeviceConfiguration config, String overrideFragment) throws ProvisioningException {
        Properties overrideProps = new Properties();
        try {
            overrideProps.load(new StringReader(overrideFragment));
        } catch (IOException e) {
            throw new ProvisioningException("Failed to parse override fragment: " + e.getMessage());
        }

        // Apply overrides to config
        for (String key : overrideProps.stringPropertyNames()) {
            String value = overrideProps.getProperty(key);

            switch (key) {
                case "domain":
                    config.setDomain(value);
                    break;
                case "port":
                    config.setPort(value);
                    break;
                default:
                    if (config.getAdditionalProperties() == null) {
                        config = DeviceConfiguration.builder()
                                .username(config.getUsername())
                                .password(config.getPassword())
                                .domain(config.getDomain())
                                .port(config.getPort())
                                .codecs(config.getCodecs())
                                .build();
                        config.setAdditionalProperties(new HashMap<>());
                        config.getAdditionalProperties().put(key, value);
                    } else {
                        config.getAdditionalProperties().put(key, value);
                    }
            }
        }

        return generateConfiguration(device, config);
    }
}