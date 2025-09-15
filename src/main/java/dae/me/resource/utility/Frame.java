package dae.me.resource.utility;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

public class Frame {
	public static Rectangle create (double ancho, double alto, Stop[] arrayColors, String colorEdge){ 
	    Rectangle frame = new Rectangle(ancho, alto);
	    frame.setArcWidth(30);
	    frame.setArcHeight(30);
	    frame.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, arrayColors));
	    frame.setStroke(Color.web(colorEdge));
	    
	    return frame;
	    }
}
