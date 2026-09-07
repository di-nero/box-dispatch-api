package com.example.box_dispatch_api.Service;

import com.example.box_dispatch_api.DTO.BoxResponse;
import com.example.box_dispatch_api.DTO.CreateBoxRequest;
import com.example.box_dispatch_api.DTO.ItemRequest;
import com.example.box_dispatch_api.DTO.LoadItemsRequest;
import com.example.box_dispatch_api.Entity.Box;
import com.example.box_dispatch_api.Entity.Item;
import com.example.box_dispatch_api.Enum.BoxState;
import com.example.box_dispatch_api.Exception.BatteryTooLowException;
import com.example.box_dispatch_api.Exception.BoxNotFoundException;
import com.example.box_dispatch_api.Exception.InvalidBoxStateException;
import com.example.box_dispatch_api.Exception.WeightLimitExceededException;
import com.example.box_dispatch_api.Repository.BoxRepository;
import com.example.box_dispatch_api.Repository.ItemRepository;
import com.example.box_dispatch_api.Util.TxrefGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoxService {

    private final ItemRepository itemRepository;

    private final BoxRepository boxRepository;

    private final TxrefGenerator txrefGenerator;

    public BoxResponse createBox(CreateBoxRequest request){

        String txref;
        do {
            txref = txrefGenerator.generateTxref();
        } while (boxRepository.existsByTxref(txref));

        Box box = Box.builder()
                .batteryCapacity(100)
                .state(BoxState.IDLE)
                .txref(txref)
                .weightLimit(request.getWeightLimit())
                .build();

        Box save = boxRepository.save(box);

        return BoxResponse.builder()
                .id(save.getId())
                .txref(save.getTxref())
                .batteryCapacity(save.getBatteryCapacity())
                .state(save.getState())
                .weightLimit(save.getWeightLimit())
                .build();
    }

    public BoxResponse loadBox(String txref , LoadItemsRequest request){

        Box box = boxRepository.findByTxref(txref).orElseThrow(() -> new BoxNotFoundException("Box not found!"));

        if (box.getBatteryCapacity() < 25) {
            throw new BatteryTooLowException("Box battery is below 25%");
        }

        if (box.getState().equals(BoxState.DELIVERED) || box.getState().equals(BoxState.DELIVERING) || box.getState().equals(BoxState.RETURNING)){
            throw new InvalidBoxStateException("Box cannot be loaded in it current state");
        }

        int newWeight = 0;

        for (ItemRequest item : request.getItems()) {
            newWeight += item.getWeight();
        }

        int existingWeight = 0;

        for (Item item : box.getItem()) {
            existingWeight += item.getWeight();
        }

        if (existingWeight + newWeight > box.getWeightLimit()) {
            throw new WeightLimitExceededException("Box weight limit exceeded");
        }

        for (ItemRequest itemRequest : request.getItems()) {

            Item item = Item.builder()
                    .name(itemRequest.getName())
                    .weight(itemRequest.getWeight())
                    .code(itemRequest.getCode())
                    .box(box)
                    .build();

            itemRepository.save(item);
        }

        box.setState(BoxState.LOADED);

        Box save = boxRepository.save(box);

        return BoxResponse.builder()
                .id(save.getId())
                .txref(save.getTxref())
                .weightLimit(save.getWeightLimit())
                .batteryCapacity(save.getBatteryCapacity())
                .state(save.getState())
                .build();
    }

    public List<Item> getBoxItems(String txref) {
        Box box = boxRepository.findByTxref(txref).orElseThrow(() -> new BoxNotFoundException("Box not found!"));
        return itemRepository.findByBox(box);
    }


    public List<BoxResponse> getAvailableBoxes() {

        List<Box> boxes = boxRepository.findAll();

        return boxes.stream()
                .filter(box -> box.getBatteryCapacity() >= 25)
                .filter(box ->
                        box.getState() == BoxState.IDLE ||
                                box.getState() == BoxState.LOADING ||
                                box.getState() == BoxState.LOADED)
                .filter(box -> {
                    int currentWeight = 0;

                    for (Item item : box.getItem()) {
                        currentWeight += item.getWeight();
                    }

                    return currentWeight < box.getWeightLimit();
                })
                .map(box -> BoxResponse.builder()
                        .id(box.getId())
                        .txref(box.getTxref())
                        .weightLimit(box.getWeightLimit())
                        .batteryCapacity(box.getBatteryCapacity())
                        .state(box.getState())
                        .build())
                .toList();
    }

    public int getBatteryLevel(String txref) {

        Box box = boxRepository.findByTxref(txref)
                .orElseThrow(() -> new BoxNotFoundException("Box not found!"));

        return box.getBatteryCapacity();
    }


}
