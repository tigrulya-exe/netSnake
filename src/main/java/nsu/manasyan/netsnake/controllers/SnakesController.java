package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.Wrappers.Snake;
import nsu.manasyan.netsnake.proto.SnakesProto;
import nsu.manasyan.netsnake.proto.SnakesProto.Direction;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState.Coord;
import nsu.manasyan.netsnake.util.SnakePartManipulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static nsu.manasyan.netsnake.models.Field.Cell.*;
import static nsu.manasyan.netsnake.util.GameObjectBuilder.getCoord;

public class SnakesController {

    private static final int HEAD_INDEX = 0;

    private MasterController controller;

    private Field field;

    private boolean isCurrentSnakeDead = false;

    private SnakesProto.GameState.Coord currentSnakeHead;

    public void setField(Field field) {
        this.field = field;
    }

    public void moveSnake(Snake snake){
        Coord oldPointAfterHead = snake.getPoints().get(1);
        Coord newHead = getNewHeadCoord(snake);

        snake.getPoints().set(0, newHead);
        checkPointAfterHead(snake, oldPointAfterHead);

        if(getNewHeadCell(newHead, snake.getPoints()) == FOOD)
            eatFood(snake);
        else
            moveTail(snake);

//        switch (getNewHeadCell(newHead, snake.getPoints())){
//            case FOOD:
//                eatFood(snake);
//                break;
//            case FREE:
//                moveTail(snake);
//                break;
//            default:
//                setDead(snake);
//        }
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
        controller.addScore(snake.getPlayerId(), 1);
    }

    public void setController(MasterController mainController) {
        this.controller = mainController;
    }

    public void updateSnakePart(int from, int to, int constCoord, boolean isVertical){
        int min = (from < to) ? from : to;
        int max = (from > to) ? from : to;

        for(int coord = min; coord <= max; ++coord){
            if(isVertical)
                field.updateField(constCoord, coord, Field.Cell.SNAKE);
            else
                field.updateField(coord, constCoord, Field.Cell.SNAKE);
        }
    }

    public void checkSnakes(Collection<Snake> snakes) {
        ArrayList<Snake> otherSnakes = new ArrayList<>(snakes);

        for (var iter = snakes.iterator(); iter.hasNext(); ) {
            Snake snake = iter.next();
            otherSnakes.remove(snake);
            if(snakeBytesSnake(snake, otherSnakes)) {
                controller.gameOver(snake.getPlayerId());
                snakes.remove(snake);
                continue;
            }
            otherSnakes.add(snake);
        }
    }

    private boolean snakeBytesSnake(Snake snake, Collection<Snake> otherSnakes){
        currentSnakeHead = snake.getPoints().get(HEAD_INDEX);
        isCurrentSnakeDead = false;

        System.out.println("HEAD - [ " + currentSnakeHead.getX() + " , " + currentSnakeHead.getY() + " ]");
        for (var iter = otherSnakes.iterator(); iter.hasNext(); ) {
            Snake otherSnake = iter.next();
            var otherSnakePoints = otherSnake.getPoints();

            SnakePartManipulator.getInstance().useSnakeCoords(otherSnakePoints, this::containsHead);
            if(isCurrentSnakeDead){
                return true;
            }

//            if(otherSnake.getPoints().contains(head)){
//                return true;
//            }
        }
        ArrayList<Coord> otherPoints = new ArrayList<>(snake.getPoints());
        Coord secondCoord = otherPoints.get(1);
        Coord newSecondCoord = getCoord(field.wrapX(currentSnakeHead.getX() + secondCoord.getX()),
                field.wrapY(currentSnakeHead.getY() + secondCoord.getY()));
        otherPoints.set(1, newSecondCoord);

        SnakePartManipulator.getInstance().useSnakeCoords(otherPoints.subList(1, otherPoints.size()), this::containsHead);

        return isCurrentSnakeDead;
    }


    private void containsHead(int from, int to, int constant, boolean isVertical){
        if(isVertical)
            containsVertically(from, to, constant);
        else
            containsHorizontally(from, to, constant);
    }

    private void containsVertically(int yFrom, int yTo, int x){
        int min = (yFrom < yTo) ? yFrom : yTo;
        int max = (yFrom > yTo) ? yFrom : yTo;

        for(int y = min; y < max; ++y){
            System.out.println("[X: " + x + " , Y: " + y +"]");
            if(currentSnakeHead.getY() == field.wrapY(y) && currentSnakeHead.getX() == x){
                isCurrentSnakeDead = true;
                return;
            }
        }
    }

    private void containsHorizontally(int xFrom, int xTo, int y){
        int min = (xFrom < xTo) ? xFrom : xTo;
        int max = (xFrom > xTo) ? xFrom : xTo;

        for(int x = min; x <= max; ++x){
            System.out.println("[X: " + x + " , Y: " + y +"]");
            if(currentSnakeHead.getX() == field.wrapX(x) && currentSnakeHead.getY() == y){
                isCurrentSnakeDead = true;
                return;
            }
        }
    }

}
