package com.voxloud.provisioning.service;

import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.exception.ProvisioningException;

public interface ProvisioningService {

    String getProvisioningFile(String macAddress) throws DeviceNotFoundException, ProvisioningException;;
}
