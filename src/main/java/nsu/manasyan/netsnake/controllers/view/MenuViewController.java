package nsu.manasyan.netsnake.controllers.view;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import nsu.manasyan.netsnake.gui.NetSnakeApp;
import nsu.manasyan.netsnake.gui.SceneFactory;


public class MenuViewController {
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
