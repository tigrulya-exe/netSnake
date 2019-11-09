package nsu.manasyan.netsnake.gui;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import nsu.manasyan.netsnake.gui.NetSnakeApp;
import nsu.manasyan.netsnake.gui.SceneFactory;


public class GameSettingsController {
    @FXML
    Button backButton;

    public void backClicked(){
        Scene menu = SceneFactory.getInstance().getScene(SceneFactory.SceneType.MENU);
        NetSnakeApp.getStage().setScene(menu);
    }
}
