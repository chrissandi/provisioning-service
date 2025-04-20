package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.exception.ProvisioningException;
import com.voxloud.provisioning.service.ProvisioningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/provisioning")
@Slf4j
public class ProvisioningController {

    private final ProvisioningService provisioningService;

    @Autowired
    public ProvisioningController(ProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @GetMapping("/{macAddress}")
    public ResponseEntity<String> getDeviceConfiguration(@PathVariable String macAddress) {
        try {
            String configuration = provisioningService.getProvisioningFile(macAddress);
            return ResponseEntity.ok(configuration);
        } catch (DeviceNotFoundException e) {
            log.warn("Device not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (ProvisioningException e) {
            log.error("Error provisioning device: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}