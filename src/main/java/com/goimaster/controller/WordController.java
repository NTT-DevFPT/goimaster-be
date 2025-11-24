package com.goimaster.controller;

import com.goimaster.dto.request.BatchCreateWordsRequest;
import com.goimaster.dto.request.CreateWordRequest;
import com.goimaster.model.Word;
import com.goimaster.service.WordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class WordController {
    
    @Autowired
    private WordService wordService;
    
    @GetMapping("/lessons/{lessonId}/words")
    public ResponseEntity<List<Word>> getWords(@PathVariable UUID lessonId) {
        List<Word> words = wordService.getWordsByLessonId(lessonId);
        return ResponseEntity.ok(words);
    }
    
    @PostMapping("/lessons/{lessonId}/words")
    public ResponseEntity<List<Word>> createWords(
            @PathVariable UUID lessonId,
            @Valid @RequestBody BatchCreateWordsRequest request) {
        List<Word> words = wordService.createWords(lessonId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(words);
    }
    
    @PutMapping("/words/{id}")
    public ResponseEntity<Word> updateWord(
            @PathVariable UUID id,
            @Valid @RequestBody CreateWordRequest request) {
        Word word = wordService.updateWord(id, request);
        return ResponseEntity.ok(word);
    }
    
    @DeleteMapping("/words/{id}")
    public ResponseEntity<Void> deleteWord(@PathVariable UUID id) {
        wordService.deleteWord(id);
        return ResponseEntity.noContent().build();
    }
}


