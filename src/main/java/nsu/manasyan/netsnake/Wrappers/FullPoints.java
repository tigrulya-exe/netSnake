package nsu.manasyan.netsnake.Wrappers;

import nsu.manasyan.netsnake.proto.SnakesProto;
import nsu.manasyan.netsnake.util.GameObjectBuilder;
import nsu.manasyan.netsnake.util.SnakePartManipulator;

import java.util.ArrayList;
import java.util.List;

public class FullPoints {
    private List<SnakesProto.GameState.Coord> fullPoints = new ArrayList<>();

    private int height;

    private int width;

    private int id;

    public FullPoints(Snake snake, int height, int width) {
        this.height = height;
        this.width = width;
        this.id = snake.getPlayerId();
        fromKeyPoints(snake.getPoints());
    }

    private SnakePartManipulator manipulator = SnakePartManipulator.getInstance();

    public void fromKeyPoints(List<SnakesProto.GameState.Coord> keyPoints){
        manipulator.useSnakeCoords(keyPoints, this::addCoords);
        fullPoints.remove(keyPoints.get(0));
        fullPoints.add(keyPoints.get(0));
        System.out.println(toString());
    }

    private void addCoords(int from, int to, int constCoord, boolean isVertical){
        int min = (from < to) ? from : to;
        int max = (from > to) ? from : to;

        for(int coord = min; coord <= max; ++coord){
            SnakesProto.GameState.Coord newCoord = isVertical ?
                    getCoord(constCoord, coord) : getCoord(coord, constCoord);

//            if(!fullPoints.contains(newCoord))
                fullPoints.add(newCoord);
        }
    }

    private SnakesProto.GameState.Coord getCoord(int x, int y){
        int wrappedX = (x + width) % width;
        int wrappedY = (y + height) % height;

        return GameObjectBuilder.getCoord(wrappedX, wrappedY);
    }

    public int getId() {
        return id;
    }

    public String toString(){
        StringBuilder toReturn = new StringBuilder();
        fullPoints.forEach(f-> toReturn.append("[").append(f.getX()).append(" : ").append(f.getY()).append("]\n"));
        return toReturn.toString();
    }

    public List<SnakesProto.GameState.Coord> getFullPoints() {
        return fullPoints;
    }

}
