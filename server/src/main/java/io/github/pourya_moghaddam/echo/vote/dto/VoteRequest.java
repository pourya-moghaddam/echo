package io.github.pourya_moghaddam.echo.vote.dto;

import io.github.pourya_moghaddam.echo.vote.VoteDirection;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteRequest {
    @NotNull
    private VoteDirection direction;
}
