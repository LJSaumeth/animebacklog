package dae.me.javafx.controller;

import dae.me.dto.AnimeResponseDto;
import dae.me.javafx.SpringFxWeaver;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainController implements NavigationService {

    @FXML
    private StackPane contentArea;

    private final SpringFxWeaver weaver;
    private final ApplicationContext context;
    private final JikanDataHolder jikanDataHolder;
    private Node currentView;
    private String currentFxml;

    public MainController(SpringFxWeaver weaver, ApplicationContext context,
                           JikanDataHolder jikanDataHolder) {
        this.weaver = weaver;
        this.context = context;
        this.jikanDataHolder = jikanDataHolder;
    }

    @FXML
    public void onGalleryClick() {
        navigateTo("/fxml/anime-gallery.fxml");
    }

    @FXML
    public void onAnimesClick() {
        navigateTo("/fxml/anime-list.fxml");
    }

    @FXML
    public void onNewAnimeClick() {
        navigateToAnimeForm(null);
    }

    @FXML
    public void onCategoriesClick() {
        navigateTo("/fxml/category-manager.fxml");
    }

    @FXML
    public void onJikanImportClick() {
        navigateTo("/fxml/jikan-search.fxml");
    }

    @Override
    public void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(weaver);
            Node view = loader.load();
            currentView = view;
            currentFxml = fxmlPath;
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            contentArea.getChildren().setAll(
                    new Label("Error loading view: " + e.getMessage()));
        }
    }

    @Override
    public void navigateToAnimeForm(AnimeResponseDto anime) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/anime-form.fxml"));
            loader.setControllerFactory(weaver);
            Node view = loader.load();
            AnimeFormController controller = loader.getController();
            if (anime != null) {
                controller.setEditMode(anime);
            } else if (jikanDataHolder.hasData()) {
                jikanDataHolder.consume(controller);
            }
            currentView = view;
            currentFxml = "/fxml/anime-form.fxml";
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            contentArea.getChildren().setAll(
                    new Label("Error loading view: " + e.getMessage()));
        }
    }

    @Override
    public void refreshCurrentView() {
        if (currentFxml != null) {
            navigateTo(currentFxml);
        }
    }
}
