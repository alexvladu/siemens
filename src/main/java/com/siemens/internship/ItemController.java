package com.siemens.internship;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody Item item, BindingResult result) {
        logger.info("Received item: " + item.toString());

        if (result.hasErrors()) {
            List<ErrorResponse> errors = result.getFieldErrors()
                    .stream()
                    .map(error -> new ErrorResponse(error.getField(), error.getDefaultMessage()))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        System.out.println("Saving item: " + item);
        Item savedItem = itemService.save(item);
        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody Item updatedItem, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<ErrorResponse> errors = result.getFieldErrors()
                        .stream()
                        .map(error -> new ErrorResponse(error.getField(), error.getDefaultMessage()))
                        .collect(Collectors.toList());
                return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
            }
            Optional<Item> existingItemOpt = itemService.findById(id);
            if (existingItemOpt.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            if(updatedItem==null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Item existingItem = existingItemOpt.get();
            existingItem.setName(updatedItem.getName());
            existingItem.setDescription(updatedItem.getDescription());
            existingItem.setStatus(updatedItem.getStatus());
            existingItem.setEmail(updatedItem.getEmail());

            Item savedItem = itemService.save(existingItem);
            return new ResponseEntity<>(savedItem, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        Optional<Item> item = itemService.findById(id);
        if (item.isPresent()) {
            itemService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/process")
    public CompletableFuture<ResponseEntity<List<Item>>> processItems() {
        return itemService.processItemsAsync()
                .thenApply(items -> new ResponseEntity<>(items, HttpStatus.OK))
                .exceptionally(ex -> {
                    return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

}
