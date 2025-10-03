package com.yolifay.identityservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.Instant;
import java.util.UUID;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
@Entity @Table(name = "warga")
public class Warga {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(length = 16, unique = true, nullable = false)
    private String nik;

    @Column(nullable = false)
    private String nama;

    @Column(length = 15, unique = true, nullable = false)
    private String phoneNumber;

    private String alamat;
    private Integer rt;
    private Integer rw;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
