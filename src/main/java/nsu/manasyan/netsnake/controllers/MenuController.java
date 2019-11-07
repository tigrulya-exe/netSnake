package nsu.manasyan.netsnake.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import nsu.manasyan.netsnake.NetSnakeApp;


public class MenuController {
    @FXML
    private Button newGameButton;

    @FXML
    private Button exitButton;

    public void newGameClicked(){
        Scene newGameSettings = SceneFactory.getInstance().getScene(SceneFactory.SceneType.NEW_GAME_SETTINGS);
        NetSnakeApp.getStage().setScene(newGameSettings);
    }

    public void exitClicked(){
        NetSnakeApp.getStage().close();
    }
}
