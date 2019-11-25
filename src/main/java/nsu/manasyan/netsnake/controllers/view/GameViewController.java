package nsu.manasyan.netsnake.controllers.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.gui.FieldCanvas;
import nsu.manasyan.netsnake.gui.NetSnakeApp;
import nsu.manasyan.netsnake.gui.ObjectDrawer;
import nsu.manasyan.netsnake.gui.SceneFactory;

import java.util.Map;

public class GameViewController {
    @FXML
    private Button exitButton;

    @FXML
    private GridPane scoreGrid;

    @FXML
    private Button newGameButton;

    @FXML
    private Button menuButton;

    private ClientController clientController = ClientController.getInstance();

    private int scoresCount = 0;

    private ClientController controller = ClientController.getInstance();

    public void menuClicked(){
        NetSnakeApp.getGameClient().stopCurrentGame();
        setScene(SceneFactory.SceneType.MENU);
    }

    public void newGameClicked(){
        NetSnakeApp.getGameClient().stopCurrentGame();
        setScene(SceneFactory.SceneType.NEW_GAME_SETTINGS);
    }

    public void exitClicked(){
        NetSnakeApp.getGameClient().stopCurrentGame();
        NetSnakeApp.getStage().close();
    }

    private static int counter = 0;

    public void initialize() {
        clientController.registerGameStateListener(this::onUpdate);

    }

    private void onUpdate(Map<Integer, Integer> scores) {
        Platform.runLater(() -> {
            FieldCanvas fieldCanvas = NetSnakeApp.getFieldCanvas();
            fieldCanvas.flush();
//            ObjectDrawer.drawField(MainController.getInstance().getField());
            clientController.getFoods().forEach(ObjectDrawer::drawFood);
            clientController.getSnakes().forEach(ObjectDrawer::drawSnake);

            scores.forEach(this::setScore);
        });
    }

    public void setScore(int id, int score){
        scoreGrid.getChildren().clear();

        setGridCell(new Label(Integer.toString(id)), 0, id );
        setGridCell(new Label("MASTER"), 1, id );
        setGridCell(new Label(Integer.toString(score)), 2, id );

    }

    private void setGridCell(Label label, int x, int y){
        label.getStyleClass().add("score-label");
        scoreGrid.add(label, x, y);
    }

    private void setScene(SceneFactory.SceneType sceneType){
        Scene menu = SceneFactory.getInstance().getScene(sceneType);
        NetSnakeApp.getStage().setScene(menu);
    }

}
