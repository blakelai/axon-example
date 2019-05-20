package com.example.axon.merchandise;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Merchandise {

    @Id
    @Getter
    private String merchandiseId;

    @Getter
    @Setter
    private int stock;

    public Merchandise(String merchandiseId, int stock) {
        this.merchandiseId = merchandiseId;
        this.stock = stock;
    }
}
