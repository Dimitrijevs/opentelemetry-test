package openTelemetry.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import openTelemetry.products.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer>{

}
