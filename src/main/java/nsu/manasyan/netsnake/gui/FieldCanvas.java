package nsu.manasyan.netsnake.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import nsu.manasyan.netsnake.models.Field;


public class FieldCanvas {

    private Canvas canvas;

    private GraphicsContext context;

    private int fieldHeight;

    private int fieldWidth;

    private int squareSize;

    public FieldCanvas(Canvas canvas, int fieldHeight, int fieldWidth, int squareSize) {
        this.fieldHeight = fieldHeight;
        this.fieldWidth = fieldWidth;
        this.squareSize = squareSize;
        this.canvas = canvas;
        this.context = canvas.getGraphicsContext2D();
    }


    private void initCanvas(int height, int width) {
        canvas.setHeight(height);
        canvas.setWidth(width);

        context = canvas.getGraphicsContext2D();
    }

    public GraphicsContext getContext() {
        return context;
    }


    public void drawPoint(int x, int y, Color color){
        context.setFill(color);
        x = (x + fieldWidth) % fieldWidth;
        y = (y + fieldHeight) % fieldHeight;

        context.fillRect(x * squareSize, y  * squareSize, squareSize, squareSize);
    }

    public void drawPoint(int x, int y, Field.Cell cell){
        drawPoint(x, y, ColorFactory.getInstance().getColor(cell));
    }

    public void flush(){
        context.setFill(Color.BLACK);
        context.fillRect(0,0, fieldWidth * squareSize, fieldHeight * squareSize);
    }
}
