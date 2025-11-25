package com.goimaster.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BatchUpdateWordStatsRequest {
    @NotEmpty(message = "Stats updates cannot be empty")
    @Valid
    private List<WordStatUpdate> updates;
    
    @Data
    public static class WordStatUpdate {
        @NotNull
        private UUID wordId;
        
        @Min(0)
        private Integer seenCount = 0;
        
        @Min(0)
        private Integer correctCount = 0;
        
        @Min(0)
        private Integer incorrectCount = 0;
    }
}

