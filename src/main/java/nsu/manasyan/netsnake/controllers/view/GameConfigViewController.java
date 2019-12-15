package nsu.manasyan.netsnake.controllers.view;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.gui.*;
import nsu.manasyan.netsnake.proto.SnakesProto;

public class GameConfigViewController {

    @FXML
    private Button backButton;

    @FXML
    private Button startButton;

    @FXML
    private TextField width;

    @FXML
    private TextField height;

    @FXML
    private TextField staticFood;

    @FXML
    private TextField foodPerPlayer;

    @FXML
    private TextField stateDelayMs;

    @FXML
    private TextField deadFoodProb;

    @FXML
    private TextField pingDelayMs;

    @FXML
    private TextField nodeTimeoutMs;

    private ClientController clientController = ClientController.getInstance();

    public void backClicked(){
        NetSnakeApp.setScene(SceneFactory.SceneType.MENU);
    }

    public void startClicked(){
        SnakesProto.GameConfig config = initConfig();
        clientController.startNewGame(config);

        Scene game = SceneFactory.getInstance().getScene(SceneFactory.SceneType.GAME);
        NetSnakeApp.getStage().setScene(game);
    }

    private SnakesProto.GameConfig initConfig(){
        return SnakesProto.GameConfig.newBuilder()
                .setWidth(getInt(width.getText()))
                .setHeight(getInt(height.getText()))
                .setFoodStatic(getInt(staticFood.getText()))
                .setFoodPerPlayer(getFloat(foodPerPlayer.getText()))
                .setStateDelayMs(getInt(stateDelayMs.getText()))
                .setDeadFoodProb(getFloat(deadFoodProb.getText()))
                .setPingDelayMs(getInt(pingDelayMs.getText()))
                .setNodeTimeoutMs(getInt(nodeTimeoutMs.getText()))
                .build();
    }

    private int getInt(String str){
        return Integer.parseInt(str);
    }

    private float getFloat(String str){
        return Float.parseFloat(str);
    }
}
