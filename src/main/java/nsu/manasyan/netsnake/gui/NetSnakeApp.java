package nsu.manasyan.netsnake.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import nsu.manasyan.netsnake.GameClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class NetSnakeApp extends Application {

    private static GameClient gameClient;

    private static Stage stage;

    private static RectanglesField rectanglesField;

    public static Stage getStage() {
        return stage;
    }

    private void setStage(Stage stage) {
        NetSnakeApp.stage = stage;
    }

    public static GameClient getGameClient() {
        return gameClient;
    }

//    private Parent setGraphics(AnchorPane root){
////        for(int i = 0 ; i < 10 ; ++i){
////            Rectangle r = new Rectangle(100 * i, (i % 2 == 0) ? 0 : 650, 50, 50);
////            r.setFill(Color.WHITE);
////            root.getChildren().addAll(r);
////            TranslateTransition translate = new TranslateTransition(Duration.millis(1750));
////            translate.setToX(0);
////            translate.setToY((i % 2 == 0) ? 350 : -350);
////            ParallelTransition transition = new ParallelTransition(r,translate);
////
////            transition.setCycleCount(Timeline.INDEFINITE);
////            transition.setAutoReverse(true);
////            transition.play();
////        }
//
//        for(int i = 0 ; i < 10 ; ++i){
//            Rectangle r = new Rectangle(100 * i, (i % 2 == 0) ? 0 : 650, 50, 50);
//            r.setFill(Color.WHITE);
//            root.getChildren().addAll(r);
//            FillTransition translate = new FillTransition(Duration.millis(1950));
//            translate.setShape(r);
//            translate.setToValue(Color.color(0.29,0.29,0.29));
//            ParallelTransition transition = new ParallelTransition(r,translate);
//
//            transition.setCycleCount(Timeline.INDEFINITE);
//            transition.setAutoReverse(true);
//            transition.play();
//        }
//
//        return root;
//    }

    @Override
    public void start(Stage stage) throws IOException {
        setStage(stage);
        gameClient = new GameClient();
//        Scene scene = SceneFactory.getInstance().getScene(SceneFactory.SceneType.MENU);
        FXMLLoader loader = new FXMLLoader();
        URL xmlUrl = getClass().getResource("/menu.fxml");
        loader.setLocation(xmlUrl);
        AnchorPane root = loader.load();
        Scene scene = new Scene(root);
//        Scene scene = new Scene(setGraphics(root));


        InputStream iconStream = getClass().getResourceAsStream("/snake.png");
        Image image = new Image(iconStream);
        stage.getIcons().add(image);

        stage.setTitle("NACHALO");
        stage.setWidth(1000);
        stage.setHeight(700);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static RectanglesField getRectanglesField() {
        return rectanglesField;
    }

    public static void setRectanglesField(RectanglesField rectanglesField) {
        NetSnakeApp.rectanglesField = rectanglesField;
    }

    public static void setScene(SceneFactory.SceneType sceneType){
        Scene menu = SceneFactory.getInstance().getScene(sceneType);
        stage.setScene(menu);
    }

    public void go(String[] args){
        launch(args);
    }

}