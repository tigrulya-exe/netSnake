package nsu.manasyan.netsnake.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import nsu.manasyan.netsnake.gui.NetSnakeApp;
import nsu.manasyan.netsnake.gui.RectanglesField;
import nsu.manasyan.netsnake.gui.SceneFactory;
import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.proto.SnakesProto;

public class GameConfigViewController {
    private static final int FIELD_BOX_WIDTH = 630;

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

    private GameStateController controller = GameStateController.getInstance();

    public void backClicked(){
        NetSnakeApp.setScene(SceneFactory.SceneType.MENU);
    }

    public void startClicked(){
        int fieldWidth = getInt(width.getText());
        int fieldHeight = getInt(height.getText());

        NetSnakeApp.getGameClient().startNewGame(initConfig());

        Scene game = SceneFactory.getInstance().getScene(SceneFactory.SceneType.GAME);
        generateField(game, fieldWidth, fieldHeight);
        controller.registerGameStateListener(this::onUpdate);
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

    private void onUpdate(SnakesProto.GameState gameState) {
        Platform.runLater(() -> {
            RectanglesField rectanglesField = NetSnakeApp.getRectanglesField();
                    rectanglesField.flush();
                    controller.getFoods().forEach(f -> {
                        rectanglesField.updateGrid(f.getX(), f.getY(), Field.Cell.FOOD);
                    });

                    controller.getSnakes().forEach(GameViewController::drawSnake);
                    }
        );
    }

    private void generateField(Scene scene, int fieldWidth, int fieldHeight){
        BorderPane borderPane = new BorderPane();
        borderPane.setLayoutX(52);
        borderPane.setLayoutY(18);

        RectanglesField rectanglesField = new RectanglesField(getCellSize(), fieldHeight, fieldWidth);
        NetSnakeApp.setRectanglesField(rectanglesField);

        controller.getFoods().forEach(f -> {
            rectanglesField.updateGrid(f.getX(), f.getY(), Field.Cell.FOOD);
        });

        controller.getSnakes().forEach(GameViewController::drawSnake);

        borderPane.setCenter(rectanglesField.getGridPane());
//        ((AnchorPane) scene.getRoot()).getChildren().addAll(rectanglesField.getGridPane());
        ((AnchorPane) scene.getRoot()).getChildren().addAll(borderPane);
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
