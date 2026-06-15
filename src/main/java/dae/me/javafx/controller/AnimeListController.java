package dae.me.javafx.controller;

import dae.me.dto.AnimeResponseDto;
import dae.me.dto.CategoryResponseDto;
import dae.me.dto.PagedResponseDto;
import dae.me.entity.Anime.AnimeStatus;
import dae.me.service.AnimeService;
import dae.me.service.CategoryService;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import dae.me.javafx.component.StarRating;
import javafx.scene.input.MouseButton;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Component
public class AnimeListController {

    @FXML
    private TableView<AnimeResponseDto> animeTable;

    @FXML
    private TableColumn<AnimeResponseDto, String> colName;

    @FXML
    private TableColumn<AnimeResponseDto, Integer> colEpisodes;

    @FXML
    private TableColumn<AnimeResponseDto, String> colSeasons;

    @FXML
    private TableColumn<AnimeResponseDto, String> colStatus;

    @FXML
    private TableColumn<AnimeResponseDto, Double> colRating;

    @FXML
    private TableColumn<AnimeResponseDto, List<Long>> colCategories;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private TextField minRatingField;

    @FXML
    private TextField maxRatingField;

    private final AnimeService animeService;
    private final CategoryService categoryService;
    private final NavigationService navigationService;

    private Timer debounceTimer;
    private final ObservableList<AnimeResponseDto> animeList = FXCollections.observableArrayList();

    public AnimeListController(AnimeService animeService,
                                CategoryService categoryService,
                                NavigationService navigationService) {
        this.animeService = animeService;
        this.categoryService = categoryService;
        this.navigationService = navigationService;
    }

    @FXML
    public void initialize() {
        animeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns();
        setupStatusFilter();
        setupSearchDebounce();
        setupContextMenu();
        setupDoubleClick();
        loadData();
    }

