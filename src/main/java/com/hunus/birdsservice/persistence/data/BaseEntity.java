package com.hunus.birdsservice.persistence.data;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @Column(name = "id", nullable = false)
    protected UUID id;

    @Column
    @EqualsAndHashCode.Exclude
    protected LocalDateTime created;

    @Column
    @EqualsAndHashCode.Exclude
    protected LocalDateTime modified;

    @PrePersist
    @PreUpdate
    private void onSave() {
        if (id == null) {
            id = UUID.randomUUID();
            created = LocalDateTime.now();
        }
        modified = LocalDateTime.now();
    }
}
