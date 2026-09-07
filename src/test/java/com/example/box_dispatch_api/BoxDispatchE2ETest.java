package com.example.box_dispatch_api;

import com.example.box_dispatch_api.DTO.ItemRequest;
import com.example.box_dispatch_api.DTO.LoadItemsRequest;
import com.example.box_dispatch_api.Entity.Box;
import com.example.box_dispatch_api.Entity.Item;
import com.example.box_dispatch_api.Enum.BoxState;
import com.example.box_dispatch_api.Repository.BoxRepository;
import com.example.box_dispatch_api.Repository.ItemRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BoxDispatchE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("box_dispatch")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @org.springframework.test.context.DynamicPropertySource
    static void configureProperties(
            org.springframework.test.context.DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BoxRepository boxRepository;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void cleanDatabase() {
        itemRepository.deleteAll();
        boxRepository.deleteAll();
    }

    @Test
    void shouldCreateBox() throws Exception {

        String request = """
                {
                    "weightLimit": 500
                }
                """;

        mockMvc.perform(post("/boxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.txref").exists())
                .andExpect(jsonPath("$.weightLimit").value(500))
                .andExpect(jsonPath("$.batteryCapacity").value(100))
                .andExpect(jsonPath("$.state").value("IDLE"));
    }

    @Test
    void shouldLoadBoxWithItems() throws Exception {

        Box box = createBox();

        ItemRequest item = new ItemRequest();
        item.setName("Phone-1");
        item.setWeight(100);
        item.setCode("PHONE_001");

        LoadItemsRequest request = new LoadItemsRequest();
        request.setItems(List.of(item));

        mockMvc.perform(post("/boxes/{txref}/items", box.getTxref())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.txref").value(box.getTxref()))
                .andExpect(jsonPath("$.state").value("LOADED"));
    }

    @Test
    void shouldGetBoxItems() throws Exception {

        Box box = createBox();

        Item item = Item.builder()
                .name("Laptop")
                .weight(300)
                .code("LAPTOP_001")
                .box(box)
                .build();

        itemRepository.save(item);

        mockMvc.perform(get("/boxes/{txref}/items", box.getTxref()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].weight").value(300))
                .andExpect(jsonPath("$[0].code").value("LAPTOP_001"));
    }

    @Test
    void shouldGetBatteryLevel() throws Exception {

        Box box = createBox();

        mockMvc.perform(get("/boxes/{txref}/battery", box.getTxref()))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    void shouldReturnAvailableBoxes() throws Exception {

        Box box = createBox();

        mockMvc.perform(get("/boxes/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].txref").value(box.getTxref()))
                .andExpect(jsonPath("$[0].batteryCapacity").value(100))
                .andExpect(jsonPath("$[0].state").value("IDLE"));
    }

    @Test
    void shouldReturn404WhenBoxDoesNotExist() throws Exception {

        mockMvc.perform(get("/boxes/BOX-NOTFOUND/battery"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Box not found!"));
    }

    @Test
    void shouldRejectInvalidItemName() throws Exception {

        Box box = createBox();

        String request = """
                {
                    "items": [
                        {
                            "name": "Phone@123",
                            "weight": 100,
                            "code": "PHONE_001"
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/boxes/{txref}/items", box.getTxref())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldRejectItemWhenWeightLimitExceeded() throws Exception {

        Box box = createBox();

        Item existingItem = Item.builder()
                .name("Laptop")
                .weight(450)
                .code("LAPTOP_001")
                .box(box)
                .build();

        itemRepository.save(existingItem);

        String request = """
                {
                    "items": [
                        {
                            "name": "Phone-1",
                            "weight": 100,
                            "code": "PHONE_001"
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/boxes/{txref}/items", box.getTxref())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Box weight limit exceeded"));
    }

    private Box createBox() {

        Box box = Box.builder()
                .txref("BOX-TEST-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .weightLimit(500)
                .batteryCapacity(100)
                .state(BoxState.IDLE)
                .build();

        return boxRepository.save(box);
    }
}
