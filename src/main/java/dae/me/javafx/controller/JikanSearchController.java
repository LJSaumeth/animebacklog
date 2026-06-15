package dae.me.javafx.controller;

import dae.me.dto.jikan.JikanAnimeItemDto;
import dae.me.service.JikanService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class JikanSearchController {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<JikanAnimeItemDto> resultsList;

    @FXML
    private HBox loadingBox;

    @FXML
    private Label errorLabel;

    private final JikanService jikanService;
    private final NavigationService navigationService;
    private final JikanDataHolder dataHolder;

    private Timer debounceTimer;

    public JikanSearchController(JikanService jikanService,
                                  NavigationService navigationService,
                                  JikanDataHolder dataHolder) {
        this.jikanService = jikanService;
        this.navigationService = navigationService;
        this.dataHolder = dataHolder;
    }

    @FXML
    public void initialize() {
        resultsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(JikanAnimeItemDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String text = String.format("%s  |  Episodios: %s  |  %s",
                            item.title(),
                            item.episodes() != null ? item.episodes() : "?",
                            item.status() != null ? item.status() : "");
                    setText(text);
                }
            }
        });

        resultsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                selectAndFillForm();
            }
        });

        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (debounceTimer != null) debounceTimer.cancel();
            debounceTimer = new Timer(true);
            debounceTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(JikanSearchController.this::doSearch);
                }
            }, 500);
        });
    }

    @FXML
    public void onSearch() {
        if (debounceTimer != null) debounceTimer.cancel();
        doSearch();
    }

    @FXML
    public void onBack() {
        navigationService.navigateTo("/fxml/anime-form.fxml");
    }

    private void selectAndFillForm() {
        JikanAnimeItemDto selected = resultsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        dataHolder.setData(
                selected.title(),
                selected.episodes(),
                selected.imageUrl(),
                selected.malId());

        navigationService.navigateToAnimeForm(null);
    }

    private void doSearch() {
        String query = searchField.getText();
        if (query == null || query.isBlank()) return;

        setLoading(true);
        errorLabel.setText("");

        new Thread(() -> {
            try {
                List<JikanAnimeItemDto> results = jikanService.searchAnime(query);
                Platform.runLater(() -> {
                    resultsList.setItems(FXCollections.observableArrayList(results));
                    setLoading(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    resultsList.setItems(FXCollections.observableArrayList(
                            Collections.emptyList()));
                    errorLabel.setText("Jikan no disponible. Intenta de nuevo.");
                    setLoading(false);
                });
            }
        }).start();
    }

    private void setLoading(boolean loading) {
        loadingBox.setVisible(loading);
        loadingBox.setManaged(loading);
    }
}
