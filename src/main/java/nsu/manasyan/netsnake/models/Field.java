package nsu.manasyan.netsnake.models;

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
        this.field = new Cell[height][width];
    }

    public Cell[][] getField() {
        return field;
    }

    public void flush(){
        for(int i = 0; i < height; ++i){
            for (int j = 0; j < width; ++j){
                field[i][j] = Cell.FREE;
            }
        }
    }

    public void updateField(int x, int y, Cell cell){
        field[x][y] = cell;
    }
}
