package dae.me.javafx.controller;

import dae.me.dto.AnimeResponseDto;

public interface NavigationService {
    void navigateTo(String fxmlPath);
    void navigateToAnimeForm(AnimeResponseDto anime);
    void navigateToAnimeDetail(AnimeResponseDto anime);
    void refreshCurrentView();
}
