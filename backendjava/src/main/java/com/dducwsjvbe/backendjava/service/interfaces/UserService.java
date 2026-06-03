package com.dducwsjvbe.backendjava.service.interfaces;

import com.dducwsjvbe.backendjava.dto.request.CreateAdminRequest;
import com.dducwsjvbe.backendjava.dto.response.ResponseData;

public interface UserService {
    ResponseData<?>createAdmin(CreateAdminRequest createAdminRequest);
}
