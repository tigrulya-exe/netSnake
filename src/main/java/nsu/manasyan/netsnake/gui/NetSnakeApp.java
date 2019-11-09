package nsu.manasyan.netsnake.gui;

import javafx.animation.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class NetSnakeApp extends Application {

    private static Stage stage;

    public static Stage getStage() {
        return stage;
    }

    private void setStage(Stage stage) {
        NetSnakeApp.stage = stage;
    }

    private Parent setGraphics(AnchorPane root){
        Rectangle r = new Rectangle(50, 50, 50, 50);
        r.setFill(Color.WHITE);

        Rectangle r2 = new Rectangle(50, 50, 50, 50);
        r2.setFill(Color.RED);

        StackPane stackPane = new StackPane(root, r, r2);
        stackPane.setAlignment(Pos.TOP_RIGHT);

        TranslateTransition translate = new TranslateTransition(Duration.millis(1750));
        translate.setToX(0);
        translate.setToY(400);

        ParallelTransition transition = new ParallelTransition(r,translate);
        ParallelTransition transition2 = new ParallelTransition(r2,translate);

        transition.setCycleCount(Timeline.INDEFINITE);
        transition2.setCycleCount(Timeline.INDEFINITE);
//        transition.setAutoReverse(true);
        transition.play();
        transition2.play();

        return stackPane;
    }

    @Override
    public void start(Stage stage) throws IOException {
        setStage(stage);
//        Scene scene = SceneFactory.getInstance().getScene(SceneFactory.SceneType.MENU);
        FXMLLoader loader = new FXMLLoader();
        URL xmlUrl = getClass().getResource("/menu.fxml");
        loader.setLocation(xmlUrl);
        AnchorPane root = loader.load();
//        setGraphics(root);
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

    public void go(String[] args){
        launch(args);
    }

}