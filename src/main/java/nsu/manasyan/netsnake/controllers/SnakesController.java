package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.models.Snake;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState.Coord;
import nsu.manasyan.netsnake.util.GameObjectBuilder;

import java.util.List;

public class SnakesController {

    public void snakeStep(Snake snake){
        List<Coord> coords = snake.getPoints();
        int coordsSize = coords.size();

        Coord newTale = getNewTail(coords.get(coordsSize - 1));
        stepHead(snake);
        coords.remove(coordsSize - 1);
        if (newTale.getX() == 0 && newTale.getY() == 0){
            Coord previous = coords.get(coords.size() - 1);
            newTale = GameObjectBuilder.getCoord(previous.getX(), previous.getY());
//            coords.remove(coords.size() - 1);
        }
//        coords.removeIf(c -> c.equals(newTale));
        coords.add(newTale);
    }

    private Coord getNewTail(Coord oldTale){
        int oldX = oldTale.getX();
        int oldY = oldTale.getY();

        return GameObjectBuilder.getCoord(oldX == 0 ? 0 : --oldX,
                oldY == 0 ? 0 : --oldY);
    }

    private void stepHead(Snake snake){
        List<Coord> coords = snake.getPoints();
        Coord head = coords.get(0);

        int x = head.getX();
        int y = head.getY();

        // TODO tmp :)
        switch (snake.getHeadDirection()){
            case UP:
                --y;
                break;
            case DOWN:
                ++y;
                break;
            case LEFT:
                --x;
                break;
            case RIGHT:
                ++x;
        }

        coords.set(0, GameObjectBuilder.getCoord(x, y));
        setNodeAfterHead(coords);

    }

    private void setNodeAfterHead(List<Coord> coords){
        if(coords.size() != 2){
            int x = coords.get(1).getX();
            int y = coords.get(1).getY();
            coords.set(1, GameObjectBuilder.getCoord(x == 0 ? 0 : ++x,
                    y == 0 ? 0 : ++y));
        }
    }
}
