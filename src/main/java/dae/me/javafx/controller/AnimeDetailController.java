package dae.me.javafx.controller;

import dae.me.dto.AnimeDetailResponseDto;
import dae.me.dto.AnimeResponseDto;
import dae.me.dto.EpisodeDto;
import dae.me.exception.JikanApiException;
import dae.me.javafx.component.StarRating;
import dae.me.service.AnimeService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class AnimeDetailController {

    private static final int PAGE_SIZE = 5;

    @FXML
    private ImageView coverImage;

    @FXML
    private Label nameLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox ratingBox;

    @FXML
    private Label infoLabel;

    @FXML
    private Label episodesHeader;

    @FXML
    private VBox episodesContainer;

    @FXML
    private HBox paginationBox;

    @FXML
    private Label pageLabel;

    @FXML
    private FlowPane categoriesPane;

    private final AnimeService animeService;
    private final NavigationService navigationService;

    private AnimeResponseDto anime;
    private List<EpisodeDto> allEpisodes = Collections.emptyList();
    private int currentPage = 0;

    public AnimeDetailController(AnimeService animeService,
                                  NavigationService navigationService) {
        this.animeService = animeService;
        this.navigationService = navigationService;
    }

    public void setAnime(AnimeResponseDto anime) {
        this.anime = anime;
        if (anime != null) {
            loadDetail();
        }
    }

    @FXML
    public void initialize() {
    }

    private void loadDetail() {
        log.info("loadDetail() called for anime id={}", anime.id());
        episodesHeader.setText("Cargando episodios...");
        episodesHeader.setVisible(true);
        paginationBox.setVisible(false);

        new Thread(() -> {
            try {
                AnimeDetailResponseDto detail = animeService.getAnimeDetail(anime.id());
                log.info("getAnimeDetail returned: episodesList size={}, categories={}",
                        detail.episodesList() != null ? detail.episodesList().size() : 0,
                        detail.categoryNames());

                Platform.runLater(() -> populateUI(detail));
            } catch (JikanApiException e) {
                log.warn("JikanApiException in loadDetail", e);
                Platform.runLater(() -> {
                    episodesHeader.setText("Episodios");
                    showError("No se pudieron cargar los episodios. Intenta de nuevo.");
                });
            } catch (Exception e) {
                log.error("Error in loadDetail", e);
                Platform.runLater(() -> {
                    showError("Error al cargar el detalle: " + e.getMessage());
                    navigationService.navigateTo("/fxml/anime-gallery.fxml");
                });
            }
        }).start();
    }

    private void populateUI(AnimeDetailResponseDto detail) {
        nameLabel.setText(detail.name());

        loadCover(detail.imageUrl());

        statusLabel.setText(detail.status() != null ? detail.status().name() : "");

        ratingBox.getChildren().clear();
        StarRating starRating = new StarRating();
        starRating.setValue(detail.rating() != null ? detail.rating() : 0.0);
        starRating.setEditable(false);
        ratingBox.getChildren().add(starRating);

        infoLabel.setText(detail.episodes() + " episodios · " + detail.seasons() + " temporadas");

        allEpisodes = detail.episodesList() != null ? detail.episodesList() : Collections.emptyList();

        if (!allEpisodes.isEmpty()) {
            episodesHeader.setText("Episodios");
            currentPage = 0;
            showPage();
        } else {
            episodesHeader.setText("Sin datos de episodios");
            episodesContainer.getChildren().clear();
            paginationBox.setVisible(false);
        }

        categoriesPane.getChildren().clear();
        if (detail.categoryNames() != null) {
            for (String name : detail.categoryNames()) {
                Label chip = new Label(name);
                chip.getStyleClass().add("category-chip");
                categoriesPane.getChildren().add(chip);
            }
        }
    }

    private void showPage() {
        episodesContainer.getChildren().clear();
        paginationBox.setVisible(false);

        int totalPages = (int) Math.ceil((double) allEpisodes.size() / PAGE_SIZE);
        if (totalPages == 0) return;

        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allEpisodes.size());

        for (int i = start; i < end; i++) {
            EpisodeDto ep = allEpisodes.get(i);
            Label epLabel = new Label(ep.episodeNumber() + ". " + ep.title());
            epLabel.setStyle("-fx-text-fill: #FAEB92; -fx-font-size: 12px; -fx-padding: 2 0 2 0;");
            episodesContainer.getChildren().add(epLabel);
        }

        if (totalPages > 1) {
            pageLabel.setText("Pag " + (currentPage + 1) + " / " + totalPages);
            paginationBox.setVisible(true);
        }
    }

    @FXML
    public void onPrevPage() {
        if (currentPage > 0) {
            currentPage--;
            showPage();
        }
    }

    @FXML
    public void onNextPage() {
        int totalPages = (int) Math.ceil((double) allEpisodes.size() / PAGE_SIZE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            showPage();
        }
    }

    private void loadCover(String imageUrl) {
        if (imageUrl != null && !imageUrl.isBlank()) {
            try {
                File file = new File(imageUrl);
                if (file.exists()) {
                    coverImage.setImage(new Image(file.toURI().toString()));
                } else {
                    coverImage.setImage(new Image(imageUrl, true));
                }
            } catch (Exception e) {
                coverImage.setImage(createPlaceholder());
            }
        } else {
            coverImage.setImage(createPlaceholder());
        }
    }

    @FXML
    public void onVolver() {
        navigationService.navigateTo("/fxml/anime-gallery.fxml");
    }

    @FXML
    public void onEditar() {
        navigationService.navigateToAnimeForm(anime);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Image createPlaceholder() {
        return new Image("https://via.placeholder.com/200x280/0f3460/e0e0e0?text=Anime", true);
    }
}
