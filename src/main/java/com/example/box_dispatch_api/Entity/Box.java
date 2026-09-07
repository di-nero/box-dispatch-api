package com.example.box_dispatch_api.Entity;

import com.example.box_dispatch_api.Enum.BoxState;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor@NoArgsConstructor
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 20)
    private String txref;

    private int weightLimit;

    private int batteryCapacity;

    @Enumerated(EnumType.STRING)
    private BoxState state;

    @OneToMany(mappedBy = "box")
    private List<Item> item;

}
