package com.dducwsjvbe.backendjava.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserType {
    @JsonProperty("system")
    SYSTEM,
    @JsonProperty("admin")
    ADMIN,
    @JsonProperty("user")
    USER
}
