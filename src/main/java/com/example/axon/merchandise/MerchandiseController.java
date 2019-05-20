package com.example.axon.merchandise;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.IdentifierFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/merchandise")
public class MerchandiseController {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private MerchandiseService merchandiseService;

    @GetMapping
    public List<Merchandise> list() {
        return merchandiseService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Merchandise create(@RequestBody Merchandise merchandise) {
        String merchandiseId = IdentifierFactory.getInstance().generateIdentifier();
        CreateMerchandiseCommand cmd = new CreateMerchandiseCommand(
                merchandiseId, merchandise.getStock()
        );
        commandGateway.sendAndWait(cmd);
        return new Merchandise(merchandiseId, merchandise.getStock());
    }

    @GetMapping("/{merchandiseId}")
    public Merchandise get(@PathVariable String merchandiseId) {
        return merchandiseService.get(merchandiseId);
    }

    @PutMapping("/{merchandiseId}")
    public Merchandise update(@PathVariable String merchandiseId, @RequestBody Merchandise merchandise) {
        UpdateMerchandiseCommand cmd = new UpdateMerchandiseCommand(
                merchandiseId, merchandise.getStock()
        );
        commandGateway.sendAndWait(cmd);
        return new Merchandise(merchandiseId, merchandise.getStock());
    }

}
