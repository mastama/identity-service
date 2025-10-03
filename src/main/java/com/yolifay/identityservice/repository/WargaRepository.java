package com.yolifay.identityservice.repository;

import com.yolifay.identityservice.entity.Warga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WargaRepository extends JpaRepository<Warga, UUID> {
    Optional<Warga> findByNik(String nik);
    Optional<Warga> findByPhoneNumber(String phoneNumber);

    @Query(value = """
        SELECT * FROM warga w
        WHERE (:q IS NULL OR lower(w.nama) LIKE lower(concat('%', :q, '%')) OR w.nik LIKE concat('%', :q, '%'))
        ORDER BY w.created_at DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Warga> search(String q, int limit, int offset);
}
