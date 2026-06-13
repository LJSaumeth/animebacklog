package dae.me.javafx.controller;

import dae.me.dto.AnimeRequestDto;
import dae.me.dto.AnimeResponseDto;
import dae.me.dto.CategoryResponseDto;
import dae.me.entity.Anime.AnimeStatus;
import dae.me.service.AnimeService;
import dae.me.service.CategoryService;
import dae.me.javafx.component.StarRating;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AnimeFormController {

    @FXML
    private Label titleLabel;

    @FXML
    private TextField nameField;

    @FXML
    private TextField episodesField;

    @FXML
    private TextField seasonsField;

    @FXML
    private ComboBox<AnimeStatus> statusCombo;

    @FXML
    private HBox ratingContainer;

    private StarRating starRating;

    @FXML
    private TextField imageUrlField;

    @FXML
    private ListView<CategoryCheckItem> categoryList;

    @FXML
    private Label nameError;

    @FXML
    private Label episodesError;

    private final AnimeService animeService;
    private final CategoryService categoryService;
    private final NavigationService navigationService;

    private AnimeResponseDto editAnime;
    private Long importMalId;

    public AnimeFormController(AnimeService animeService,
                                CategoryService categoryService,
                                NavigationService navigationService) {
        this.animeService = animeService;
        this.categoryService = categoryService;
        this.navigationService = navigationService;
    }

    @FXML
    public void initialize() {
        statusCombo.getItems().setAll(AnimeStatus.values());
        statusCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(AnimeStatus s) {
                return s == null ? "" : s.name();
            }
            @Override
            public AnimeStatus fromString(String s) {
                return s == null || s.isEmpty() ? null : AnimeStatus.valueOf(s);
            }
        });

        starRating = new StarRating();
        ratingContainer.getChildren().add(starRating);

        setupCategoryList();
        clearForm();
    }

    public void setEditMode(AnimeResponseDto anime) {
        this.editAnime = anime;
        titleLabel.setText("Editar Anime");
        nameField.setText(anime.name());
        episodesField.setText(String.valueOf(anime.episodes()));
        seasonsField.setText(anime.seasons() != null ? anime.seasons() : "");
        statusCombo.setValue(anime.status());
        starRating.setValue(anime.rating() != null ? anime.rating() : 0.0);
        imageUrlField.setText(anime.imageUrl() != null ? anime.imageUrl() : "");
        setSelectedCategories(anime.categoryIds());
        importMalId = anime.malId();
    }

    public void setJikanData(String name, Integer episodes, String imageUrl, Long malId) {
        nameField.setText(name);
        episodesField.setText(episodes != null ? String.valueOf(episodes) : "");
        imageUrlField.setText(imageUrl != null ? imageUrl : "");
        importMalId = malId;
    }

    @FXML
    public void onSave() {
        clearErrors();

        if (!validate()) return;

        try {
            AnimeRequestDto dto = new AnimeRequestDto(
                    nameField.getText().trim(),
                    Integer.parseInt(episodesField.getText().trim()),
                    seasonsField.getText().trim().isEmpty()
                            ? "1" : seasonsField.getText().trim(),
                    statusCombo.getValue(),
                    imageUrlField.getText().trim().isEmpty()
                            ? null : imageUrlField.getText().trim(),
                    starRating.getValue() > 0 ? starRating.getValue() : null
            );

            if (editAnime != null) {
                animeService.updateAnime(editAnime.id(), dto);
                if (!getSelectedCategoryIds().equals(
                        new HashSet<>(editAnime.categoryIds()))) {
                    animeService.assignCategories(editAnime.id(),
                            getSelectedCategoryIds().stream().toList());
                }
            } else {
                AnimeResponseDto created = animeService.saveAnime(dto);
                Set<Long> catIds = getSelectedCategoryIds();
                if (!catIds.isEmpty()) {
                    animeService.assignCategories(created.id(),
                            catIds.stream().toList());
                }
                if (importMalId != null) {
                    animeService.processJikanGenres(created.id(), importMalId);
                }
            }

            navigationService.navigateTo("/fxml/anime-list.fxml");
        } catch (DataIntegrityViolationException e) {
            showAlert("Error", "Ya existe un anime con ese nombre.");
        } catch (Exception e) {
            showAlert("Error", "No se pudo guardar: " + e.getMessage());
        }
    }

    @FXML
    public void onCancel() {
        navigationService.navigateTo("/fxml/anime-list.fxml");
    }

    @FXML
    public void onJikanSearch() {
        navigationService.navigateTo("/fxml/jikan-search.fxml");
    }

    private boolean validate() {
        boolean valid = true;

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            nameError.setText("Requerido");
            valid = false;
        }

        try {
            int ep = Integer.parseInt(episodesField.getText().trim());
            if (ep <= 0) {
                episodesError.setText("Debe ser > 0");
                valid = false;
            }
        } catch (NumberFormatException e) {
            episodesError.setText("Numero requerido");
            valid = false;
        }

        if (statusCombo.getValue() == null) {
            valid = false;
            showAlert("Validacion", "Selecciona un estado.");
        }

        return valid;
    }

    private void clearErrors() {
        nameError.setText("");
        episodesError.setText("");
    }

    private void clearForm() {
        nameField.clear();
        episodesField.clear();
        seasonsField.setText("1");
        statusCombo.setValue(null);
        starRating.setValue(0.0);
        imageUrlField.clear();
        editAnime = null;
        importMalId = null;
        titleLabel.setText("Nuevo Anime");
        clearErrors();

        for (CategoryCheckItem item : categoryList.getItems()) {
            item.setSelected(false);
        }
    }

    private void setupCategoryList() {
        List<CategoryResponseDto> categories = categoryService.findAll();
        List<CategoryCheckItem> items = categories.stream()
                .map(c -> new CategoryCheckItem(c, false))
                .collect(Collectors.toList());
        categoryList.setItems(FXCollections.observableArrayList(items));
        categoryList.setCellFactory(CheckBoxListCell.forListView(CategoryCheckItem::selectedProperty));
    }

    private void setSelectedCategories(List<Long> categoryIds) {
        Set<Long> ids = new HashSet<>(categoryIds);
        for (CategoryCheckItem item : categoryList.getItems()) {
            item.setSelected(ids.contains(item.getCategory().id()));
        }
    }

    private Set<Long> getSelectedCategoryIds() {
        return categoryList.getItems().stream()
                .filter(CategoryCheckItem::isSelected)
                .map(c -> c.getCategory().id())
                .collect(Collectors.toSet());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class CategoryCheckItem {
        private final CategoryResponseDto category;
        private final javafx.beans.property.BooleanProperty selected;

        public CategoryCheckItem(CategoryResponseDto category, boolean selected) {
            this.category = category;
            this.selected = new javafx.beans.property.SimpleBooleanProperty(selected);
        }

        public CategoryResponseDto getCategory() { return category; }
        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean selected) { this.selected.set(selected); }
        public javafx.beans.property.BooleanProperty selectedProperty() {
            return selected;
        }

        @Override
        public String toString() {
            return category.name();
        }
    }
}
