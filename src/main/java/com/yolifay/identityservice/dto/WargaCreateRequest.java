package com.yolifay.identityservice.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;

public record WargaCreateRequest(
        @NotBlank @Size(min=16,max=16) String nik,
        @NotBlank String nama,
        @NotBlank @Size(min=10,max=15) String phoneNumber,
        String alamat,
        @Min(1) @Max(999) Integer rt,
        @Min(1) @Max(999) Integer rw
) implements Serializable {}
