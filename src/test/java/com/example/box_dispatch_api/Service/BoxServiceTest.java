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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoxServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BoxRepository boxRepository;

    @Mock
    private TxrefGenerator txrefGenerator;

    @InjectMocks
    private BoxService boxService;

    private Box box;

    @BeforeEach
    void setUp() {
        box = Box.builder()
                .id(UUID.randomUUID())
                .txref("BOX-12345678")
                .weightLimit(500)
                .batteryCapacity(100)
                .state(BoxState.IDLE)
                .item(new ArrayList<>())
                .build();
    }

    @Test
    void shouldCreateBoxSuccessfully() {

        CreateBoxRequest request = new CreateBoxRequest();
        request.setWeightLimit(500);

        when(txrefGenerator.generateTxref())
                .thenReturn("BOX-12345678");

        when(boxRepository.existsByTxref("BOX-12345678"))
                .thenReturn(false);

        when(boxRepository.save(any(Box.class)))
                .thenReturn(box);

        BoxResponse response = boxService.createBox(request);

        assertNotNull(response);
        assertEquals("BOX-12345678", response.getTxref());
        assertEquals(500, response.getWeightLimit());
        assertEquals(100, response.getBatteryCapacity());
        assertEquals(BoxState.IDLE, response.getState());

        verify(boxRepository).save(any(Box.class));
    }

    @Test
    void shouldThrowExceptionWhenBoxDoesNotExist() {

        LoadItemsRequest request = new LoadItemsRequest();

        when(boxRepository.findByTxref("BOX-NOTFOUND"))
                .thenReturn(Optional.empty());

        assertThrows(
                BoxNotFoundException.class,
                () -> boxService.loadBox("BOX-NOTFOUND", request)
        );
    }

    @Test
    void shouldThrowExceptionWhenBatteryIsTooLow() {

        box.setBatteryCapacity(20);

        LoadItemsRequest request = new LoadItemsRequest();

        when(boxRepository.findByTxref(box.getTxref()))
                .thenReturn(Optional.of(box));

        assertThrows(
                BatteryTooLowException.class,
                () -> boxService.loadBox(box.getTxref(), request)
        );
    }

    @Test
    void shouldThrowExceptionWhenBoxStateIsInvalid() {

        box.setState(BoxState.DELIVERING);

        LoadItemsRequest request = new LoadItemsRequest();

        when(boxRepository.findByTxref(box.getTxref()))
                .thenReturn(Optional.of(box));

        assertThrows(
                InvalidBoxStateException.class,
                () -> boxService.loadBox(box.getTxref(), request)
        );
    }

    @Test
    void shouldThrowExceptionWhenWeightLimitIsExceeded() {

        Item existingItem = Item.builder()
                .id(UUID.randomUUID())
                .name("Laptop")
                .weight(450)
                .code("LAPTOP_001")
                .box(box)
                .build();

        box.setItem(List.of(existingItem));

        ItemRequest newItem = new ItemRequest();
        newItem.setName("Phone-1");
        newItem.setWeight(100);
        newItem.setCode("PHONE_001");

        LoadItemsRequest request = new LoadItemsRequest();
        request.setItems(List.of(newItem));

        when(boxRepository.findByTxref(box.getTxref()))
                .thenReturn(Optional.of(box));

        assertThrows(
                WeightLimitExceededException.class,
                () -> boxService.loadBox(box.getTxref(), request)
        );
    }

    @Test
    void shouldLoadBoxSuccessfully() {

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setName("Phone-1");
        itemRequest.setWeight(100);
        itemRequest.setCode("PHONE_001");

        LoadItemsRequest request = new LoadItemsRequest();
        request.setItems(List.of(itemRequest));

        when(boxRepository.findByTxref(box.getTxref()))
                .thenReturn(Optional.of(box));

        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(boxRepository.save(any(Box.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BoxResponse response =
                boxService.loadBox(box.getTxref(), request);

        assertNotNull(response);
        assertEquals(BoxState.LOADED, response.getState());
        assertEquals(box.getTxref(), response.getTxref());

        verify(itemRepository).save(any(Item.class));
        verify(boxRepository).save(box);
    }

    @Test
    void shouldReturnBoxItems() {

        Item item = Item.builder()
                .id(UUID.randomUUID())
                .name("Phone-1")
                .weight(100)
                .code("PHONE_001")
                .box(box)
                .build();

        when(boxRepository.findByTxref(box.getTxref()))
                .thenReturn(Optional.of(box));

        when(itemRepository.findByBox(box))
                .thenReturn(List.of(item));

        List<Item> result =
                boxService.getBoxItems(box.getTxref());

        assertEquals(1, result.size());
        assertEquals("Phone-1", result.get(0).getName());

        verify(itemRepository).findByBox(box);
    }

    @Test
    void shouldReturnBatteryLevel() {

        when(boxRepository.findByTxref(box.getTxref()))
                .thenReturn(Optional.of(box));

        int battery =
                boxService.getBatteryLevel(box.getTxref());

        assertEquals(100, battery);
    }

    @Test
    void shouldThrowExceptionWhenGettingBatteryForUnknownBox() {

        when(boxRepository.findByTxref("BOX-NOTFOUND"))
                .thenReturn(Optional.empty());

        assertThrows(
                BoxNotFoundException.class,
                () -> boxService.getBatteryLevel("BOX-NOTFOUND")
        );
    }

    @Test
    void shouldReturnAvailableBoxes() {

        when(boxRepository.findAll())
                .thenReturn(List.of(box));

        List<BoxResponse> result =
                boxService.getAvailableBoxes();

        assertEquals(1, result.size());
        assertEquals(box.getTxref(), result.get(0).getTxref());
    }

    @Test
    void shouldNotReturnBoxWithLowBattery() {

        box.setBatteryCapacity(20);

        when(boxRepository.findAll())
                .thenReturn(List.of(box));

        List<BoxResponse> result =
                boxService.getAvailableBoxes();

        assertTrue(result.isEmpty());
    }
}
