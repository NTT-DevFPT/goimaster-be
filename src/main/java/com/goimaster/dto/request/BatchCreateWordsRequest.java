package com.goimaster.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchCreateWordsRequest {
    @NotEmpty(message = "Words list cannot be empty")
    @Valid
    private List<CreateWordRequest> words;
}


