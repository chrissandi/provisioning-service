package com.voxloud.provisioning.service.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceConfiguration;
import com.voxloud.provisioning.exception.ProvisioningException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class ConferenceConfigurationStrategy implements ConfigurationStrategy {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateConfiguration(Device device, DeviceConfiguration config) throws ProvisioningException {
        try {
            ObjectNode rootNode = objectMapper.createObjectNode();

            rootNode.put("username", config.getUsername());
            rootNode.put("password", config.getPassword());
            rootNode.put("domain", config.getDomain());
            rootNode.put("port", config.getPort());

            // Handle codecs as array
            String[] codecsArray = config.getCodecs().split(",");
            ArrayNode codecsNode = rootNode.putArray("codecs");
            Arrays.stream(codecsArray).forEach(codecsNode::add);

            // Add additional properties
            if (config.getAdditionalProperties() != null) {
                for (Map.Entry<String, Object> entry : config.getAdditionalProperties().entrySet()) {
                    addJsonNode(rootNode, entry.getKey(), entry.getValue());
                }
            }

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        } catch (Exception e) {
            throw new ProvisioningException("Failed to generate JSON configuration: " + e.getMessage());
        }
    }

    @Override
    public String generateConfigurationWithOverride(Device device, DeviceConfiguration config, String overrideFragment) throws ProvisioningException {
        try {
            JsonNode overrideNode = objectMapper.readTree(overrideFragment);

            // Apply overrides to config
            Iterator<Map.Entry<String, JsonNode>> fields = overrideNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                JsonNode value = entry.getValue();

                switch (key) {
                    case "domain":
                        config.setDomain(value.asText());
                        break;
                    case "port":
                        config.setPort(value.asText());
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
                            config.getAdditionalProperties().put(key, parseJsonValue(value));
                        } else {
                            config.getAdditionalProperties().put(key, parseJsonValue(value));
                        }
                }
            }

            return generateConfiguration(device, config);
        } catch (IOException e) {
            throw new ProvisioningException("Failed to parse override fragment: " + e.getMessage());
        }
    }

    private Object parseJsonValue(JsonNode node) {
        if (node.isInt()) {
            return node.asInt();
        } else if (node.isLong()) {
            return node.asLong();
        } else if (node.isDouble()) {
            return node.asDouble();
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isArray()) {
            return node.toString();
        } else {
            return node.asText();
        }
    }

    private void addJsonNode(ObjectNode parentNode, String key, Object value) {
        if (value instanceof Integer) {
            parentNode.put(key, (Integer) value);
        } else if (value instanceof Long) {
            parentNode.put(key, (Long) value);
        } else if (value instanceof Double) {
            parentNode.put(key, (Double) value);
        } else if (value instanceof Boolean) {
            parentNode.put(key, (Boolean) value);
        } else {
            parentNode.put(key, String.valueOf(value));
        }
    }
}