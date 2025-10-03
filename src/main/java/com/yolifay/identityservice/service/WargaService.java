package com.yolifay.identityservice.service;

import com.yolifay.identityservice.dto.WargaCreateRequest;
import com.yolifay.identityservice.dto.WargaResponse;
import com.yolifay.identityservice.dto.pagination.ListWargaRequest;
import com.yolifay.identityservice.dto.pagination.PageEnvelope;
import com.yolifay.identityservice.dto.pagination.SortMeta;
import com.yolifay.identityservice.entity.Warga;
import com.yolifay.identityservice.exception.ConflictException;
import com.yolifay.identityservice.exception.DataNotFoundException;
import com.yolifay.identityservice.repository.WargaRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WargaService {
    private final WargaRepository wargaRepository;
    private static final String PHONE_NUMBER = "phoneNumber";
    private static final String CREATED_AT = "createdAt";

    @Transactional
    public WargaResponse createWarga(WargaCreateRequest req) throws ConflictException {
        log.info("Start create warga: {}", req.nama());

        wargaRepository.findByNik(req.nik()).ifPresent(w -> {
            throw new ConflictException("NIK sudah terdaftar");
        });
        wargaRepository.findByPhoneNumber(req.phoneNumber()).ifPresent(w -> {
            throw new ConflictException("Nomor telepon sudah terdaftar");
        });

        Warga entity = Warga.builder()
                .nik(req.nik())
                .nama(req.nama())
                .phoneNumber(req.phoneNumber())
                .rt(req.rt())
                .rw(req.rw())
                .alamat(req.alamat())
                .build();

        Warga saved = wargaRepository.save(entity);

        log.info("End create warga: {}", saved.getNama());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageEnvelope<WargaResponse> getAllWarga(ListWargaRequest req) {
        log.info("Start get all warga");

        // 1. Normalize sort and direction
        String sortField = normalizeSortField(req.paging().sortField());
        Sort.Direction sortDir = normalizeSortDirection(req.paging().sortDirection());

        Pageable pageable = PageRequest.of(req.paging().pageIndex(), req.paging().perpage(), Sort.by(sortDir, sortField));

        // 2. Build Specification (WHERE)
        Specification<Warga> spec = ((root, query, criteriaBuilder) -> {
            List<Predicate> filters = new ArrayList<>();

            // 2a. Free text search (q)
            if (req.paging().q() != null && !req.paging().q().isBlank()) {
                String likePattern = "%" + req.paging().q().trim().toLowerCase() + "%";
                Predicate any = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nik")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nama")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get(PHONE_NUMBER)), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("alamat")), likePattern)
                );
                filters.add(any);
                log.info("[filter] q='{}' on [nik,nama,alamat,phoneNumber] (LOWER LIKE)", req.paging().q().trim());
            } else {
                log.info("[filter] no q");
            }

            // 2b. Filter by RT
            Integer rt = req.wargaFilter() != null ? req.wargaFilter().getRt() : null;
            if (rt != null && rt > 0) {
                Predicate byRt = criteriaBuilder.equal(root.get("rt"), rt);
                filters.add(byRt);
                log.info("[filter] rt={}", rt);
            } else {
                log.info("[filter] no rt");
            }

            // 2c. Filter by RW
            Integer rw = req.wargaFilter() != null ? req.wargaFilter().getRw() : null;
            if (rw != null && rw > 0) {
                Predicate byRw = criteriaBuilder.equal(root.get("rw"), rw);
                filters.add(byRw);
                log.info("[filter] rw={}", rw);
            } else {
                log.info("[filter] no rw");
            }

            // Combine all filters with AND
            if (filters.isEmpty()) {
                log.info("[filter.final] outcome=match_all reason=no_active_filters");
                return  criteriaBuilder.conjunction(); // match all
            } else if (filters.size() == 1) {
                log.info("[filter.final] outcome=single active_filters={}", filters.size());
                return filters.getFirst();
            } else {
                log.info("[filter.final] outcome=multiple active_filters={}", filters.size());
                return criteriaBuilder.and(filters.toArray(new Predicate[0]));
            }
        });

        // 3. Execute query (count & data)
        Page<Warga> pageData = wargaRepository.findAll(spec, pageable);
        log.info("[query] fetched contentCount={} totalElements={} totalPages={}",
                pageData.getNumberOfElements(), pageData.getTotalElements(), pageData.getTotalPages());

        // 4. Map Entity to response DTO
        List<WargaResponse> content = pageData.stream().map(this::mapToResponse).toList();

        // 5. Bungkus ke PageEnvelope + meta
        SortMeta sortMeta = new SortMeta(sortField, sortDir.name().toLowerCase());
        PageEnvelope<WargaResponse> resp = PageEnvelope.of(
                req.paging(),
                pageData.getTotalElements(),
                content,
                sortMeta
        );
        log.info("End get all warga");
        return resp;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "wargaByNik", key = "#nik")
    public WargaResponse getWargaByNik(String nik) throws DataNotFoundException {
        log.info("Start get warga by NIK: {}", nik);

        Warga w = wargaRepository.findByNik(nik)
                .orElseThrow(() -> new DataNotFoundException("Get Warga dengan NIK " + nik + " tidak dapat ditemukan"));

        log.info("End get warga by NIK: {}", nik);
        return mapToResponse(w);
    }

    @Transactional
    @CacheEvict(cacheNames = "wargaByNik", key = "#nik")
    public WargaResponse updateWarga(String nik, WargaCreateRequest req) throws DataNotFoundException, ConflictException {
        log.info("Start update warga with NIK: {}", nik);

        Warga existingWarga = wargaRepository.findByNik(nik)
                .orElseThrow(() -> new DataNotFoundException("Update Warga dengan NIK " + nik + " tidak ditemukan"));

        if (!existingWarga.getPhoneNumber().equals(req.phoneNumber())) {
            wargaRepository.findByPhoneNumber(req.phoneNumber()).ifPresent(w -> {
                throw new ConflictException("Nomor telepon sudah terdaftar");
            });
        }

        existingWarga.setNama(req.nama());
        existingWarga.setPhoneNumber(req.phoneNumber());
        existingWarga.setRt(req.rt());
        existingWarga.setRw(req.rw());
        existingWarga.setAlamat(req.alamat());

        Warga updated = wargaRepository.save(existingWarga);

        log.info("End update warga with NIK: {}", nik);
        return mapToResponse(updated);
    }

    @CacheEvict(cacheNames = "wargaByNik", key = "#nik")
    public void deleteWarga(String nik) throws DataNotFoundException {
        log.info("Start delete warga with NIK: {}", nik);

        Warga existingWarga = wargaRepository.findByNik(nik)
                .orElseThrow(() -> new DataNotFoundException("Delete Warga dengan NIK " + nik + " tidak ditemukan"));

        wargaRepository.delete(existingWarga);

        log.info("End delete warga with NIK: {}", nik);
        mapToResponse(existingWarga);
    }

    private WargaResponse mapToResponse(Warga w) {
        return WargaResponse.builder()
                .id(String.valueOf(w.getId()))
                .nik(w.getNik())
                .nama(w.getNama())
                .phoneNumber(w.getPhoneNumber())
                .rt(w.getRt())
                .rw(w.getRw())
                .alamat(w.getAlamat())
                .build();
    }

    /**
     * Whitelist field untuk sorting. Return nama field sesuai entity.
     * Default fallback: createdAt
     */
    private String normalizeSortField(String input) {
        if (input == null || input.isBlank()) return CREATED_AT;
        return switch (input.trim()) {
            case "nama"        -> "nama";
            case "nik"         -> "nik";
            case PHONE_NUMBER -> PHONE_NUMBER;
            case "rt"          -> "rt";
            case "rw"          -> "rw";
            case CREATED_AT   -> CREATED_AT;
            default -> {
                log.warn("[sort] unsupported sortField='{}' -> fallback to createdAt", input);
                yield CREATED_AT;
            }
        };
    }

    /** Normalisasi arah sort (default ASC) */
    private Sort.Direction normalizeSortDirection(String dir) {
        if (dir == null) return Sort.Direction.ASC;
        return "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }
}
