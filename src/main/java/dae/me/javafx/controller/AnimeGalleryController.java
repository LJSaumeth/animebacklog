package dae.me.javafx.controller;

import dae.me.dto.AnimeResponseDto;
import dae.me.dto.PagedResponseDto;
import dae.me.service.AnimeService;
import dae.me.javafx.component.StarRating;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class AnimeGalleryController {

    @FXML
    private FlowPane galleryPane;

    private final AnimeService animeService;
    private final NavigationService navigationService;

    public AnimeGalleryController(AnimeService animeService,
                                   NavigationService navigationService) {
        this.animeService = animeService;
        this.navigationService = navigationService;
    }

    @FXML
    public void initialize() {
        loadGallery();
    }

    private void loadGallery() {
        PagedResponseDto<AnimeResponseDto> result = animeService.findAll(
                null, null, null, null, null, "name", "asc", 0, 500);

        galleryPane.getChildren().clear();

        for (AnimeResponseDto anime : result.content()) {
            VBox card = createCard(anime);
            galleryPane.getChildren().add(card);
        }
    }

    private VBox createCard(AnimeResponseDto anime) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(160);
        card.setStyle("-fx-background-color: #0a0a0a; -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(140);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        if (anime.imageUrl() != null && !anime.imageUrl().isBlank()) {
            try {
                File file = new File(anime.imageUrl());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                } else {
                    imageView.setImage(new Image(anime.imageUrl(), true));
                }
            } catch (Exception e) {
                imageView.setImage(createPlaceholder());
            }
        } else {
            imageView.setImage(createPlaceholder());
        }

        Label nameLabel = new Label(anime.name());
        nameLabel.setStyle("-fx-text-fill: #FAEB92; -fx-font-size: 12px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(140);
        nameLabel.setAlignment(Pos.CENTER);

        String ratingText = anime.rating() != null
                ? String.format(" %.1f/5", anime.rating()) : " Sin rating";
        Label ratingLabel = new Label(ratingText);
        ratingLabel.setStyle("-fx-text-fill: #FF5FCF; -fx-font-size: 11px;");

        StarRating starDisplay = new StarRating();
        starDisplay.setValue(anime.rating() != null ? anime.rating() : 0.0);
        starDisplay.setEditable(false);
        starDisplay.setStyle("-fx-font-size: 11px;");

        card.getChildren().addAll(imageView, nameLabel, starDisplay, ratingLabel);

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                navigationService.navigateToAnimeForm(anime);
            }
        });

        return card;
    }

    private Image createPlaceholder() {
        return new Image("https://via.placeholder.com/140x200/0f3460/e0e0e0?text=Anime", true);
    }
}
