package com.voxloud.provisioning.integration;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProvisioningIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceRepository deviceRepository;

    @BeforeEach
    public void setup() {
        deviceRepository.deleteAll();

        // Create test devices
        Device deskDevice = new Device();
        deskDevice.setMacAddress("test-desk-1");
        deskDevice.setModel(Device.DeviceModel.DESK);
        deskDevice.setUsername("test-user");
        deskDevice.setPassword("test-pass");
        deviceRepository.save(deskDevice);

        Device deskDeviceWithOverride = new Device();
        deskDeviceWithOverride.setMacAddress("test-desk-2");
        deskDeviceWithOverride.setModel(Device.DeviceModel.DESK);
        deskDeviceWithOverride.setUsername("test-user-2");
        deskDeviceWithOverride.setPassword("test-pass-2");
        deskDeviceWithOverride.setOverrideFragment(
                "domain=sip.test.com\n" +
                        "port=5555\n" +
                        "timeout=20"
        );
        deviceRepository.save(deskDeviceWithOverride);

        Device conferenceDevice = new Device();
        conferenceDevice.setMacAddress("test-conf-1");
        conferenceDevice.setModel(Device.DeviceModel.CONFERENCE);
        conferenceDevice.setUsername("conf-user");
        conferenceDevice.setPassword("conf-pass");
        deviceRepository.save(conferenceDevice);
    }

    @Test
    public void testGetDeskDeviceConfig() throws Exception {
        mockMvc.perform(get("/api/v1/provisioning/test-desk-1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("username=test-user")))
                .andExpect(content().string(containsString("password=test-pass")))
                .andExpect(content().string(containsString("domain=sip.voxloud.com")))
                .andExpect(content().string(containsString("port=5060")))
                .andExpect(content().string(containsString("codecs=G711,G729,OPUS")));
    }

    @Test
    public void testGetDeskDeviceConfigWithOverride() throws Exception {
        mockMvc.perform(get("/api/v1/provisioning/test-desk-2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("username=test-user-2")))
                .andExpect(content().string(containsString("password=test-pass-2")))
                .andExpect(content().string(containsString("domain=sip.test.com")))
                .andExpect(content().string(containsString("port=5555")))
                .andExpect(content().string(containsString("timeout=20")))
                .andExpect(content().string(containsString("codecs=G711,G729,OPUS")));
    }

    @Test
    public void testGetConferenceDeviceConfig() throws Exception {
        mockMvc.perform(get("/api/v1/provisioning/test-conf-1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"username\" : \"conf-user\"")))
                .andExpect(content().string(containsString("\"password\" : \"conf-pass\"")))
                .andExpect(content().string(containsString("\"domain\" : \"sip.voxloud.com\"")))
                .andExpect(content().string(containsString("\"port\" : \"5060\"")))
                .andExpect(content().string(containsString("\"codecs\" : [ \"G711\", \"G729\", \"OPUS\" ]")));
    }

    @Test
    public void testDeviceNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/provisioning/non-existent"))
                .andExpect(status().isNotFound());
    }
}