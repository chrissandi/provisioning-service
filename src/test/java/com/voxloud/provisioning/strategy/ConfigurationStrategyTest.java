package com.voxloud.provisioning.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceConfiguration;
import com.voxloud.provisioning.exception.ProvisioningException;
import com.voxloud.provisioning.service.strategy.ConferenceConfigurationStrategy;
import com.voxloud.provisioning.service.strategy.DeskConfigurationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationStrategyTest {

    private DeskConfigurationStrategy deskStrategy;
    private ConferenceConfigurationStrategy conferenceStrategy;
    private Device device;
    private DeviceConfiguration baseConfig;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        deskStrategy = new DeskConfigurationStrategy();
        conferenceStrategy = new ConferenceConfigurationStrategy();
        objectMapper = new ObjectMapper();

        device = new Device();
        device.setMacAddress("test-mac");
        device.setModel(Device.DeviceModel.DESK);
        device.setUsername("test-user");
        device.setPassword("test-pass");

        baseConfig = DeviceConfiguration.builder()
                .username("test-user")
                .password("test-pass")
                .domain("sip.test.com")
                .port("5060")
                .codecs("G711,G729,OPUS")
                .build();
    }

    @Test
    public void testDeskStrategy_WithoutOverride() throws Exception {
        String config = deskStrategy.generateConfiguration(device, baseConfig);

        assertTrue(config.contains("username=test-user"));
        assertTrue(config.contains("password=test-pass"));
        assertTrue(config.contains("domain=sip.test.com"));
        assertTrue(config.contains("port=5060"));
        assertTrue(config.contains("codecs=G711,G729,OPUS"));
    }

    @Test
    public void testDeskStrategy_WithAdditionalProperties() throws Exception {
        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("timeout", 10);
        additionalProps.put("feature", "test-feature");

        baseConfig = DeviceConfiguration.builder()
                .username("test-user")
                .password("test-pass")
                .domain("sip.test.com")
                .port("5060")
                .codecs("G711,G729,OPUS")
                .additionalProperties(additionalProps)
                .build();

        String config = deskStrategy.generateConfiguration(device, baseConfig);

        assertTrue(config.contains("username=test-user"));
        assertTrue(config.contains("password=test-pass"));
        assertTrue(config.contains("domain=sip.test.com"));
        assertTrue(config.contains("port=5060"));
        assertTrue(config.contains("codecs=G711,G729,OPUS"));
        assertTrue(config.contains("timeout=10"));
        assertTrue(config.contains("feature=test-feature"));
    }

    @Test
    public void testDeskStrategy_WithOverride() throws Exception {
        String overrideFragment = "domain=sip.override.com\nport=5555\ntimeout=20";

        String config = deskStrategy.generateConfigurationWithOverride(device, baseConfig, overrideFragment);

        assertTrue(config.contains("username=test-user"));
        assertTrue(config.contains("password=test-pass"));
        assertTrue(config.contains("domain=sip.override.com"));
        assertTrue(config.contains("port=5555"));
        assertTrue(config.contains("codecs=G711,G729,OPUS"));
        assertTrue(config.contains("timeout=20"));
    }

    @Test
    public void testConferenceStrategy_WithoutOverride() throws Exception {
        String jsonConfig = conferenceStrategy.generateConfiguration(device, baseConfig);
        JsonNode rootNode = objectMapper.readTree(jsonConfig);

        assertEquals("test-user", rootNode.get("username").asText());
        assertEquals("test-pass", rootNode.get("password").asText());
        assertEquals("sip.test.com", rootNode.get("domain").asText());
        assertEquals("5060", rootNode.get("port").asText());

        JsonNode codecsNode = rootNode.get("codecs");
        assertTrue(codecsNode.isArray());
        assertEquals(3, codecsNode.size());
        assertEquals("G711", codecsNode.get(0).asText());
        assertEquals("G729", codecsNode.get(1).asText());
        assertEquals("OPUS", codecsNode.get(2).asText());
    }

    @Test
    public void testConferenceStrategy_WithAdditionalProperties() throws Exception {
        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("timeout", 10);
        additionalProps.put("feature", "test-feature");

        baseConfig = DeviceConfiguration.builder()
                .username("test-user")
                .password("test-pass")
                .domain("sip.test.com")
                .port("5060")
                .codecs("G711,G729,OPUS")
                .additionalProperties(additionalProps)
                .build();

        String jsonConfig = conferenceStrategy.generateConfiguration(device, baseConfig);
        JsonNode rootNode = objectMapper.readTree(jsonConfig);

        assertEquals("test-user", rootNode.get("username").asText());
        assertEquals("test-pass", rootNode.get("password").asText());
        assertEquals("sip.test.com", rootNode.get("domain").asText());
        assertEquals("5060", rootNode.get("port").asText());
        assertEquals(10, rootNode.get("timeout").asInt());
        assertEquals("test-feature", rootNode.get("feature").asText());
    }

    @Test
    public void testConferenceStrategy_WithOverride() throws Exception {
        String overrideFragment = "{\"domain\":\"sip.override.com\",\"port\":\"5555\",\"timeout\":20}";

        String jsonConfig = conferenceStrategy.generateConfigurationWithOverride(device, baseConfig, overrideFragment);
        JsonNode rootNode = objectMapper.readTree(jsonConfig);

        assertEquals("test-user", rootNode.get("username").asText());
        assertEquals("test-pass", rootNode.get("password").asText());
        assertEquals("sip.override.com", rootNode.get("domain").asText());
        assertEquals("5555", rootNode.get("port").asText());
        assertEquals(20, rootNode.get("timeout").asInt());
    }

    @Test
    public void testConferenceStrategy_InvalidOverride() {
        String invalidOverride = "invalid:json:format";

        assertThrows(ProvisioningException.class, () -> {
            conferenceStrategy.generateConfigurationWithOverride(device, baseConfig, invalidOverride);
        });
    }
}