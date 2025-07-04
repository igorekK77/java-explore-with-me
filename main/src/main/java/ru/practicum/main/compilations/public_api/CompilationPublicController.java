package ru.practicum.main.compilations.public_api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.compilations.dto.CompilationDto;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class CompilationPublicController {
    private final CompilationPublicService compilationPublicService;

    @GetMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public List<CompilationDto> getCompilations(@RequestParam(defaultValue = "false") boolean pinned,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size) {
        return compilationPublicService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        return compilationPublicService.getCompilation(compId);
    }
}
