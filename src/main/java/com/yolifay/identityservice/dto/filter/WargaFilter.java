package com.yolifay.identityservice.dto.filter;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class WargaFilter implements Serializable {
    @Serial
    private static final long serialVersionUID = -8938046858929477746L;
    private Integer rt;
    private Integer rw;
}
