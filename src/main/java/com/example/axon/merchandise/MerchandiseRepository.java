package com.example.axon.merchandise;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MerchandiseRepository extends MongoRepository<Merchandise, String> {

    Optional<Merchandise> findByMerchandiseId(String merchandiseId);

}
