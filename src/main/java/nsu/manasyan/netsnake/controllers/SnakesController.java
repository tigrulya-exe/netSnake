package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.models.Snake;
import nsu.manasyan.netsnake.proto.SnakesProto.Direction;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState.Coord;

import java.util.List;

import static nsu.manasyan.netsnake.models.Field.Cell.FREE;
import static nsu.manasyan.netsnake.util.GameObjectBuilder.getCoord;

public class SnakesController {

    private GameStateController controller;

    private Field field;

    public void setField(Field field) {
        this.field = field;
    }

    public void moveSnake(Snake snake){
        Coord oldPointAfterHead = snake.getPoints().get(1);
        Coord newHead = getNewHeadCoord(snake);

        snake.getPoints().set(0, newHead);
        checkPointAfterHead(snake, oldPointAfterHead);

        switch (getNewHeadCell(newHead, snake.getPoints())){
            case FOOD:
                eatFood(snake);
                break;
            case FREE:
                moveTail(snake);
                break;
            default:
                setDead(snake);
        }
    }

    private Field.Cell getNewHeadCell (Coord newHead, List<Coord> points){
        return newHead.equals(getTailCoord(points)) ? FREE : field.getCell(newHead);
    }

    private Coord getTailCoord(List<Coord> points){
        int x = 0, y = 0;

        for(var point : points) {
            x += point.getX();
            y += point.getY();
        }

        return getCoord(field.wrapX(x), field.wrapY(y));
    }

    private Coord getNewHeadCoord(Snake snake){
        Coord head = snake.getPoints().get(0);

        int headX = field.wrapX(head.getX() + snake.getdX());
        int headY = field.wrapY(head.getY() + snake.getdY());

        return getCoord(headX, headY);
    }

    private void moveTail(Snake snake) {
        List<Coord> points = snake.getPoints();

        int coordsSize = points.size();
        Coord oldTale = points.get(coordsSize - 1);

        field.updateField(oldTale.getX(), oldTale.getY(), FREE);
        int tailX = getNewTailOffset(oldTale.getX());
        int tailY = getNewTailOffset(oldTale.getY());

        points.set(coordsSize - 1, getCoord(tailX, tailY));

        if(tailX + tailY == 0){
            points.remove(points.size() - 1);
        }
    }

    private int getNewTailOffset(int oldOffset){
        return oldOffset - Integer.compare(oldOffset, 0);
    }

    private void checkPointAfterHead(Snake snake, Coord oldSecondCoord){
        List<Coord> points = snake.getPoints();
        Direction direction = snake.getHeadDirection();

        if(isRotateY(direction, oldSecondCoord.getX())){
            points.add(1, getCoord(0, -snake.getdY()));
            return;
        }

        if(isRotateX(direction, oldSecondCoord.getY())){
            points.add(1, getCoord(-snake.getdX(), 0));
            return;
        }

        points.set(1, getCoord(points.get(1).getX() - snake.getdX(),
                points.get(1).getY() - snake.getdY()));
    }

    private boolean isRotateY(Direction direction, int xOffset){
        return (direction == Direction.UP || direction == Direction.DOWN) && xOffset != 0;
    }

    private boolean isRotateX(Direction direction, int yOffset){
        return (direction == Direction.LEFT || direction == Direction.RIGHT) && yOffset != 0;
    }

    private void setDead(Snake snake){
        controller.gameOver(snake.getPlayerId());
    }

    private void eatFood(Snake snake){
        Coord head =  snake.getPoints().get(0);
        int x = head.getX();
        int y = head.getY();

        field.updateField(x, y, FREE);
        controller.getModifiableFoods().removeIf(f -> f.getY() == y && f.getX() == x);
    }

    public void setController(GameStateController gameStateController) {
        this.controller = gameStateController;
    }
}
