package com.example.eshopapi.dto;

import java.math.BigDecimal;

public record CategoryStatResponse(
        Long id,
        String name,
        long productCount,
        long totalInventoryValue
) {}