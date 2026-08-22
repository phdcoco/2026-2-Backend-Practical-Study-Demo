package com.gdghongik.commerce.config;

import com.gdghongik.commerce.entity.Product;
import com.gdghongik.commerce.entity.SellingStatus;
import com.gdghongik.commerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션을 띄우면 확인용 상품이 자동으로 들어간다.
 * 테스트에서는 뜨지 않도록 test 프로파일을 제외한다.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        productRepository.save(new Product("기계식 키보드", 129_000L, 10));
        productRepository.save(new Product("무선 마우스", 45_000L, 3));

        Product limited = new Product("한정판 마우스패드", 19_000L, 1);
        productRepository.save(limited);

        Product stopped = new Product("단종된 USB 허브", 25_000L, 5);
        stopped.setStatus(SellingStatus.STOPPED);
        productRepository.save(stopped);
    }
}
