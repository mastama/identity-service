package com.yolifay.identityservice.service;

import com.yolifay.identityservice.dto.WargaCreateRequest;
import com.yolifay.identityservice.dto.WargaResponse;
import com.yolifay.identityservice.entity.Warga;
import com.yolifay.identityservice.exception.ConflictException;
import com.yolifay.identityservice.exception.DataNotFoundException;
import com.yolifay.identityservice.repository.WargaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WargaService {
    private final WargaRepository wargaRepository;

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
    public List<WargaResponse> getAllWarga() {
        log.info("Start get all warga");

        List<Warga> wargaList = wargaRepository.findAll();
        List<WargaResponse> responseList = wargaList.stream()
                .map(this::mapToResponse)
                .toList();

        log.info("End get all warga, total: {}", responseList.size());
        return responseList;
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
}
