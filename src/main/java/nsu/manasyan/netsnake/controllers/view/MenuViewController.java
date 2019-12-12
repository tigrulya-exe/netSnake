package nsu.manasyan.netsnake.controllers.view;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.gui.FieldCanvas;
import nsu.manasyan.netsnake.gui.NetSnakeApp;
import nsu.manasyan.netsnake.gui.SceneFactory;
import nsu.manasyan.netsnake.proto.SnakesProto;
import nsu.manasyan.netsnake.util.GameExecutorService;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

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

//    // TODO
//    public void searchGamesClicked(){
////        Scene gameSearch = SceneFactory.getInstance().getScene(SceneFactory.SceneType.GAME_SEARCH);
////        NetSnakeApp.getStage().setScene(gameSearch);
//        try {
//            Scene gameSearch = SceneFactory.getInstance().getScene(SceneFactory.SceneType.GAME);
////            ClientController.getInstance().joinGame( new InetSocketAddress(InetAddress.getByName("192.168.0.102"), 9192), false, SnakesProto.GameConfig.getDefaultInstance());
//            ClientController.getInstance().joinGame( new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 7777), false, SnakesProto.GameConfig.getDefaultInstance());
//            NetSnakeApp.getStage().setScene(gameSearch);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//    }

    // TODO
    public void searchGamesClicked() throws UnknownHostException {
//        Scene gameSearch = SceneFactory.getInstance().getScene(SceneFactory.SceneType.GAME_SEARCH);
//        NetSnakeApp.getStage().setScene(gameSearch);
        Scene gameSearch = SceneFactory.getInstance().getScene(SceneFactory.SceneType.GAME);
        ClientController.getInstance().joinGame(new InetSocketAddress(InetAddress.getByName("192.168.0.104"), 7777), false, SnakesProto.GameConfig.getDefaultInstance());
        initFieldCanvas(gameSearch, 40, 30);
        NetSnakeApp.getStage().setScene(gameSearch);
    }

    private void initFieldCanvas(Scene scene, int fieldWidth, int fieldHeight){

        Canvas canvas = new Canvas();
        canvas.setWidth(600);
        canvas.setHeight(600);

        FieldCanvas fieldCanvas = new FieldCanvas(canvas, fieldHeight, fieldWidth, getCellSize());

        NetSnakeApp.setFieldCanvas(fieldCanvas);

//        ObjectDrawer.drawField(MainController.getInstance().getField());
//        clientController.getFoods().forEach(ObjectDrawer::drawFood);
//        clientController.getSnakes().forEach(ObjectDrawer::drawSnake);

        List<Node> children =  ((AnchorPane) scene.getRoot()).getChildren();
        VBox gameBox = (VBox) children.get(3);
        gameBox.getChildren().clear();
        gameBox.getChildren().add(canvas);
        gameBox.setAlignment(Pos.CENTER);

    }

    private int getCellSize(){
        int tmpWidth = 40;
        int tmpHeight = 30;

        int min = (tmpWidth > tmpHeight) ? tmpWidth : tmpHeight;
        return 600/min;
    }

    public void exitClicked(){
        NetSnakeApp.getNetworkControllerBridge().stopCurrentGame();
        GameExecutorService.getExecutorService().shutdownNow();
        NetSnakeApp.getStage().close();
//        System.exit(0);
    }
}
