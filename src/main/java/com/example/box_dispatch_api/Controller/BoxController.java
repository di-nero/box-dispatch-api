package com.example.box_dispatch_api.Controller;

import com.example.box_dispatch_api.DTO.BoxResponse;
import com.example.box_dispatch_api.DTO.CreateBoxRequest;
import com.example.box_dispatch_api.DTO.LoadItemsRequest;
import com.example.box_dispatch_api.Entity.Item;
import com.example.box_dispatch_api.Service.BoxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boxes")
@RequiredArgsConstructor
public class BoxController {

    private final BoxService boxService;

    @PostMapping
    public ResponseEntity<BoxResponse> createBox(@Valid @RequestBody CreateBoxRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boxService.createBox(request));
    }

    @GetMapping("/{txref}/items")
    public ResponseEntity<List<Item>> getBoxItems(@PathVariable String txref) {
        return ResponseEntity.ok(boxService.getBoxItems(txref));
    }
    @GetMapping("/available")
    public ResponseEntity<List<BoxResponse>> getAvailableBoxes() {
        return ResponseEntity.ok(boxService.getAvailableBoxes());
    }

    @GetMapping("/{txref}/battery")
    public ResponseEntity<Integer> getBatteryLevel(@PathVariable String txref) {
        return ResponseEntity.ok(boxService.getBatteryLevel(txref));
    }

    @PostMapping("/{txref}/items")
    public ResponseEntity<BoxResponse> loadBox(@PathVariable String txref, @Valid @RequestBody LoadItemsRequest request) {

        return ResponseEntity.ok(boxService.loadBox(txref, request));
    }
}
