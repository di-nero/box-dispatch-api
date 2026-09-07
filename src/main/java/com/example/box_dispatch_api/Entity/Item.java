package com.example.box_dispatch_api.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private int weight;

    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    private Box box;
}
