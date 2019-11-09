package nsu.manasyan.netsnake.gui;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;


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
