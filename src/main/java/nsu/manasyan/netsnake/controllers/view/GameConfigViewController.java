package nsu.manasyan.netsnake.controllers.view;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.gui.*;
import nsu.manasyan.netsnake.proto.SnakesProto;

import java.util.List;

public class GameConfigViewController {
    private static final int FIELD_BOX_WIDTH = 600;

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
        int fieldWidth = getInt(width.getText());
        int fieldHeight = getInt(height.getText());

        NetSnakeApp.getGameClient().startNewGame(initConfig());

        Scene game = SceneFactory.getInstance().getScene(SceneFactory.SceneType.GAME);
        initFieldCanvas(game, fieldWidth, fieldHeight);
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


    private void initFieldCanvas(Scene scene, int fieldWidth, int fieldHeight){

        Canvas canvas = new Canvas();
        canvas.setWidth(600);
        canvas.setHeight(600);

        FieldCanvas fieldCanvas = new FieldCanvas(canvas, fieldHeight, fieldWidth, getCellSize());

        NetSnakeApp.setFieldCanvas(fieldCanvas);

//        ObjectDrawer.drawField(MainController.getInstance().getField());
        clientController.getFoods().forEach(ObjectDrawer::drawFood);
        clientController.getSnakes().forEach(ObjectDrawer::drawSnake);

        List<Node> children =  ((AnchorPane) scene.getRoot()).getChildren();
        VBox gameBox = (VBox) children.get(3);
        gameBox.getChildren().clear();
        gameBox.getChildren().add(canvas);
        gameBox.setAlignment(Pos.CENTER);

    }

    private int getCellSize(){
        int tmpWidth = getInt(width.getText());
        int tmpHeight = getInt(height.getText());

        int min = (tmpWidth > tmpHeight) ? tmpWidth : tmpHeight;
        return FIELD_BOX_WIDTH/min;
    }

    private int getInt(String str){
        return Integer.parseInt(str);
    }

    private float getFloat(String str){
        return Float.parseFloat(str);
    }

}
