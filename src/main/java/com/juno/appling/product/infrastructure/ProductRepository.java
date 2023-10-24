package com.juno.appling.product.infrastructure;

import com.juno.appling.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}