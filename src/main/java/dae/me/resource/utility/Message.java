package dae.me.resource.utility;

import javafx.scene.control.Alert;
import javafx.stage.Window;

public class Message {
	public static void modal(Alert.AlertType type, Window window, String title, String text){
	    Alert msg = new Alert(type);
	    msg.setTitle(title);
	    msg.setHeaderText(null);
	    msg.setContentText(text);
	    msg.initOwner(window);
	    msg.show();
	    }
}
