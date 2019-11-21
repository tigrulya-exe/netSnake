package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.models.MasterGameModel;
import nsu.manasyan.netsnake.models.Snake;
import nsu.manasyan.netsnake.proto.SnakesProto;
import nsu.manasyan.netsnake.proto.SnakesProto.Direction;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState.Coord;
import nsu.manasyan.netsnake.util.GameObjectBuilder;
import nsu.manasyan.netsnake.util.SnakeManipulator;

import java.util.List;

import static nsu.manasyan.netsnake.util.GameObjectBuilder.getCoord;

public class SnakesController {

    private MasterGameModel masterGameModel;

    private Field field;

    public void setMasterGameModel(MasterGameModel masterGameModel) {
        this.masterGameModel = masterGameModel;
        this.field = masterGameModel.getField();
    }

//    public static void useSnakeCoords(List<SnakesProto.GameState.Coord> points, SnakeManipulator manipulator) {
//        int currentX = points.get(0).getX();
//        int currentY = points.get(0).getY();
//        Field field = GameStateController.getInstance().getField();
//
//        for (int i = 1; i < points.size(); ++i) {
//            var point = points.get(i);
//            currentX = field.wrapX(currentX + point.getX());
//            currentY = field.wrapY(currentY + point.getY());
//
//            if (point.getX() == 0)
//                manipulator.manipulate(field.wrapY(currentY - point.getY()), currentY, currentX, true);
//            else
//                manipulator.manipulate(field.wrapX(currentX - point.getX()), currentX, currentY, false);
//        }
//
//    }

    public static void useSnakeCoords(List<SnakesProto.GameState.Coord> points, SnakeManipulator manipulator) {
        int currentX = points.get(0).getX();
        int currentY = points.get(0).getY();
        Field field = GameStateController.getInstance().getField();

        for (int i = 1; i < points.size(); ++i) {
            var point = points.get(i);
            currentX = currentX + point.getX();
            currentY = currentY + point.getY();

            if (point.getX() == 0)
                manipulator.manipulate(currentY - point.getY(), currentY, currentX, true);
            else
                manipulator.manipulate(currentX - point.getX(), currentX, currentY, false);
        }

    }

    public void moveSnake(Snake snake){
        Coord oldSecondCoord = snake.getPoints().get(1);
        switch (moveHead(snake) ){
            case FOOD:
                eatFood(snake);
                break;
            case HEAD:
            case SNAKE:
                setDead(snake);
                return;
            case FREE:
                moveTail(snake);
        }

        checkHead(snake, oldSecondCoord);
    }

    private Field.Cell moveHead(Snake snake) {
        Coord head = snake.getPoints().get(0);

        int headX = head.getX() + snake.getdX();
        int headY = head.getY() + snake.getdY();

        //TODO check field cell in (x,y)
        Field.Cell cettToReturn = field.getCell(field.wrapX(headX), field.wrapY(headY));

        snake.getPoints().set(0, getCoord(headX, headY));
        // TODO if new coord = tail coord -> return = FREE
        return cettToReturn;
    }

    private void moveTail(Snake snake) {
        List<Coord> points = snake.getPoints();

        int coordsSize = points.size();
        Coord oldTale = points.get(coordsSize - 1);

        int tailX = oldTale.getX();
        int tailY = oldTale.getY();

        tailX -= getOffsetDiff(tailX);
        tailY -= getOffsetDiff(tailY);

        points.set(coordsSize - 1, getCoord(tailX, tailY));

        if(coordsSize != 2 && tailX + tailY == 0){
            points.remove(coordsSize - 1);
        }
    }

    private int getOffsetDiff(int Offset){
        return Integer.compare(Offset, 0);
    }

    private void checkHead(Snake snake, Coord oldSecondCoord){
        List<Coord> points = snake.getPoints();
        Direction direction = snake.getHeadDirection();

        if(isRotateY(direction, oldSecondCoord.getX())){
            points.add(1, GameObjectBuilder.getCoord(0, -snake.getdY()));
            return;
        }

        if(isRotateX(direction, oldSecondCoord.getY())){
            points.add(1, GameObjectBuilder.getCoord(-snake.getdX(), 0));
            return;
        }

        int x = points.get(1).getX();
        int y = points.get(1).getY();

        points.set(1, GameObjectBuilder.getCoord(x - snake.getdX(), y - snake.getdY()));
    }

    private boolean isRotateY(Direction direction, int xOffset){
        return (direction == Direction.UP || direction == Direction.DOWN) && xOffset != 0;
    }

    private boolean isRotateX(Direction direction, int yOffset){
        return (direction == Direction.LEFT || direction == Direction.RIGHT) && yOffset != 0;
    }

    private void setDead(Snake snake){
        GameStateController.getInstance().gameOver(snake.getPlayerId());
    }

    private void eatFood(Snake snake){
        Coord head =  snake.getPoints().get(0);
        int x = head.getX();
        int y = head.getY();

        field.updateField(x, y, Field.Cell.FREE);
        GameStateController.getInstance().getModifiableFoods().removeIf(f -> f.getY() == y && f.getX() == x);
    }

}
