package nsu.manasyan.netsnake.controllers.view;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import nsu.manasyan.netsnake.gui.NetSnakeApp;
import nsu.manasyan.netsnake.gui.SceneFactory;
import nsu.manasyan.netsnake.util.GameExecutorService;


public class MenuViewController {
    @FXML
    private Button newGameButton;

    @FXML
    private Button searchGamesButton;

    @FXML
    private Button exitButton;

    public void newGameClicked() {
        Scene newGameSettings = SceneFactory.getInstance().getScene(SceneFactory.SceneType.NEW_GAME_SETTINGS);
        NetSnakeApp.getStage().setScene(newGameSettings);
    }

    // TODO
    public void searchGamesClicked(){
        Scene gameSearch = SceneFactory.getInstance().getScene(SceneFactory.SceneType.GAME_SEARCH);
        NetSnakeApp.getStage().setScene(gameSearch);
    }

    public void exitClicked(){
        NetSnakeApp.getNetworkControllerBridge().stopCurrentGame();
        GameExecutorService.getExecutorService().shutdownNow();
        NetSnakeApp.getStage().close();
        System.exit(0);
    }
}
