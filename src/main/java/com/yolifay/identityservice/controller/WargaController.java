package com.yolifay.identityservice.controller;

import com.yolifay.identityservice.common.Constants;
import com.yolifay.identityservice.common.ConstantsProperties;
import com.yolifay.identityservice.common.ResponseApiService;
import com.yolifay.identityservice.common.ResponseApiUtil;
import com.yolifay.identityservice.dto.WargaCreateRequest;
import com.yolifay.identityservice.dto.WargaResponse;
import com.yolifay.identityservice.dto.filter.WargaFilter;
import com.yolifay.identityservice.dto.pagination.BasePaging;
import com.yolifay.identityservice.dto.pagination.ListWargaRequest;
import com.yolifay.identityservice.dto.pagination.PageEnvelope;
import com.yolifay.identityservice.service.WargaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/warga")
@Validated
public class WargaController {
    private final WargaService wargaService;
    private final ConstantsProperties constantsProperties;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseApiService<WargaResponse>> createWarga(@RequestBody @Valid WargaCreateRequest req) {
        log.info("Incoming create warga: {}", req.nama());
        WargaResponse res = wargaService.createWarga(req);

        log.info("Outgoing Warga created: {}", res.nama());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseApiUtil.setResponse(
                        HttpStatus.CREATED.value(),
                        constantsProperties.getServiceId(),
                        Constants.RESPONSE.CREATED,
                        res
                )
        );
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseApiService<PageEnvelope<WargaResponse>>> getAllWarga(
            @RequestParam (required = false) Integer page,
            @RequestParam (required = false, name = "perpage") Integer perPage,
            @RequestParam (required = false, name = "sortField") String sortField,
            @RequestParam (required = false, name = "sortDirection") String sortDirection,
            @RequestParam (required = false, name = "q") String q,
            @RequestParam (required = false, name = "rt") Integer rt,
            @RequestParam (required = false, name = "rw") Integer rw
    ) {
        log.info("Incoming get all warga");
        // Simpan raw parameters into BasePaging
        var paging = new BasePaging(page, perPage, sortField, sortDirection, q);

        // Create filter object
        WargaFilter wargaFilter = new WargaFilter();
        wargaFilter.setRt(rt);
        wargaFilter.setRw(rw);

        var req = new ListWargaRequest(paging, wargaFilter);
        var resp = wargaService.getAllWarga(req);

        log.info("Outgoing All Warga data page={} size={} totalElements={}",
                resp.page(), resp.size(), resp.totalElements());

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseApiUtil.setResponse(
                        HttpStatus.OK.value(),
                        constantsProperties.getServiceId(),
                        Constants.RESPONSE.APPROVED,
                        resp
                )
        );
    }

    @GetMapping(value = "/by-nik/{nik}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseApiService<WargaResponse>> getWargaByNik(
            @PathVariable @Pattern(regexp = "\\d{16}", message = "NIK harus 16 digit") String nik) {
        log.info("Incoming get warga by NIK: {}", nik);
        WargaResponse res = wargaService.getWargaByNik(nik);

        log.info("Outgoing Warga found by NIK: {}", nik);
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseApiUtil.setResponse(
                        HttpStatus.OK.value(),
                        constantsProperties.getServiceId(),
                        Constants.RESPONSE.APPROVED,
                        res
                )
        );
    }

    @PutMapping("/{nik}")
    public ResponseEntity<ResponseApiService<WargaResponse>> updateWarga(
            @PathVariable("nik") String nik,
            @RequestBody @Valid WargaCreateRequest req) {
        log.info("Incoming update warga by NIK: {}", nik);
        WargaResponse res = wargaService.updateWarga(nik, req);

        log.info("Outgoing Warga updated by NIK: {}", nik);
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseApiUtil.setResponse(
                        HttpStatus.OK.value(),
                        constantsProperties.getServiceId(),
                        Constants.RESPONSE.APPROVED,
                        res
                )
        );
    }

    @DeleteMapping("/{nik}")
    public ResponseEntity<ResponseApiService<String>> deleteWarga(@PathVariable("nik") String nik) {
        log.info("Incoming delete warga by NIK: {}", nik);
        wargaService.deleteWarga(nik);

        log.info("Outgoing Warga deleted by NIK: {}", nik);
        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseApiUtil.setResponse(
                        HttpStatus.OK.value(),
                        constantsProperties.getServiceId(),
                        Constants.RESPONSE.APPROVED,
                        "Warga dengan NIK " + nik + " berhasil dihapus"
                )
        );
    }
}
