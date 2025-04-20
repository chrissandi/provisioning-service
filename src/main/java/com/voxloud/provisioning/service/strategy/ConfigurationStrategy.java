package com.voxloud.provisioning.service.strategy;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceConfiguration;
import com.voxloud.provisioning.exception.ProvisioningException;

public interface ConfigurationStrategy {
    String generateConfiguration(Device device, DeviceConfiguration config) throws ProvisioningException;

    String generateConfigurationWithOverride(Device device, DeviceConfiguration config, String overrideFragment) throws ProvisioningException;
}
