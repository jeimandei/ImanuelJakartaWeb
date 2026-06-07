package com.jeimandei.imanuelbytes.interaction.dto;

import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateContactStatusRequest {

    @NotNull
    private RequestStatus status;

    public UpdateContactStatusRequest() {
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
