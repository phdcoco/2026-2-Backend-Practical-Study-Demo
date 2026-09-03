package com.gdghongik.commerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProductCreateRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        String name,

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        long price,

        @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
        int stock
) {
}
