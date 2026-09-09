package com.example.box_dispatch_api.controller;

import com.example.box_dispatch_api.dto.BoxResponse;
import com.example.box_dispatch_api.dto.CreateBoxRequest;
import com.example.box_dispatch_api.dto.ItemResponse;
import com.example.box_dispatch_api.dto.LoadItemsRequest;
import com.example.box_dispatch_api.service.BoxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @Operation(summary = "Get items in a box")
    @GetMapping("/{txref}/items")
    public ResponseEntity<List<ItemResponse>> getBoxItems(@PathVariable String txref) {
        return ResponseEntity.ok(boxService.getBoxItems(txref));
    }

    @Operation(summary = "Get available boxes")
    @GetMapping("/available")
    public ResponseEntity<Page<BoxResponse>> getAvailableBoxes(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(boxService.getAvailableBoxes(pageable));
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
