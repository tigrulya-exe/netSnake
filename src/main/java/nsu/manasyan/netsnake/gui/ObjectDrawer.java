package nsu.manasyan.netsnake.gui;

import nsu.manasyan.netsnake.Wrappers.FullPoints;
import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.proto.SnakesProto;


public class ObjectDrawer {
    public static void drawFood(SnakesProto.GameState.Coord food){
        FieldCanvas fieldCanvas =  NetSnakeApp.getFieldCanvas();
        fieldCanvas.drawPoint(food.getX(), food.getY(), Field.Cell.FOOD);
    }


    public static void drawSnake(FullPoints fullPoints) {
        FieldCanvas fieldCanvas = NetSnakeApp.getFieldCanvas();
        fullPoints.getFullPoints().forEach(f -> fieldCanvas.drawPoint(f.getX(), f.getY(), Field.Cell.SNAKE));
        SnakesProto.GameState.Coord head = fullPoints.getFullPoints().get(fullPoints.getFullPoints().size() - 1);
        fieldCanvas.drawPoint(head.getX(), head.getY(), Field.Cell.HEAD);
    }
}
