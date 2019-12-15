package nsu.manasyan.netsnake.controllers.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import nsu.manasyan.netsnake.contexts.ScoreContext;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.gui.FieldCanvas;
import nsu.manasyan.netsnake.gui.NetSnakeApp;
import nsu.manasyan.netsnake.gui.ObjectDrawer;
import nsu.manasyan.netsnake.gui.SceneFactory;
import nsu.manasyan.netsnake.proto.SnakesProto;

import javax.print.DocFlavor;
import java.util.List;

public class GameViewController {
    private static final int FIELD_BOX_SIDE = 600;

    @FXML
    private Button restartButton;

    @FXML
    private GridPane scoreGrid;

    @FXML
    private Canvas canvas;

    @FXML
    private Button newGameButton;

    @FXML
    private Button menuButton;

    @FXML
    private VBox fieldBox;

    private ClientController clientController = ClientController.getInstance();

    private int scoresCount = 0;

    public void menuClicked() {
        stopGame();
        setScene(SceneFactory.SceneType.MENU);
    }

    public void newGameClicked(){
        stopGame();
        setScene(SceneFactory.SceneType.NEW_GAME_SETTINGS);
    }

    public void restartClicked(){
        scoreGrid.getChildren().clear();
        NetSnakeApp.getNetworkControllerBridge().restartCurrentGame();
    }

    public void initialize() {
        initFieldCanvas(clientController.getConfig());
        clientController.registerConfigListener(c ->
            Platform.runLater(() -> initFieldCanvas(c))
        );
        clientController.registerGameStateListener(this::onUpdate);
    }

    private void onUpdate(List<ScoreContext> scores) {
        Platform.runLater(() -> {
            FieldCanvas fieldCanvas = NetSnakeApp.getFieldCanvas();
            fieldCanvas.flush();
            clientController.getFoods().forEach(ObjectDrawer::drawFood);
            clientController.getFullPoints().forEach(ObjectDrawer::drawSnake);
            scoreGrid.getChildren().clear();
            for (int i = 0; i < scores.size(); i++) {
                setScore(i, scores.get(i));
            }
        });
    }

    public void setScore(int rank, ScoreContext score){
        Label rankLabel = new Label(Integer.toString(rank));
        setGridCell(rankLabel, 0, rank );
        setGridCell(new Label(score.getPlayerName()), 1, rank );
        setGridCell(new Label(Integer.toString(score.getPoints())), 2, rank );
    }

    private void stopGame(){
        NetSnakeApp.getNetworkControllerBridge().stopCurrentGame();
        scoreGrid.getChildren().clear();
    }

    private void setGridCell(Label label, int x, int y){
        label.getStyleClass().add("score-label");
        scoreGrid.add(label, x, y);
    }

    private void setScene(SceneFactory.SceneType sceneType){
        Scene menu = SceneFactory.getInstance().getScene(sceneType);
        NetSnakeApp.getStage().setScene(menu);
    }

    public void initFieldCanvas(SnakesProto.GameConfig gameConfig){
        int cellSize = getCellSize(gameConfig.getWidth(), gameConfig.getHeight());
        Platform.runLater(() -> {
            canvas.setWidth(cellSize * gameConfig.getWidth());
            canvas.setHeight(cellSize * gameConfig.getHeight());
            canvas.getGraphicsContext2D().setFill(Color.RED);

            ClientController clientController = ClientController.getInstance();

            FieldCanvas fieldCanvas = new FieldCanvas(canvas, gameConfig.getHeight(), gameConfig.getWidth(), cellSize);
            NetSnakeApp.setFieldCanvas(fieldCanvas);

            if(clientController.getState() == null)
                return;
//        ObjectDrawer.drawField(MainController.getInstance().getField());
            clientController.getFoods().forEach(ObjectDrawer::drawFood);
//            clientController.getSnakes().forEach(ObjectDrawer::drawSnake);
            clientController.getFullPoints().forEach(ObjectDrawer::drawSnake);
        });
    }

    private int getCellSize(int width, int height){
        int min = (width > height) ? width : height;
        return FIELD_BOX_SIDE /min;
    }

}
