package project.library.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookRecordDTO(@NotBlank String name, @NotBlank String author, @NotNull int pages) {
}
