package com.goimaster.controller;

import com.goimaster.model.GlobalWord;
import com.goimaster.service.GlobalWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/global-words")
public class GlobalWordController {

    @Autowired
    private GlobalWordService globalWordService;

    @GetMapping
    public List<GlobalWord> getAllGlobalWords(
            @RequestParam(required = false) String search) {
        if (search != null && !search.trim().isEmpty()) {
            return globalWordService.searchWords(search);
        }
        return globalWordService.getAllWords();
    }

    @GetMapping("/{id}")
    public GlobalWord getGlobalWordById(@PathVariable UUID id) {
        return globalWordService.getWordById(id);
    }

    @GetMapping("/quiz-pool")
    public List<GlobalWord> getQuizPool(
            @RequestParam String furiganaPattern) {
        return globalWordService.findSimilarWords(furiganaPattern);
    }
}
