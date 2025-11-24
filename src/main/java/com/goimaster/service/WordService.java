package com.goimaster.service;

import com.goimaster.dto.request.BatchCreateWordsRequest;
import com.goimaster.dto.request.CreateWordRequest;
import com.goimaster.model.Lesson;
import com.goimaster.model.Word;
import com.goimaster.repository.LessonRepository;
import com.goimaster.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class WordService {
    
    @Autowired
    private WordRepository wordRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private LessonService lessonService;
    
    public List<Word> getWordsByLessonId(UUID lessonId) {
        return wordRepository.findByLessonIdOrderByCreatedAtAsc(lessonId);
    }
    
    public List<Word> createWords(UUID lessonId, BatchCreateWordsRequest request) {
        // Verify lesson exists
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        
        List<Word> words = request.getWords().stream()
                .map(wordRequest -> {
                    Word word = new Word();
                    word.setLessonId(lessonId);
                    word.setKanji(wordRequest.getKanji());
                    word.setHanViet(wordRequest.getHanViet());
                    word.setFurigana(wordRequest.getFurigana());
                    word.setMeaning(wordRequest.getMeaning());
                    return word;
                })
                .collect(Collectors.toList());
        
        List<Word> savedWords = wordRepository.saveAll(words);
        
        // Update lesson word count
        long totalWordCount = wordRepository.countByLessonId(lessonId);
        lessonService.updateWordCount(lessonId, (int) totalWordCount);
        
        return savedWords;
    }
    
    public Word updateWord(UUID wordId, CreateWordRequest request) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Word not found"));
        
        word.setKanji(request.getKanji());
        word.setHanViet(request.getHanViet());
        word.setFurigana(request.getFurigana());
        word.setMeaning(request.getMeaning());
        
        return wordRepository.save(word);
    }
    
    public void deleteWord(UUID wordId) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Word not found"));
        
        UUID lessonId = word.getLessonId();
        wordRepository.delete(word);
        
        // Update lesson word count
        long totalWordCount = wordRepository.countByLessonId(lessonId);
        lessonService.updateWordCount(lessonId, (int) totalWordCount);
    }
}


