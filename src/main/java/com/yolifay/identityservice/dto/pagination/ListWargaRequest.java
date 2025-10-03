package com.yolifay.identityservice.dto.pagination;

import com.yolifay.identityservice.dto.filter.WargaFilter;

public record ListWargaRequest(
        BasePaging paging,
        WargaFilter wargaFilter
) {
}
