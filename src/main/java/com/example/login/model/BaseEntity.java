package com.example.login.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreUpdate;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/*
* Indica que esta classe não será uma entidade por si só, mas servirá como base para outras entidades.
* */
@Data
@MappedSuperclass
public abstract class BaseEntity {

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PreUpdate
    public void prePersist() {
        if (this.createdAt != null) {
            this.updatedAt = LocalDateTime.now(); // Definir o updatedAt somente durante a atualização
        }
    }
}
