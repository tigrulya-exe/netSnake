package nsu.manasyan.netsnake.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import nsu.manasyan.netsnake.gui.NetSnakeApp;
import nsu.manasyan.netsnake.gui.RectanglesField;
import nsu.manasyan.netsnake.gui.SceneFactory;
import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.proto.SnakesProto;

import java.util.List;

public class GameViewController {
    @FXML
    private Button exitButton;

    @FXML
    private Button newGameButton;

    @FXML
    private Button menuButton;

    private RectanglesField rectanglesField = NetSnakeApp.getRectanglesField();

    private GameStateController controller = GameStateController.getInstance();

    public void menuClicked(){
        setScene(SceneFactory.SceneType.MENU);
    }

    public void newGameClicked(){
        setScene(SceneFactory.SceneType.NEW_GAME_SETTINGS);
    }

    public void exitClicked(){
        NetSnakeApp.getStage().close();
    }

    private void setScene(SceneFactory.SceneType sceneType){
        Scene menu = SceneFactory.getInstance().getScene(sceneType);
        NetSnakeApp.getStage().setScene(menu);
    }


    // TODO move to
    private static void drawSnakePart(int from, int to, int constCoord, boolean isVertical){
        RectanglesField field =  NetSnakeApp.getRectanglesField();
        int min = (from < to) ? from : to;
        int max = (from > to) ? from : to;

        for(int coord = min; coord < max; ++coord){
            if(isVertical)
                field.updateGrid(constCoord, coord, Field.Cell.SNAKE);
            else
                field.updateGrid(coord, constCoord, Field.Cell.SNAKE);
        }
    }


    public static void drawSnake(SnakesProto.GameState.Snake snake){
        List<SnakesProto.GameState.Coord> points = snake.getPointsList();
        RectanglesField field = NetSnakeApp.getRectanglesField();

        int currentX = points.get(0).getX();
        int currentY = points.get(0).getY();
        field.updateGrid(currentX, currentY, Field.Cell.HEAD);

        for (int i = 1; i < points.size(); ++i){
            var point = points.get(i);
            currentX += point.getX();
            currentY += point.getY();

            if(point.getX() == 0)
                drawSnakePart(currentY - point.getY(), currentY, currentX, true);
            else
                drawSnakePart(currentX - point.getX(), currentX, currentY, false);
        }
    }
}
