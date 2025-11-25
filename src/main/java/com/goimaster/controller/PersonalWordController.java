package com.goimaster.controller;

import com.goimaster.model.PersonalWord;
import com.goimaster.service.PersonalWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/personal-words")
public class PersonalWordController {

    @Autowired
    private PersonalWordService personalWordService;

    @GetMapping
    public List<PersonalWord> getUserWords(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String search) {
        UUID userUuid = UUID.fromString(userId);
        if (search != null && !search.trim().isEmpty()) {
            return personalWordService.searchUserWords(userUuid, search);
        }
        return personalWordService.getUserWords(userUuid);
    }
}
