package dae.me.resource.utility;

import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class Effect {
	public static void transitionX(Pane container, double seconds){
        TranslateTransition change = new TranslateTransition();
        change.setDuration(Duration.seconds(seconds));
        change.setNode(container);
        change.setToX(0);
        change.play();
    }
    
    public static void transitionY(Pane container, double seconds){
        TranslateTransition change = new TranslateTransition();
        change.setDuration(Duration.seconds(seconds));
        change.setNode(container);
        change.setToY(0);
        change.play();
    }
}
