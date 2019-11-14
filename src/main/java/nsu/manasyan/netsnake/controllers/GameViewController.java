package nsu.manasyan.netsnake.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import nsu.manasyan.netsnake.gui.NetSnakeApp;
import nsu.manasyan.netsnake.gui.RectanglesField;
import nsu.manasyan.netsnake.gui.SceneFactory;

public class GameViewController {
    @FXML
    private Button exitButton;

    @FXML
    private Button newGameButton;

    @FXML
    private Button menuButton;

    private RectanglesField rectanglesField;

    public void menuClicked(){
        setScene(SceneFactory.SceneType.MENU);
    }

    public void newGameClicked(){
        setScene(SceneFactory.SceneType.NEW_GAME_SETTINGS);
    }

    public void exitClicked(){
        NetSnakeApp.getStage().close();
    }

    private void setScene(SceneFactory.SceneType sceneType){
        Scene menu = SceneFactory.getInstance().getScene(sceneType);
        NetSnakeApp.getStage().setScene(menu);
    }
}
