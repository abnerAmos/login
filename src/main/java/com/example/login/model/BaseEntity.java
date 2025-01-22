package com.example.login.model;

import com.example.login.aspect.view.Views;
import com.fasterxml.jackson.annotation.JsonView;
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
    @JsonView(Views.Complete.class)
    private LocalDateTime createdAt;

    @JsonView(Views.Complete.class)
    private LocalDateTime updatedAt;

    @PreUpdate
    public void prePersist() {
        if (this.createdAt != null) {
            this.updatedAt = LocalDateTime.now(); // Definir o updatedAt somente durante a atualização
        }
    }
}
