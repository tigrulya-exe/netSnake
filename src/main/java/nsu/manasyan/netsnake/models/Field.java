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

    public enum WallPass{
        LEFT,
        RIGHT,
        UP,
        DOWN,
        NOT_PASSED
    }

    private Cell[][] cells;

    public Field(int height, int width) {
        this.height = height;
        this.width = width;
        this.cells = new Cell[width][height];

        flush();
    }

    public Cell[][] getCells() {
        return cells;
    }

    public void flush(){
        for(int i = 0; i < width; ++i){
            for (int j = 0; j < height; ++j){
                cells[i][j] = Cell.FREE;
            }
        }
    }

    public void updateField(int x, int y, Cell cell){
        x = wrapX(x);
        y = wrapY(y);
        cells[x][y] = cell;
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

    public Coord wrap(Coord coord){
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

    public Cell getCell(int x, int y){
        return cells[x][y];
    }

    public Cell getCell(Coord coord){
        return cells[coord.getX()][coord.getY()];
    }

    public WallPass checkIfPassedThroughWallX(int x){
        return x < 0 ? WallPass.RIGHT : x >= width ? WallPass.LEFT : WallPass.NOT_PASSED;
    }

    public WallPass checkIfPassedThroughWallY(int y){
        return y < 0 ? WallPass.UP : y >= height ? WallPass.DOWN : WallPass.NOT_PASSED;
    }
}
