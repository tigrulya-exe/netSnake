package nsu.manasyan.netsnake.models;

import nsu.manasyan.netsnake.proto.SnakesProto.GameState.Coord;

public class Field {
    private final int height;

    private final int width;

    public enum Cell{
        SNAKE,
        FOOD,
        HEAD,
        FREE
    }

    private Cell[][] field;

    public Field(int height, int width) {
        this.height = height;
        this.width = width;
        this.field = new Cell[width][height];

        flush();
    }

    public Cell[][] getField() {
        return field;
    }

    public void flush(){
        for(int i = 0; i < width; ++i){
            for (int j = 0; j < height; ++j){
                field[i][j] = Cell.FREE;
            }
        }
    }

    public void updateField(int x, int y, Cell cell){
        x = wrapX(x);
        y = wrapY(y);
        field[x][y] = cell;
    }

    public void updateField(Coord coord, Cell cell){
        updateField(coord.getX(), coord.getY(), cell);
    }

    public int wrapX(int x){
        return (x + width) % width;
    }

    public int wrapY(int y){
        return (y + height) % height;
    }

    private Coord wrapCoord(Coord coord){
        return Coord.newBuilder()
                .setX(wrapX(coord.getX()))
                .setY(wrapY(coord.getY()))
                .build();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
