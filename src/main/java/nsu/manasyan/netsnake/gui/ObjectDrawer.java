package nsu.manasyan.netsnake.gui;

import javafx.scene.paint.Color;
import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.proto.SnakesProto;
import nsu.manasyan.netsnake.util.SnakePartManipulator;

import java.util.List;
import java.util.Map;


public class ObjectDrawer {

    private static SnakePartManipulator manipulator = SnakePartManipulator.getInstance();

    private static void drawSnakePart(int from, int to, int constCoord, boolean isVertical){
        FieldCanvas fieldCanvas =  NetSnakeApp.getFieldCanvas();
        Color snakeColor = ColorFactory.getInstance().getColor(Field.Cell.SNAKE);

        int min = (from < to) ? from : to;
        int max = (from > to) ? from : to;

        for(int coord = min; coord <= max; ++coord){
            if(isVertical)
                fieldCanvas.drawPoint(constCoord, coord, snakeColor);
            else
                fieldCanvas.drawPoint(coord, constCoord, snakeColor);
        }
    }


    public static void drawSnake(SnakesProto.GameState.Snake snake) {
        List<SnakesProto.GameState.Coord> points = snake.getPointsList();
        FieldCanvas fieldCanvas = NetSnakeApp.getFieldCanvas();
        Color headColor = ColorFactory.getInstance().getColor(Field.Cell.HEAD);

//        for(SnakesProto.GameState.Coord coord = )
        manipulator.useSnakeCoords(snake.getPointsList(), ObjectDrawer::drawSnakePart);
        fieldCanvas.drawPoint(points.get(0).getX(), points.get(0).getY(), headColor);
    }

    public static void drawField(Field field){
        Field.Cell[][] cells = field.getCells();
        FieldCanvas fieldCanvas =  NetSnakeApp.getFieldCanvas();

        for(int i = 0; i < field.getWidth(); ++i){
            for(int j = 0; j < field.getHeight(); ++j){
                if(cells[i][j] == Field.Cell.FREE)
                    continue;
                fieldCanvas.drawPoint(i,j, cells[i][j]);
            }
        }
    }


    public static void drawFood(SnakesProto.GameState.Coord food){
        FieldCanvas fieldCanvas =  NetSnakeApp.getFieldCanvas();
        fieldCanvas.drawPoint(food.getX(), food.getY(), Field.Cell.FOOD);
    }

    public static void drawScores(Map.Entry<Integer, Integer> score) {

    }
}
