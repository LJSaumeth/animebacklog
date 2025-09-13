package dae.me.resource.domain;

import javafx.stage.Screen;

public class Configuration {
	public final static int WIDTH_APP = (int) (Screen.getPrimary().getBounds().getWidth() * 0.6);
    public final static int HEIGHT_APP = (int) (Screen.getPrimary().getBounds().getHeight() * 0.7);
    
    
    public final static String ICON_APP = "iconApp.png";
    public final static String ICON_DELETE = "iconDelete.png";
    public final static String ICON_EDIT = "iconEdit.png";
    public final static String ICON_BACK = "iconBack.png";
    public final static String ICON_NEXT = "iconNext.png";
    
    public final static Double ABOUT_WIDTH = 290.0;
    public final static Double ABOUT_HEIGHT = 290.0;
    
    public final static Double HEADER_HEIGHT_PERCENTAGE = 0.1;
    public final static String HEADER_COLOR_BACKGROUND = "-fx-background-color: transparent";
     
}
