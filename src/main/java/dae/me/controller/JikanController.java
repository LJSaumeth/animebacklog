package dae.me.controller;

import dae.me.dto.jikan.JikanAnimeItemDto;
import dae.me.service.JikanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jikan")
@RequiredArgsConstructor
public class JikanController {

    private final JikanService jikanService;

    @GetMapping("/search")
    public ResponseEntity<List<JikanAnimeItemDto>> search(@RequestParam String q) {
        return ResponseEntity.ok(jikanService.searchAnime(q));
    }

    @GetMapping("/anime/{malId}")
    public ResponseEntity<JikanAnimeItemDto> getById(@PathVariable Long malId) {
        return ResponseEntity.ok(jikanService.getAnimeById(malId));
    }
}
