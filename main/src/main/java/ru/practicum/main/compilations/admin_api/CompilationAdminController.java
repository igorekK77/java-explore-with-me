package ru.practicum.main.compilations.admin_api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.compilations.CompilationService;
import ru.practicum.main.compilations.dto.CompilationCreateDto;
import ru.practicum.main.compilations.dto.CompilationDto;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class CompilationAdminController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Valid @RequestBody CompilationCreateDto compilationCreateDto) {
        return compilationService.createCompilation(compilationCreateDto);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @Valid @RequestBody CompilationCreateDto compilationCreateDto) {
        return compilationService.updateCompilation(compId, compilationCreateDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        compilationService.deleteCompilation(compId);
    }

}
