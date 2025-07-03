package ru.practicum.main.compilations.public_api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.compilations.dto.CompilationDto;

import java.util.List;

@RestController
@RequestMapping("/compilation")
@RequiredArgsConstructor
public class CompilationPublicController {
    private final CompilationPublicService compilationPublicService;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam boolean pinned, @RequestParam int from,
                                                @RequestParam int size) {
        return compilationPublicService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        return compilationPublicService.getCompilation(compId);
    }
}
