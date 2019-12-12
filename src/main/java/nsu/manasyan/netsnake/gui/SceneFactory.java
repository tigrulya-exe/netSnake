package nsu.manasyan.netsnake.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.proto.SnakesProto;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SceneFactory {

    private interface SceneCreator{
        Scene create();
    }

    private static final String MENU_PATH = "/menu.fxml";

    private static final String NEW_GAME_SETTINGS_PATH = "/gameConfig.fxml";

    private static final String GAME_PATH = "/game.fxml";

    private static final String  GAME_SEARCH_PATH = "/availableGames.fxml";

    public enum SceneType {
        MENU,
        NEW_GAME_SETTINGS,
        GAME,
        GAME_SEARCH
    }

    private static Map<SceneType, SceneCreator> sceneCreators = new HashMap<>();

    private SceneFactory(){
        initSceneTypes();
    }

    private static class SingletonHelper{
        private static final SceneFactory sceneFactory = new SceneFactory();
    }

    public static SceneFactory getInstance(){
        return SingletonHelper.sceneFactory;
    }

    public Scene getScene(SceneType sceneType){
        return sceneCreators.get(sceneType).create();
    }

    private void initSceneTypes() {
        sceneCreators.put(SceneType.MENU, () -> initSceneType(MENU_PATH));
        sceneCreators.put(SceneType.GAME_SEARCH, () -> initSceneType(GAME_SEARCH_PATH));
        sceneCreators.put(SceneType.NEW_GAME_SETTINGS, () -> initSceneType(NEW_GAME_SETTINGS_PATH));
        sceneCreators.put(SceneType.GAME, this::initGameScene);
    }

    private Scene createScene(String path) throws IOException {
        return initSceneType(path);
    }

    private Scene initSceneType(String path){
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setController(null);
            loader.setRoot(null);

            URL xmlUrl = getClass().getResource(path);
            loader.setLocation(xmlUrl);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Scene(root);
    }

    private Scene initGameScene() {
        Scene scene = initSceneType(GAME_PATH);

        scene.setOnKeyPressed((ke) -> {
            SnakesProto.Direction direction;
            if ((direction = getDirection(ke.getCode())) != null)
                ClientController.getInstance().registerDirection(direction);
            });
        return  scene;
    }

    private SnakesProto.Direction getDirection(KeyCode keyCode){
        switch (keyCode){
            case W:
                return SnakesProto.Direction.UP;
            case A:
                return SnakesProto.Direction.LEFT;
            case D:
                return SnakesProto.Direction.RIGHT;
            case S:
                return SnakesProto.Direction.DOWN;
            default:
                return null;
        }
    }
}
