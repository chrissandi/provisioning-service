package com.voxloud.provisioning.service.impl;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceConfiguration;
import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.exception.ProvisioningException;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.service.ProvisioningService;
import com.voxloud.provisioning.service.factory.ConfigurationStrategyFactory;
import com.voxloud.provisioning.service.strategy.ConfigurationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProvisioningServiceImpl implements ProvisioningService {
    private final DeviceRepository deviceRepository;
    private final ConfigurationStrategyFactory strategyFactory;

    @Value("${provisioning.domain}")
    private String domain;

    @Value("${provisioning.port}")
    private String port;

    @Value("${provisioning.codecs}")
    private String codecs;

    @Autowired
    public ProvisioningServiceImpl(DeviceRepository deviceRepository, ConfigurationStrategyFactory strategyFactory) {
        this.deviceRepository = deviceRepository;
        this.strategyFactory = strategyFactory;
    }

    @Override
    public String getProvisioningFile(String macAddress) throws DeviceNotFoundException, ProvisioningException {
        log.info("Fetching configuration for device with MAC: {}", macAddress);

        // Find device in repository
        Device device = deviceRepository.findByMacAddress(macAddress)
                .orElseThrow(() -> new DeviceNotFoundException("Device with MAC address " + macAddress + " not found"));

        // Create base configuration from app properties and device data
        DeviceConfiguration baseConfig = DeviceConfiguration.builder()
                .username(device.getUsername())
                .password(device.getPassword())
                .domain(domain)
                .port(port)
                .codecs(codecs)
                .build();

        // Get appropriate strategy for device type
        ConfigurationStrategy strategy = strategyFactory.getStrategy(device.getModel());

        // Generate configuration (with or without override)
        if (StringUtils.isNotBlank(device.getOverrideFragment())) {
            log.info("Applying override fragment for device: {}", macAddress);
            return strategy.generateConfigurationWithOverride(device, baseConfig, device.getOverrideFragment());
        } else {
            return strategy.generateConfiguration(device, baseConfig);
        }
    }
}
