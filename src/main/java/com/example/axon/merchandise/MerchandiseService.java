package com.example.axon.merchandise;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ProcessingGroup("projections")
public class MerchandiseService {

    private static final Logger logger = LoggerFactory.getLogger(MerchandiseService.class);

    @Autowired
    private MerchandiseRepository merchandiseRepository;

    public List<Merchandise> list() {
        return merchandiseRepository.findAll();
    }

    public Merchandise get(String merchandiseId) {
        return merchandiseRepository.findByMerchandiseId(merchandiseId).get();
    }

    @EventHandler
    public void on(MerchandiseCreatedEvent event) {
        Merchandise merchandise = new Merchandise(event.getMerchandiseId(), event.getStock());
        merchandiseRepository.save(merchandise);
    }

    @EventHandler
    public void on(MerchandiseUpdatedEvent event) throws InterruptedException {
        Merchandise merchandise = merchandiseRepository.findByMerchandiseId(event.getMerchandiseId()).get();
        merchandise.setStock(event.getStock());
        Thread.sleep(10000);
        merchandiseRepository.save(merchandise);
        logger.info("done update merchandise");
    }

    @EventHandler
    public void on(MerchandiseReservedEvent event) {
        Merchandise merchandise = merchandiseRepository.findByMerchandiseId(event.getMerchandiseId()).get();
        merchandise.setStock(merchandise.getStock() - event.getQuantity());
        merchandiseRepository.save(merchandise);
        logger.info("done reserve merchandise");
    }

    @EventHandler
    public void on(MerchandiseReserveCanceledEvent event) {
        Merchandise merchandise = merchandiseRepository.findByMerchandiseId(event.getMerchandiseId()).get();
        merchandise.setStock(merchandise.getStock() + event.getQuantity());
        merchandiseRepository.save(merchandise);
    }
}
