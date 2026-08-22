package com.gdghongik.commerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 상품 등록 요청.
 *
 * 여기에 붙은 검증은 '요청 형식'에 대한 검증이다.
 * '상품이라면 지켜야 할 규칙'과는 다르다는 점을 2주차에 다시 다룬다.
 */
public record ProductCreateRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        String name,

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        long price,

        @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
        int stock
) {
}
