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
    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    @PreUpdate
    public void prePersist() {
        if (this.createAt != null) {
            this.updateAt = LocalDateTime.now(); // Definir o updateAt somente durante a atualização
        }
    }
}
