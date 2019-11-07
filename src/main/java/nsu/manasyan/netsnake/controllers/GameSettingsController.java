package nsu.manasyan.netsnake.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import nsu.manasyan.netsnake.NetSnakeApp;


public class GameSettingsController {
    @FXML
    Button backButton;

    public void backClicked(){
        Scene menu = SceneFactory.getInstance().getScene(SceneFactory.SceneType.MENU);
        NetSnakeApp.getStage().setScene(menu);
    }
}
