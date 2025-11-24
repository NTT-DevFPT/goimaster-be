package com.goimaster.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateLessonRequest {
    @NotBlank(message = "Lesson name is required")
    private String name;
    private Integer orderIndex = 0;

    @Valid
    private List<CreateWordRequest> words;
}


