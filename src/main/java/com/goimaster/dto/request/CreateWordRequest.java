package com.goimaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateWordRequest {
    private String kanji;

    private String hanViet;

    @NotBlank(message = "Furigana is required")
    private String furigana;

    @NotBlank(message = "Meaning is required")
    private String meaning;
}
