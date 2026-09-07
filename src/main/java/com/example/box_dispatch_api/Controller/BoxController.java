package com.example.box_dispatch_api.Controller;

import com.example.box_dispatch_api.DTO.BoxResponse;
import com.example.box_dispatch_api.DTO.CreateBoxRequest;
import com.example.box_dispatch_api.DTO.LoadItemsRequest;
import com.example.box_dispatch_api.Entity.Item;
import com.example.box_dispatch_api.Service.BoxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boxes")
@RequiredArgsConstructor
@Tag(name = "Box Management", description = "APIs for managing dispatch boxes and their items")
public class BoxController {

    private final BoxService boxService;

    @Operation(summary = "Create a new box")
    @PostMapping
    public ResponseEntity<BoxResponse> createBox(@Valid @RequestBody CreateBoxRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boxService.createBox(request));
    }

    @Operation(summary = "Get items in a box a box")
    @GetMapping("/{txref}/items")
    public ResponseEntity<List<Item>> getBoxItems(@PathVariable String txref) {
        return ResponseEntity.ok(boxService.getBoxItems(txref));
    }

    @Operation(summary = "Get available boxes")
    @GetMapping("/available")
    public ResponseEntity<List<BoxResponse>> getAvailableBoxes() {
        return ResponseEntity.ok(boxService.getAvailableBoxes());
    }

    @Operation(summary = "Get a box battery level")
    @GetMapping("/{txref}/battery")
    public ResponseEntity<Integer> getBatteryLevel(@PathVariable String txref) {
        return ResponseEntity.ok(boxService.getBatteryLevel(txref));
    }

    @Operation(summary = "Load items into a box")
    @PostMapping("/{txref}/items")
    public ResponseEntity<BoxResponse> loadBox(@PathVariable String txref, @Valid @RequestBody LoadItemsRequest request) {

        return ResponseEntity.ok(boxService.loadBox(txref, request));
    }
}
