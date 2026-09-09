package com.example.box_dispatch_api.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TxrefGenerator {
    private static final String BOX_PREFIX = "BOX-";

    public String generateTxref() {
        String hex = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16)
                .toUpperCase();

        return BOX_PREFIX + hex;
    }

}
