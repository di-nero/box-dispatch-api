package com.example.box_dispatch_api.entity;

import com.example.box_dispatch_api.enums.BoxState;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "box")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Box {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "txref", length = 20, unique = true, nullable = false)
    private String txref;

    @Column(name = "weight_limit", nullable = false)
    private int weightLimit;

    @Column(name = "battery_capacity", nullable = false)
    private int batteryCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private BoxState state;

    @Version
    @Column(name = "version")
    private Long version;

    @Builder.Default
    @OneToMany(mappedBy = "box")
    private List<Item> items = new ArrayList<>();
}
