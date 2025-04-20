package com.voxloud.provisioning.service.factory;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.exception.ProvisioningException;
import com.voxloud.provisioning.service.strategy.ConferenceConfigurationStrategy;
import com.voxloud.provisioning.service.strategy.ConfigurationStrategy;
import com.voxloud.provisioning.service.strategy.DeskConfigurationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationStrategyFactory {
    private final DeskConfigurationStrategy deskStrategy;
    private final ConferenceConfigurationStrategy conferenceStrategy;

    @Autowired
    public ConfigurationStrategyFactory(
            DeskConfigurationStrategy deskStrategy,
            ConferenceConfigurationStrategy conferenceStrategy) {
        this.deskStrategy = deskStrategy;
        this.conferenceStrategy = conferenceStrategy;
    }

    public ConfigurationStrategy getStrategy(Device.DeviceModel deviceModel) throws ProvisioningException {
        switch (deviceModel) {
            case DESK:
                return deskStrategy;
            case CONFERENCE:
                return conferenceStrategy;
            default:
                throw new ProvisioningException("Unsupported device type: " + deviceModel);
        }
    }
}
