package dae.me.javafx.controller;

import dae.me.dto.CategoryRequestDto;
import dae.me.dto.CategoryResponseDto;
import dae.me.service.CategoryService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Component;

@Component
public class CategoryManagerController {

    @FXML
    private TableView<CategoryResponseDto> categoryTable;

    @FXML
    private TableColumn<CategoryResponseDto, String> colName;

    @FXML
    private TableColumn<CategoryResponseDto, String> colColor;

    @FXML
    private TextField nameField;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private Label nameError;

    private final CategoryService categoryService;
    private final ObservableList<CategoryResponseDto> categoryList = FXCollections.observableArrayList();

    private CategoryResponseDto selected;

    public CategoryManagerController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @FXML
    public void initialize() {
        categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colName.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().name()));

        colColor.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().color()));

        colColor.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String color, boolean empty) {
                super.updateItem(color, empty);
                if (empty || color == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(color);
                    setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
                }
            }
        });

        categoryTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> selectCategory(newVal));

        loadCategories();
    }

    private void selectCategory(CategoryResponseDto cat) {
        selected = cat;
        if (cat != null) {
            nameField.setText(cat.name());
            if (cat.color() != null) {
                try {
                    colorPicker.setValue(Color.web(cat.color()));
                } catch (Exception e) {
                    colorPicker.setValue(null);
                }
            } else {
                colorPicker.setValue(null);
            }
        }
    }

    @FXML
    public void onAdd() {
        nameError.setText("");
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            nameError.setText("Requerido");
            return;
        }

        String color = null;
        if (colorPicker.getValue() != null) {
            color = toHexString(colorPicker.getValue());
        }

        try {
            categoryService.create(new CategoryRequestDto(name.trim(), color));
            loadCategories();
            onClear();
        } catch (Exception e) {
            nameError.setText("Ya existe o es invalido");
        }
    }

    @FXML
    public void onUpdate() {
        if (selected == null) return;
        nameError.setText("");

        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            nameError.setText("Requerido");
            return;
        }

        String color = null;
        if (colorPicker.getValue() != null) {
            color = toHexString(colorPicker.getValue());
        }

        try {
            categoryService.update(selected.id(), new CategoryRequestDto(name.trim(), color));
            loadCategories();
        } catch (Exception e) {
            nameError.setText("Error al actualizar");
        }
    }

    @FXML
    public void onDelete() {
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar categoria");
        confirm.setHeaderText("Eliminar \"" + selected.name() + "\"?");
        confirm.setContentText("Se desasignara de todos los animes.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                categoryService.delete(selected.id());
                onClear();
                loadCategories();
            }
        });
    }

    @FXML
    public void onClear() {
        nameField.clear();
        colorPicker.setValue(null);
        nameError.setText("");
        selected = null;
        categoryTable.getSelectionModel().clearSelection();
    }

    private void loadCategories() {
        categoryList.setAll(categoryService.findAll());
        categoryTable.setItems(categoryList);
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
