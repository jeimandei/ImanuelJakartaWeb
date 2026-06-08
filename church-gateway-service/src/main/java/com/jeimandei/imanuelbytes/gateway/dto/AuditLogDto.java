package com.jeimandei.imanuelbytes.gateway.dto;

public class AuditLogDto {

    private Long id;
    private String actor;
    private String actorRole;
    private String action;
    private String entityType;
    private String entityId;
    private String entityName;
    private String serviceName;
    private String ipAddress;
    private String createdAt;

    public AuditLogDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
