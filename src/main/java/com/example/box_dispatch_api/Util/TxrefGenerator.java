package com.example.box_dispatch_api.Util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TxrefGenerator {

    public final String BOX_PRIFIX = "BOX-";

    public String generateTxref(){

        String uuid = UUID
                .randomUUID()
                .toString()
                .substring(0 , 8)
                .toUpperCase();

        return BOX_PRIFIX + uuid;

    }

}
