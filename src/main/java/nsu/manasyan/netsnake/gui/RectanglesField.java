package nsu.manasyan.netsnake.gui;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import nsu.manasyan.netsnake.models.Field;

public class RectanglesField {
    private int cellSize;

    private int height;

    private int width;

    private GridPane gridPane = new GridPane();

    public RectanglesField(int cellSize, int height, int width) {
        this.cellSize = cellSize;
        this.height = height;
        this.width = width;

        gridPane.setLayoutX(52);
        gridPane.setLayoutY(18);
        initGrid();
    }

    public void updateGrid(int x, int y, Field.Cell cell){
        Color color = ColorFactory.getInstance().getColor(cell);
        ((Rectangle)gridPane.getChildren().get(x * height + y)).setFill(color);
    }

    public GridPane getGridPane(){
        return gridPane;
    }

    private void initGrid(){
        Color freeColor = ColorFactory.getInstance().getColor(Field.Cell.FREE);

        for(int i = 0; i <  width; ++i){
            for (int j = 0; j < height; ++j){
                Rectangle rectangle = new Rectangle(cellSize, cellSize);
                rectangle.setFill(freeColor);
                gridPane.add(rectangle, i, j);
            }
        }
    }
}
