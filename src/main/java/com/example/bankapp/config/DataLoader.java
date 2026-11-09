package com.example.bankapp.config;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final CustomerRepository customerRepository;

    @Bean
    CommandLineRunner loadData() {
        return args -> {
            if (customerRepository.count() == 0) {
                customerRepository.save(Customer.builder()
                        .accountNumber("100001")
                        .fullName("Kamal Perera")
                        .email("kamal@example.com")
                        .balance(new BigDecimal("50000.00"))
                        .build());

                customerRepository.save(Customer.builder()
                        .accountNumber("100002")
                        .fullName("Nimali Silva")
                        .email("nimali@example.com")
                        .balance(new BigDecimal("25000.00"))
                        .build());

                customerRepository.save(Customer.builder()
                        .accountNumber("100003")
                        .fullName("Saman Fernando")
                        .email("saman@example.com")
                        .balance(new BigDecimal("10000.00"))
                        .build());
            }
        };
    }
}