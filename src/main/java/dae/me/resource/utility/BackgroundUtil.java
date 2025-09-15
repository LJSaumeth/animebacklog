package dae.me.resource.utility;

import dae.me.resource.domain.URL;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;

public class BackgroundUtil {
	public static Background assign(String nameBackground, double width, double height){
	    String urlBackground = URL.URL_IMAGES + nameBackground;
	        Image img = new Image(urlBackground, width, height, false, true);
	        BackgroundImage backgroundReady = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, null);
	        return new Background(backgroundReady);
	    }
}
