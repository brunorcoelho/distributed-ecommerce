package com.distributed.ecommerce.inventory.config;

import com.distributed.ecommerce.inventory.model.Product;
import com.distributed.ecommerce.inventory.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Value("${inventory.initialize-sample-data:true}")
    private boolean initializeSampleData;
    
    @Override
    public void run(String... args) throws Exception {
        if (initializeSampleData && productRepository.count() == 0) {
            logger.info("Initializing sample product data...");
            initializeSampleProducts();
            logger.info("Sample product data initialized successfully");
        } else {
            logger.info("Skipping sample data initialization (data already exists or disabled)");
        }
    }
    
    private void initializeSampleProducts() {
        List<Product> sampleProducts = List.of(
                new Product(
                        "Smartphone Galaxy",
                        "Smartphone Android com 128GB de armazenamento, câmera de 48MP e tela de 6.1 polegadas.",
                        new BigDecimal("899.99"),
                        15
                ),
                new Product(
                        "Notebook Gamer",
                        "Notebook para jogos com processador Intel i7, 16GB RAM, SSD 512GB e placa de vídeo RTX 3060.",
                        new BigDecimal("3299.99"),
                        8
                ),
                new Product(
                        "Fone de Ouvido Bluetooth",
                        "Fone de ouvido sem fio com cancelamento de ruído, autonomia de 30 horas.",
                        new BigDecimal("199.99"),
                        25
                ),
                new Product(
                        "Tablet 10 polegadas",
                        "Tablet com tela de 10 polegadas, 64GB de armazenamento e suporte a caneta stylus.",
                        new BigDecimal("549.99"),
                        12
                ),
                new Product(
                        "Smart TV 55\"",
                        "Smart TV LED 55 polegadas 4K com sistema Android TV e HDR.",
                        new BigDecimal("1899.99"),
                        6
                ),
                new Product(
                        "Console de Videogame",
                        "Console de última geração com SSD de 1TB e suporte a jogos em 4K.",
                        new BigDecimal("2499.99"),
                        4
                )
        );
        
        productRepository.saveAll(sampleProducts);
        logger.info("Inserted {} sample products", sampleProducts.size());
    }
}
