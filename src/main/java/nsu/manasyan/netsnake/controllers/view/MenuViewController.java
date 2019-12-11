package nsu.manasyan.netsnake.controllers.view;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.gui.NetSnakeApp;
import nsu.manasyan.netsnake.gui.SceneFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class MenuViewController {
    @FXML
    private Button newGameButton;

    @FXML
    private Button searchGamesButton;

    @FXML
    private Button exitButton;

    public void newGameClicked(){
        Scene newGameSettings = SceneFactory.getInstance().getScene(SceneFactory.SceneType.NEW_GAME_SETTINGS);
        NetSnakeApp.getStage().setScene(newGameSettings);
    }

    // TODO
    public void searchGamesClicked(){
//        Scene gameSearch = SceneFactory.getInstance().getScene(SceneFactory.SceneType.GAME_SEARCH);
//        NetSnakeApp.getStage().setScene(gameSearch);
        try {
            Scene gameSearch = SceneFactory.getInstance().getScene(SceneFactory.SceneType.GAME);
            ClientController.getInstance().joinGame( new InetSocketAddress(InetAddress.getByName("192.168.0.102"), 9192), false);
            NetSnakeApp.getStage().setScene(gameSearch);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void exitClicked(){
        NetSnakeApp.getGameClient().stopCurrentGame();
        NetSnakeApp.getStage().close();
    }
}
