package dae.me.javafx.component;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

public class StarRating extends HBox {

    private static final String STAR_EMPTY = "\u2606";
    private static final String STAR_HALF = "\u2605\uFE0F";
    private static final String STAR_FULL = "\u2605";

    private final Label[] stars = new Label[5];
    private final DoubleProperty value = new SimpleDoubleProperty(0.0);
    private Consumer<Double> onChanged;
    private boolean editable = true;

    public StarRating() {
        super(2);
        setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < 5; i++) {
            final int index = i;
            Label star = new Label(STAR_EMPTY);
            star.setStyle("-fx-font-size: 26px; -fx-text-fill: #FF5FCF; -fx-cursor: hand;");
            star.setOnMouseClicked(e -> handleClick(index, e));
            star.setOnMouseMoved(e -> {
                if (editable && star.getStyle().contains("hand")) {
                    star.setStyle("-fx-font-size: 28px; -fx-text-fill: #FAEB92; -fx-cursor: hand;");
                }
            });
            star.setOnMouseExited(e -> updateDisplay());
            stars[i] = star;
            getChildren().add(star);
        }

        value.addListener((obs, old, newVal) -> updateDisplay());
        updateDisplay();
    }

    public double getValue() {
        return value.get();
    }

    public void setValue(double v) {
        value.set(clamp(v));
    }

    public DoubleProperty valueProperty() {
        return value;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        String cursor = editable ? "hand" : "default";
        for (Label star : stars) {
            star.setStyle(star.getStyle().replaceAll("-fx-cursor: [a-z]+;", "-fx-cursor: " + cursor + ";"));
        }
    }

    public void setOnChanged(Consumer<Double> onChanged) {
        this.onChanged = onChanged;
    }

    private void handleClick(int index, MouseEvent e) {
        if (!editable) return;

        double starWidth = stars[index].getWidth();
        double xPos = e.getX();

        double newValue;
        if (xPos < starWidth * 0.4) {
            newValue = index + 0.5;
        } else {
            newValue = index + 1.0;
        }

        if (Math.abs(value.get() - newValue) < 0.01) {
            newValue = 0.0;
        }

        value.set(newValue);
        if (onChanged != null) {
            onChanged.accept(newValue);
        }
    }

    private double clamp(double v) {
        v = Math.round(v * 2.0) / 2.0;
        return Math.max(0.0, Math.min(5.0, v));
    }

    private void updateDisplay() {
        double v = value.get();
        for (int i = 0; i < 5; i++) {
            if (v >= i + 1) {
                stars[i].setText(STAR_FULL);
            } else if (v >= i + 0.5) {
                stars[i].setText(STAR_FULL);
                stars[i].setStyle(stars[i].getStyle().replaceAll("-fx-text-fill: [^;]+;",
                        "-fx-text-fill: #FF5FCF;"));
            } else {
                stars[i].setText(STAR_EMPTY);
            }
        }
    }
}
