package nsu.manasyan.netsnake.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SceneFactory {

    private static final String MENU_PATH = "/menu.fxml";

    private static final String NEW_GAME_SETTINGS_PATH = "/newGameSettings.fxml";

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
    }

    private Scene initSceneType(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        URL xmlUrl = getClass().getResource(path);
        loader.setLocation(xmlUrl);
        Parent root = loader.load();
        return new Scene(root);
    }
}
