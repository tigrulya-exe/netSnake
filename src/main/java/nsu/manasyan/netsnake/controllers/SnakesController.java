package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.models.CurrentGameModel;
import nsu.manasyan.netsnake.models.Snake;
import nsu.manasyan.netsnake.proto.SnakesProto;
import nsu.manasyan.netsnake.proto.SnakesProto.Direction;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState.Coord;
import nsu.manasyan.netsnake.util.GameObjectBuilder;

import java.util.List;

public class SnakesController {

    private CurrentGameModel model;

    public void setModel(CurrentGameModel model) {
        this.model = model;
    }

    public void snakeStep(Snake snake){
        Coord oldSecondCoord = snake.getPoints().get(1);
        stepTail(snake);
        stepHead(snake);
        checkHead(snake, oldSecondCoord);
    }

    private void stepTail(Snake snake){
        List<Coord> coords = snake.getPoints();
        int coordsSize = coords.size();
        Coord newTail = getNewTail(coords.get(coordsSize - 1));
        coords.set(coordsSize - 1, newTail);

        if(coordsSize != 2 && newTail.getX() == 0 && newTail.getY() == 0){
            coords.remove(coordsSize - 1);
        }
    }

    private Coord getNewTail(Coord oldTale){
        int oldX = oldTale.getX();
        int oldY = oldTale.getY();

        return GameObjectBuilder.getCoord(oldX == 0 ? 0 : oldX - getSgn(oldX),
                oldY == 0 ? 0 : oldY - getSgn(oldY));
    }

    private void stepHead(Snake snake){
        List<Coord> coords = snake.getPoints();
        Coord head = coords.get(0);
        Direction direction = snake.getHeadDirection();

        int x = head.getX();
        int y = head.getY();

        x -= getSgnX(direction);
        y -= getSgnY(direction);

        //TODO check field cell in (x,y)

        coords.set(0, GameObjectBuilder.getCoord(x, y));
    }

    private void checkHead(Snake snake, Coord oldSecondCoord){
        List<Coord> coords = snake.getPoints();
        Direction direction = snake.getHeadDirection();

        if(isRotateX(oldSecondCoord.getX(), direction)){
            coords.add(1, GameObjectBuilder.getCoord(0, getSgnY(direction)));
            return;
        }

        if(isRotateY(oldSecondCoord.getY(), direction)){
            coords.add(1, GameObjectBuilder.getCoord(getSgnX(direction), 0));
            return;
        }

        setNodeAfterHead(coords, direction);
    }

    private boolean isRotateX(int xOffset, Direction direction){
        return (direction == Direction.UP || direction == Direction.DOWN) && xOffset != 0;
    }

    private boolean isRotateY(int yOffset, Direction direction){
        return (direction == Direction.LEFT || direction == Direction.RIGHT) && yOffset != 0;
    }

    private void setNodeAfterHead(List<Coord> coords, SnakesProto.Direction direction) {
        int x = coords.get(1).getX();
        int y = coords.get(1).getY();

        coords.set(1, GameObjectBuilder.getCoord(x + getSgnX(direction),
                y + getSgnY(direction)));
    }

    private int getSgn(int val){
        return (val < 0) ? -1 : 1;
    }

    private int getSgnX(Direction direction){
        return (direction == Direction.LEFT) ? 1 : (direction == Direction.RIGHT) ? -1 : 0;
    }

    private int getSgnY(Direction direction){
        return (direction == Direction.UP) ? 1 : (direction == Direction.DOWN) ? -1 : 0;
    }
}
