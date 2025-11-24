package com.goimaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLessonRequest {
    @NotBlank(message = "Lesson name is required")
    private String name;
    private Integer orderIndex = 0;
}


