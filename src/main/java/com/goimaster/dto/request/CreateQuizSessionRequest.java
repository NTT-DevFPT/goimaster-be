package com.goimaster.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateQuizSessionRequest {
    @NotBlank(message = "Mode is required")
    private String mode; // 'kanji_meaning', 'furigana_meaning', 'kanji_furigana'

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 second")
    private Integer durationSeconds;

    @NotNull(message = "Total questions is required")
    @Min(value = 0, message = "Total questions must be non-negative")
    private Integer totalQuestions;

    @NotNull(message = "Correct answers is required")
    @Min(value = 0, message = "Correct answers must be non-negative")
    private Integer correctAnswers;

    private java.util.List<QuizSessionDetailRequest> details;

    @Data
    public static class QuizSessionDetailRequest {
        @NotNull
        private java.util.UUID wordId;
        private boolean isCorrect;
        private String kanji;
        private String meaning;
    }
}
