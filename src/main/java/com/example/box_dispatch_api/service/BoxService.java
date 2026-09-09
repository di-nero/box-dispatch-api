package com.example.box_dispatch_api.service;

import com.example.box_dispatch_api.dto.BoxResponse;
import com.example.box_dispatch_api.dto.CreateBoxRequest;
import com.example.box_dispatch_api.dto.ItemRequest;
import com.example.box_dispatch_api.dto.ItemResponse;
import com.example.box_dispatch_api.dto.LoadItemsRequest;
import com.example.box_dispatch_api.entity.Box;
import com.example.box_dispatch_api.entity.Item;
import com.example.box_dispatch_api.enums.BoxState;
import com.example.box_dispatch_api.exception.BatteryTooLowException;
import com.example.box_dispatch_api.exception.BoxNotFoundException;
import com.example.box_dispatch_api.exception.InvalidBoxStateException;
import com.example.box_dispatch_api.exception.WeightLimitExceededException;
import com.example.box_dispatch_api.repository.BoxRepository;
import com.example.box_dispatch_api.repository.BoxWeightTotal;
import com.example.box_dispatch_api.repository.ItemRepository;
import com.example.box_dispatch_api.util.TxrefGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoxService {
    private static final int MIN_BATTERY_FOR_LOADING = 25;

    private static final Set<BoxState> LOADABLE_STATES = Set.of(BoxState.IDLE, BoxState.LOADING, BoxState.LOADED);

    private final ItemRepository itemRepository;

    private final BoxRepository boxRepository;

    private final TxrefGenerator txrefGenerator;

    @Transactional
    public BoxResponse createBox(CreateBoxRequest request) {
        Box box = Box.builder()
                .batteryCapacity(100)
                .state(BoxState.IDLE)
                .txref(txrefGenerator.generateTxref())
                .weightLimit(request.getWeightLimit())
                .build();

        Box saved = boxRepository.save(box);

        log.info("Created box txref={} weightLimit={}", saved.getTxref(), saved.getWeightLimit());

        return BoxResponse.from(saved);
    }

    @Transactional
    public BoxResponse loadBox(String txref, LoadItemsRequest request) {
        Box box = boxRepository.findByTxrefForUpdate(txref)
                .orElseThrow(() -> new BoxNotFoundException("Box not found!"));

        if (box.getBatteryCapacity() < MIN_BATTERY_FOR_LOADING) {
            log.warn("Rejected load on box txref={}: battery below {}% (actual={}%)",
                    txref, MIN_BATTERY_FOR_LOADING, box.getBatteryCapacity());
            throw new BatteryTooLowException("Box battery is below 25%");
        }

        if (!LOADABLE_STATES.contains(box.getState())) {
            log.warn("Rejected load on box txref={}: box in non-loadable state={}", txref, box.getState());
            throw new InvalidBoxStateException("Box cannot be loaded in it current state");
        }

        int newWeight = request.getItems().stream()
                .mapToInt(ItemRequest::getWeight)
                .sum();

        int existingWeight = itemRepository.findByBox(box).stream()
                .mapToInt(Item::getWeight)
                .sum();

        if (existingWeight + newWeight > box.getWeightLimit()) {
            log.warn("Rejected load on box txref={}: existingWeight={} + newWeight={} exceeds weightLimit={}",
                    txref, existingWeight, newWeight, box.getWeightLimit());
            throw new WeightLimitExceededException("Box weight limit exceeded");
        }

        List<Item> items = request.getItems().stream()
                .map(itemRequest -> Item.builder()
                        .name(itemRequest.getName())
                        .weight(itemRequest.getWeight())
                        .code(itemRequest.getCode())
                        .box(box)
                        .build())
                .toList();

        itemRepository.saveAll(items);

        box.setState(BoxState.LOADED);

        Box newBox = boxRepository.save(box);

        log.info("Loaded {} item(s) into box txref={}, new state={}", items.size(), txref, newBox.getState());

        return BoxResponse.from(newBox);
    }

    public List<ItemResponse> getBoxItems(String txref) {
        Box box = boxRepository.findByTxref(txref).orElseThrow(() -> new BoxNotFoundException("Box not found!"));
        return itemRepository.findByBox(box).stream()
                .map(ItemResponse::from)
                .toList();
    }

    public Page<BoxResponse> getAvailableBoxes(Pageable pageable) {
        Page<Box> candidates = boxRepository.findByBatteryCapacityGreaterThanEqualAndStateIn(
                MIN_BATTERY_FOR_LOADING, LOADABLE_STATES, pageable);

        List<UUID> boxIds = candidates.getContent().stream().map(Box::getId).toList();

        Map<UUID, Integer> weightByBoxId = itemRepository.sumWeightByBoxIds(boxIds).stream()
                .collect(Collectors.toMap(
                        BoxWeightTotal::getBoxId,
                        BoxWeightTotal::getTotalWeight));

        List<BoxResponse> available = candidates.getContent().stream()
                .filter(box -> weightByBoxId.getOrDefault(box.getId(), 0) < box.getWeightLimit())
                .map(BoxResponse::from)
                .toList();

        return new PageImpl<>(available, pageable, candidates.getTotalElements());
    }

    public int getBatteryLevel(String txref) {
        Box box = boxRepository.findByTxref(txref)
                .orElseThrow(() -> new BoxNotFoundException("Box not found!"));

        return box.getBatteryCapacity();
    }

}
