package com.goimaster.service;

import com.goimaster.model.PersonalWord;
import com.goimaster.model.Word;
import com.goimaster.repository.PersonalWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PersonalWordService {

    @Autowired
    private PersonalWordRepository personalWordRepository;

    public PersonalWord findOrCreatePersonalWord(UUID userId, Word word) {
        // Check if word already exists for this user
        Optional<PersonalWord> existing = personalWordRepository
                .findByUserIdAndKanjiAndFuriganaAndMeaningAndHanViet(
                        userId,
                        word.getKanji(),
                        word.getFurigana(),
                        word.getMeaning(),
                        word.getHanViet());

        if (existing.isPresent()) {
            return existing.get(); // Already exists, don't duplicate
        }

        // Create new personal word
        PersonalWord personalWord = new PersonalWord();
        personalWord.setUserId(userId);
        personalWord.setKanji(word.getKanji());
        personalWord.setFurigana(word.getFurigana());
        personalWord.setMeaning(word.getMeaning());
        personalWord.setHanViet(word.getHanViet());

        return personalWordRepository.save(personalWord);
    }

    public List<PersonalWord> getUserWords(UUID userId) {
        return personalWordRepository.findByUserIdOrderByAddedAtDesc(userId);
    }

    public List<PersonalWord> searchUserWords(UUID userId, String query) {
        return personalWordRepository.searchUserWords(userId, query);
    }
}
