package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private List<Item> processedItems = new ArrayList<>();


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();

        List<Item> processedItems = Collections.synchronizedList(new ArrayList<>());


        // Collect futures for each task
        List<CompletableFuture<Void>> futures = itemIds.stream()
                .map(id -> CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(100); // Simulate processing delay

                        // Retrieve item
                        Item item = itemRepository.findById(id).orElse(null);
                        if (item == null) {
                            return;
                        }
                        if(item.getStatus() == Status.processed) {
                            return;
                        }


                        item.setStatus(Status.processed);
                        itemRepository.save(item);
                        processedItems.add(item);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restore interrupted status
                        throw new RuntimeException("Interrupted while processing item ID: " + id, e);
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to process item ID: " + id, ex);
                    }
                }, executor))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> processedItems)
                .exceptionally(ex -> {
                    throw new CompletionException("Error during async processing", ex);
        });
    }


}

