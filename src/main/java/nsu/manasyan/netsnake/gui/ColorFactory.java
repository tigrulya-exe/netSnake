package nsu.manasyan.netsnake.gui;

import javafx.scene.paint.Color;
import nsu.manasyan.netsnake.models.Field;

import java.util.HashMap;
import java.util.Map;

public class ColorFactory {
    private ColorFactory(){
        initColors();
    }

    private Map<Field.Cell, Color> colors = new HashMap<>();

    private static class SingletonHelper{
        private static final ColorFactory RECTANGLE_FACTORY = new ColorFactory();
    }

    public static ColorFactory getInstance(){
        return SingletonHelper.RECTANGLE_FACTORY;
    }

    private void initColors() {
        colors.put(Field.Cell.FREE, Color.BLACK);
        colors.put(Field.Cell.FOOD, Color.GREEN);
        colors.put(Field.Cell.SNAKE, Color.CORNSILK);
        colors.put(Field.Cell.HEAD, Color.RED);

    }

    public Color getColor(Field.Cell cell){
        return colors.get(cell);
    }

}
