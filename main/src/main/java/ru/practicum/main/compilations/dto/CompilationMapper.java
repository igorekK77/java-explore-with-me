package ru.practicum.main.compilations.dto;

import ru.practicum.main.compilations.Compilation;

public class CompilationMapper {
    public static Compilation toCompilationFromCreateDto(CompilationCreateDto createDto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(createDto.getTitle());
        compilation.setPinned(createDto.getPinned());
        return compilation;
    }

    public static CompilationDto toCompilationDto(Compilation compilation) {
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setTitle(compilation.getTitle());
        compilationDto.setId(compilation.getId());
        compilationDto.setPinned(compilation.isPinned());
        return compilationDto;
    }
}
