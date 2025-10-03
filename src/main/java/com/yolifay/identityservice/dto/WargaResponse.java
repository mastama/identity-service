package com.yolifay.identityservice.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record WargaResponse(
        String id,
        String nik,
        String nama,
        String phoneNumber,
        String alamat,
        Integer rt,
        Integer rw
) implements Serializable {}