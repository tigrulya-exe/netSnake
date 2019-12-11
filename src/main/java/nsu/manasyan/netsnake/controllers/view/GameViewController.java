package nsu.manasyan.netsnake.controllers.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import nsu.manasyan.netsnake.contexts.ScoreContext;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.gui.FieldCanvas;
import nsu.manasyan.netsnake.gui.NetSnakeApp;
import nsu.manasyan.netsnake.gui.ObjectDrawer;
import nsu.manasyan.netsnake.gui.SceneFactory;

import java.util.List;

public class GameViewController {
    @FXML
    private Button restartButton;

    @FXML
    private GridPane scoreGrid;

    @FXML
    private Button newGameButton;

    @FXML
    private Button menuButton;

    private ClientController clientController = ClientController.getInstance();

    private int scoresCount = 0;

    private ClientController controller = ClientController.getInstance();

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

    private static int counter = 0;

    public void initialize() {
        clientController.registerGameStateListener(this::onUpdate);
    }

    private void onUpdate(List<ScoreContext> scores) {
        Platform.runLater(() -> {
            FieldCanvas fieldCanvas = NetSnakeApp.getFieldCanvas();
            fieldCanvas.flush();
//            ObjectDrawer.drawField(MainController.getInstance().getField());
            clientController.getFoods().forEach(ObjectDrawer::drawFood);
            clientController.getSnakes().forEach(ObjectDrawer::drawSnake);

            for (int i = 0; i < scores.size(); i++) {
                setScore(i, scores.get(i));
            }
        });
    }

    public void setScore(int rank, ScoreContext score){
        scoreGrid.getChildren().clear();

        Label rankLabel = new Label(Integer.toString(rank));
        rankLabel.setUserData(score);
        rankLabel.setOnMouseClicked(this::onTick);

//        setGridCell(new Label(Integer.toString(rank)), 0, rank );
        setGridCell(rankLabel, 0, rank );
        setGridCell(new Label(score.getPlayerName()), 1, rank );
        setGridCell(new Label(Integer.toString(score.getPoints())), 2, rank );
    }

    public void onTick(MouseEvent event){
        Node source = (Node)event.getSource();
        System.out.println(((ScoreContext)source.getUserData()).getPoints());
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

}