    private void setupColumns() {
        colName.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().name()));

        colEpisodes.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().episodes()));

        colSeasons.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().seasons() != null
                        ? data.getValue().seasons() : "-"));

        colStatus.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().status().name()));

        colRating.setCellValueFactory(data -> {
            Double rating = data.getValue().rating();
            return new SimpleObjectProperty<>(rating);
        });

        colRating.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    StarRating sr = new StarRating();
                    sr.setValue(rating);
                    sr.setEditable(false);
                    setGraphic(sr);
                    setText(null);
                }
            }
        });

        colCategories.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().categoryIds()));

        colCategories.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            private final javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(4);

            {
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(List<Long> categoryIds, boolean empty) {
                super.updateItem(categoryIds, empty);
                if (empty || categoryIds == null || categoryIds.isEmpty()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    container.getChildren().clear();
                    for (Long catId : categoryIds) {
                        Label catLabel = createCategoryLabel(catId);
                        if (catLabel != null) {
                            container.getChildren().add(catLabel);
                        }
                    }
                    setGraphic(container);
                    setText(null);
                }
            }
        });
    }

    private javafx.scene.control.Label createCategoryLabel(Long id) {
        try {
            CategoryResponseDto cat = categoryService.findById(id);
            javafx.scene.control.Label label = new javafx.scene.control.Label(cat.name());
            String bgColor = (cat.color() != null && !cat.color().isBlank())
                    ? cat.color() : "#0f3460";
            String textColor = isLightColor(bgColor) ? "#000000" : "#FFFFFF";
            label.setStyle("-fx-font-size: 11px; -fx-padding: 1 6 1 6; "
                    + "-fx-background-radius: 3; "
                    + "-fx-background-color: " + bgColor + "; "
                    + "-fx-text-fill: " + textColor + ";");
            return label;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isLightColor(String hex) {
        try {
            String h = hex.replace("#", "");
            if (h.length() == 3) {
                h = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1)
                        + h.charAt(2) + h.charAt(2);
            }
            int r = Integer.parseInt(h.substring(0, 2), 16);
            int g = Integer.parseInt(h.substring(2, 4), 16);
            int b = Integer.parseInt(h.substring(4, 6), 16);
            double luminance = 0.299 * r + 0.587 * g + 0.114 * b;
            return luminance > 150;
        } catch (Exception e) {
            return false;
        }
    }

    private void setupStatusFilter() {
        statusFilter.getItems().addAll("Todos", "WATCHING", "WATCHED", "ON_HOLD", "DROPPED", "PLANNING_TO_WATCH");
        statusFilter.setValue("Todos");
    }

    private void setupSearchDebounce() {
        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (debounceTimer != null) {
                debounceTimer.cancel();
            }
            debounceTimer = new Timer(true);
            debounceTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(AnimeListController.this::loadData);
                }
            }, 400);
        });
    }

    private void setupContextMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem editItem = new MenuItem("Editar");
        editItem.setOnAction(e -> openEditForm());

        MenuItem deleteItem = new MenuItem("Eliminar");
        deleteItem.setOnAction(e -> deleteSelected());

        MenuItem rateItem = new MenuItem("Puntuar");
        rateItem.setOnAction(e -> showRateDialog());

        menu.getItems().addAll(editItem, rateItem, deleteItem);

        animeTable.setContextMenu(menu);
        animeTable.setOnContextMenuRequested(e -> {
            if (animeTable.getSelectionModel().getSelectedItem() == null) {
                menu.hide();
            }
        });
    }

    private void setupDoubleClick() {
        animeTable.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                openEditForm();
            }
        });
    }

    @FXML
    public void onSearch() {
        loadData();
    }

    @FXML
    public void onNewAnime() {
        navigationService.navigateToAnimeForm(null);
    }

    @FXML
    public void onImportFromJikan() {
        navigationService.navigateTo("/fxml/jikan-search.fxml");
    }

    private void loadData() {
        String search = searchField.getText();
        if (search != null && search.isBlank()) search = null;

        AnimeStatus status = null;
        String sel = statusFilter.getValue();
        if (sel != null && !"Todos".equals(sel)) {
            status = AnimeStatus.valueOf(sel);
        }

        Double minRating = parseDoubleOrNull(minRatingField.getText());
        Double maxRating = parseDoubleOrNull(maxRatingField.getText());

        PagedResponseDto<AnimeResponseDto> result = animeService.findAll(
                search, status, minRating, maxRating,
                null, "name", "asc", 0, 500);

        animeList.setAll(result.content());
        animeTable.setItems(animeList);
    }

    private void openEditForm() {
        AnimeResponseDto selected = animeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            navigationService.navigateToAnimeForm(selected);
        }
    }

    private void deleteSelected() {
        AnimeResponseDto selected = animeTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminacion");
        confirm.setHeaderText("Eliminar \"" + selected.name() + "\"?");
        confirm.setContentText("Esta accion no se puede deshacer.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                animeService.deleteById(selected.id());
                loadData();
            }
        });
    }

    private void showRateDialog() {
        AnimeResponseDto selected = animeTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        javafx.scene.control.Dialog<Double> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Puntuar anime");
        dialog.setHeaderText("Puntuar \"" + selected.name() + "\"");

        javafx.scene.control.ButtonType rateButton = new javafx.scene.control.ButtonType(
                "Guardar", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(rateButton,
                javafx.scene.control.ButtonType.CANCEL);

        StarRating starRating = new StarRating();
        if (selected.rating() != null) {
            starRating.setValue(selected.rating());
        }
        dialog.getDialogPane().setContent(starRating);

        dialog.setResultConverter(btn -> {
            if (btn == rateButton) return starRating.getValue();
            return null;
        });

        dialog.showAndWait().ifPresent(score -> {
            if (score <= 0) {
                animeService.removeRating(selected.id());
            } else {
                animeService.rateAnime(selected.id(), score);
            }
            loadData();
        });
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
