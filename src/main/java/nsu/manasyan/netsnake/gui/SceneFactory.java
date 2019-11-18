package nsu.manasyan.netsnake.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import nsu.manasyan.netsnake.controllers.GameStateController;
import nsu.manasyan.netsnake.proto.SnakesProto;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SceneFactory {

    private static final String MENU_PATH = "/menu.fxml";

    private static final String NEW_GAME_SETTINGS_PATH = "/gameConfig.fxml";

    private static final String GAME_PATH = "/game.fxml";

    public enum SceneType {
        MENU,
        NEW_GAME_SETTINGS,
        GAME,
        GAME_SEARCH
    }

    private static Map<SceneType, Scene> scenes = new HashMap<>();

    private SceneFactory(){
        try {
            initSceneTypes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class SingletonHelper{
        private static final SceneFactory sceneFactory = new SceneFactory();
    }

    public static SceneFactory getInstance(){
        return SingletonHelper.sceneFactory;
    }

    public Scene getScene(SceneType sceneType){
        return scenes.get(sceneType);
    }

    private void initSceneTypes() throws IOException {
        scenes.put(SceneType.MENU, initSceneType(MENU_PATH));
        scenes.put(SceneType.NEW_GAME_SETTINGS, initSceneType(NEW_GAME_SETTINGS_PATH));
        scenes.put(SceneType.GAME, initGameScene());
    }

    private Scene initSceneType(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        URL xmlUrl = getClass().getResource(path);
        loader.setLocation(xmlUrl);
        Parent root = loader.load();
        return new Scene(root);
    }

    private Scene initGameScene() throws IOException {
        Scene scene = initSceneType(GAME_PATH);
        scene.setOnKeyPressed((ke) -> {
            SnakesProto.Direction direction;
            if ((direction = getDirection(ke.getCode())) != null)
                GameStateController.getInstance().registerDirection(direction);
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
