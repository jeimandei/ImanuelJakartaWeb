package com.jeimandei.imanuelbytes.user.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "permission_categories")
public class PermissionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    protected PermissionCategory() {}

    public PermissionCategory(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissionCategory c)) return false;
        return Objects.equals(name, c.name);
    }

    @Override
    public int hashCode() { return Objects.hash(name); }
}
