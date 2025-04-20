package com.voxloud.provisioning.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceConfiguration {
    private String username;
    private String password;
    private String domain;
    private String port;
    private String codecs;
    private Map<String, Object> additionalProperties;
}