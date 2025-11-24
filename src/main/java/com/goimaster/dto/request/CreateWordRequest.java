package com.goimaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWordRequest {
    @NotBlank(message = "Kanji is required")
    private String kanji;
    
    @NotBlank(message = "Han Viet is required")
    private String hanViet;
    
    @NotBlank(message = "Furigana is required")
    private String furigana;
    
    @NotBlank(message = "Meaning is required")
    private String meaning;
}


