package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.exception.ProvisioningException;
import com.voxloud.provisioning.service.ProvisioningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProvisioningController.class)
public class ProvisioningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProvisioningService provisioningService;

    @Test
    public void testgetProvisioningFile_Success() throws Exception {
        String macAddress = "aa-bb-cc-11-22-33";
        String expectedConfig = "username=john\npassword=doe\ndomain=sip.voxloud.com\nport=5060\ncodecs=G711,G729,OPUS";

        when(provisioningService.getProvisioningFile(macAddress)).thenReturn(expectedConfig);

        mockMvc.perform(get("/api/v1/provisioning/" + macAddress))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedConfig));
    }

    @Test
    public void testgetProvisioningFile_DeviceNotFound() throws Exception {
        String macAddress = "non-existent";

        when(provisioningService.getProvisioningFile(macAddress))
                .thenThrow(new DeviceNotFoundException("Device not found"));

        mockMvc.perform(get("/api/v1/provisioning/" + macAddress))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testgetProvisioningFile_ProvisioningError() throws Exception {
        String macAddress = "aa-bb-cc-11-22-33";

        when(provisioningService.getProvisioningFile(macAddress))
                .thenThrow(new ProvisioningException("Error during provisioning"));

        mockMvc.perform(get("/api/v1/provisioning/" + macAddress))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error during provisioning"));
    }
}