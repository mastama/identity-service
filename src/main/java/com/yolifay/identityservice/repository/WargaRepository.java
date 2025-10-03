package com.yolifay.identityservice.repository;

import com.yolifay.identityservice.entity.Warga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WargaRepository extends JpaRepository<Warga, UUID> {
    Optional<Warga> findByNik(String nik);
    Optional<Warga> findByPhoneNumber(String phoneNumber);

    Page<Warga> findAll(Specification spec, Pageable pageable);

}
