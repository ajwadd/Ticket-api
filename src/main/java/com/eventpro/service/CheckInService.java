package com.eventpro.service;

import com.eventpro.dto.request.CheckInRequest;
import com.eventpro.dto.response.CheckInResponse;

public interface CheckInService {

    CheckInResponse composterBillet(CheckInRequest request);
}
