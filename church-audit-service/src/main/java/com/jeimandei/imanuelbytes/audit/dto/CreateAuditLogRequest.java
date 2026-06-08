package com.jeimandei.imanuelbytes.audit.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload used to record a new audit-log entry.
 *
 * <p>Only {@code action} is mandatory; all other fields are optional so that
 * callers only need to supply the context they have available.</p>
 */
public class CreateAuditLogRequest {

    private String actor;
    private String actorRole;

    @NotBlank(message = "action must not be blank")
    private String action;

    private String entityType;
    private String entityId;
    private String entityName;
    private String serviceName;
    private String ipAddress;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public CreateAuditLogRequest() {
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
