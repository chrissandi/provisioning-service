package com.voxloud.provisioning.service;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.exception.ProvisioningException;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.service.factory.ConfigurationStrategyFactory;
import com.voxloud.provisioning.service.impl.ProvisioningServiceImpl;
import com.voxloud.provisioning.service.strategy.ConfigurationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProvisioningServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private ConfigurationStrategyFactory strategyFactory;

    @Mock
    private ConfigurationStrategy configurationStrategy;

    @InjectMocks
    private ProvisioningServiceImpl provisioningService;

    private Device deskDevice;
    private Device conferenceDevice;
    private Device deskDeviceWithOverride;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(provisioningService, "domain", "sip.voxloud.com");
        ReflectionTestUtils.setField(provisioningService, "port", "5060");
        ReflectionTestUtils.setField(provisioningService, "codecs", "G711,G729,OPUS");

        deskDevice = new Device();
        deskDevice.setMacAddress("aa-bb-cc-11-22-33");
        deskDevice.setModel(Device.DeviceModel.DESK);
        deskDevice.setUsername("john");
        deskDevice.setPassword("doe");

        deskDeviceWithOverride = new Device();
        deskDeviceWithOverride.setMacAddress("aa-bb-cc-11-22-44");
        deskDeviceWithOverride.setModel(Device.DeviceModel.DESK);
        deskDeviceWithOverride.setUsername("alice");
        deskDeviceWithOverride.setPassword("smith");
        deskDeviceWithOverride.setOverrideFragment(
                "domain=sip.anotherdomain.com\n" +
                        "port=5161\n" +
                        "timeout=10"
        );

        conferenceDevice = new Device();
        conferenceDevice.setMacAddress("aa-bb-cc-11-33-33");
        conferenceDevice.setModel(Device.DeviceModel.CONFERENCE);
        conferenceDevice.setUsername("conference");
        conferenceDevice.setPassword("room1");
    }

    @Test
    public void testgetProvisioningFile_DeviceNotFound() {
        when(deviceRepository.findByMacAddress("non-existent")).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> {
            provisioningService.getProvisioningFile("non-existent");
        });
    }

    @Test
    public void testgetProvisioningFile_WithoutOverride() throws Exception {
        when(deviceRepository.findByMacAddress("aa-bb-cc-11-22-33")).thenReturn(Optional.of(deskDevice));
        when(strategyFactory.getStrategy(Device.DeviceModel.DESK)).thenReturn(configurationStrategy);
        when(configurationStrategy.generateConfiguration(any(), any())).thenReturn("mockConfig");

        String result = provisioningService.getProvisioningFile("aa-bb-cc-11-22-33");

        assertEquals("mockConfig", result);
        verify(configurationStrategy, never()).generateConfigurationWithOverride(any(), any(), any());
    }

    @Test
    public void testgetProvisioningFile_WithOverride() throws Exception {
        when(deviceRepository.findByMacAddress("aa-bb-cc-11-22-44")).thenReturn(Optional.of(deskDeviceWithOverride));
        when(strategyFactory.getStrategy(Device.DeviceModel.DESK)).thenReturn(configurationStrategy);
        when(configurationStrategy.generateConfigurationWithOverride(any(), any(), any())).thenReturn("mockOverrideConfig");

        String result = provisioningService.getProvisioningFile("aa-bb-cc-11-22-44");

        assertEquals("mockOverrideConfig", result);
        verify(configurationStrategy, never()).generateConfiguration(any(), any());
    }

    @Test
    public void testgetProvisioningFile_StrategyThrowsException() throws Exception {
        when(deviceRepository.findByMacAddress("aa-bb-cc-11-22-33")).thenReturn(Optional.of(deskDevice));
        when(strategyFactory.getStrategy(Device.DeviceModel.DESK)).thenReturn(configurationStrategy);
        when(configurationStrategy.generateConfiguration(any(), any())).thenThrow(new ProvisioningException("Test error"));

        assertThrows(ProvisioningException.class, () -> {
            provisioningService.getProvisioningFile("aa-bb-cc-11-22-33");
        });
    }
}