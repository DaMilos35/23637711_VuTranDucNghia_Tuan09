package com.example.eshopapi.dto;

import jakarta.validation.constraints.NotNull;

public record StockUpdateRequest(
        @NotNull(message = "Giá trị delta không được để trống")
        Integer delta
) {}