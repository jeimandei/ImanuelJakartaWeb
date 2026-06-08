package com.jeimandei.imanuelbytes.common.dto;

/**
 * Shared DTO used by other microservices to submit audit-log entries to the
 * {@code church-audit-service}.
 *
 * <p>Only {@code action} is strictly required; all other fields are optional.
 * Services should supply as much context as they have available so that the
 * audit trail is useful for debugging and compliance review.</p>
 */
public class AuditLogRequest {

    private String actor;
    private String actorRole;
    private String action;
    private String entityType;
    private String entityId;
    private String entityName;
    private String serviceName;
    private String ipAddress;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** No-arg constructor required for JSON deserialisation. */
    public AuditLogRequest() {
    }

    /**
     * All-args constructor for convenient inline construction by caller services.
     *
     * @param actor       username or identifier of the user who performed the action
     * @param actorRole   role of the actor at the time of the action (e.g. "ADMIN")
     * @param action      short description of the action (e.g. "USER_CREATED")
     * @param entityType  type of entity affected (e.g. "User", "Event")
     * @param entityId    primary key or identifier of the affected entity
     * @param entityName  human-readable name of the affected entity
     * @param serviceName name of the service that generated this entry
     * @param ipAddress   IP address of the originating request (IPv4 or IPv6)
     */
    public AuditLogRequest(String actor,
                           String actorRole,
                           String action,
                           String entityType,
                           String entityId,
                           String entityName,
                           String serviceName,
                           String ipAddress) {
        this.actor = actor;
        this.actorRole = actorRole;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityName = entityName;
        this.serviceName = serviceName;
        this.ipAddress = ipAddress;
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
