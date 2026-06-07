package com.jeimandei.imanuelbytes.interaction.dto;

import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import jakarta.validation.constraints.NotNull;

public class UpdatePrayerRequestStatusRequest {

    @NotNull
    private RequestStatus status;

    public UpdatePrayerRequestStatusRequest() {
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
