package nsu.manasyan.netsnake.Wrappers;

import nsu.manasyan.netsnake.proto.SnakesProto;

import nsu.manasyan.netsnake.proto.SnakesProto.GameState.Snake.*;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState.Coord;

import java.util.ArrayList;
import java.util.List;

import static nsu.manasyan.netsnake.proto.SnakesProto.Direction;
import static nsu.manasyan.netsnake.util.GameObjectBuilder.getCoord;
import static nsu.manasyan.netsnake.util.GameObjectBuilder.getGameState;

public class Snake {
    private static final SnakesProto.Direction DEFAULT_DIRECTION = Direction.LEFT;

    private SnakeState snakeState = SnakeState.ALIVE;

    private int playerId;

    private List<Coord> points = new ArrayList<>();

    private SnakesProto.Direction headDirection = DEFAULT_DIRECTION;

    private int dX = 0;

    private int dY = 0;

    public Snake(int playerId){
        this.playerId = playerId;
        points.add(getCoord(0,0));
        points.add(getCoord(3,0));
    }

    public Snake(SnakesProto.GameState.Snake protoSnake){
        this.points = new ArrayList<>(protoSnake.getPointsList());
        this.setHeadDirection(protoSnake.getHeadDirection());
        this.playerId = protoSnake.getPlayerId();
        this.snakeState = protoSnake.getState();
    }

    public Snake(int playerId, List<Coord> points){
        this.playerId = playerId;
        this.points = points;
    }

    public static SnakesProto.Direction getDefaultDirection() {
        return DEFAULT_DIRECTION;
    }

    public SnakeState getSnakeState() {
        return snakeState;
    }

    public void setSnakeState(SnakeState snakeState) {
        this.snakeState = snakeState;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public List<Coord> getPoints() {
        return points;
    }

    public void setPoints(List<Coord> points) {
        this.points = points;
    }

    public SnakesProto.Direction getHeadDirection() {
        return headDirection;
    }

    public void setHeadDirection(SnakesProto.Direction headDirection) {
        this.headDirection = headDirection;
        dX = (headDirection == Direction.LEFT) ? -1 : (headDirection == Direction.RIGHT) ? 1 : 0;
        dY = (headDirection == Direction.UP) ? -1 : (headDirection == Direction.DOWN) ? 1 : 0;
    }

    public SnakesProto.GameState.Snake toProtoSnake(){
        return SnakesProto.GameState.Snake.newBuilder()
                .setState(snakeState)
                .setHeadDirection(headDirection)
                .addAllPoints(points)
                .setPlayerId(playerId)
                .build();
    }

    public int getdX() {
        return dX;
    }

    public int getdY() {
        return dY;
    }

//    public void move(Field field){
//        Coord oldSecondCoord = points.get(1);
//        if(moveHead(field) != Field.Cell.FOOD) {
//            moveTail(field);
//        } else System.out.println("HERE");
//        checkHead(oldSecondCoord);
//    }
//
//    private Field.Cell moveHead(Field field) {
//        Coord head = points.get(0);
//
//        int headX = field.wrapX(head.getX() + dX);
//        int headY = field.wrapY(head.getY() + dY);
//
//        //TODO check field cell in (x,y)
//        Field.Cell cettToReturn = field.getCell(headX, headY);
//
//        points.set(0, getCoord(headX, headY));
//        return cettToReturn;
//    }
//
//    private void moveTail(Field field) {
//        int coordsSize = points.size();
//        Coord oldTale = points.get(coordsSize - 1);
//
//        int tailX = oldTale.getX();
//        int tailY = oldTale.getY();
//
//        tailX -= getOffsetDiff(tailX);
//        tailY -= getOffsetDiff(tailY);
//
//        points.set(coordsSize - 1, getCoord(tailX, tailY));
//
//        if(coordsSize != 2 && tailX + tailY == 0){
//            points.remove(coordsSize - 1);
//        }
//    }
//
//    private int getOffsetDiff(int Offset){
//        return Integer.compare(Offset, 0);
//    }
//
//
//    private void checkHead(Coord oldSecondCoord){
//        if(isRotateY(oldSecondCoord.getX())){
//            points.add(1, GameObjectBuilder.getCoord(0, -dY));
//            return;
//        }
//
//        if(isRotateX(oldSecondCoord.getY())){
//            points.add(1, GameObjectBuilder.getCoord(-dX, 0));
//            return;
//        }
//
//        int x = points.get(1).getX();
//        int y = points.get(1).getY();
//
//        points.set(1, GameObjectBuilder.getCoord(x - dX, y - dY));
//    }
//
//    private boolean isRotateY(int xOffset){
//        return (headDirection == Direction.UP || headDirection == Direction.DOWN) && xOffset != 0;
//    }
//
//    private boolean isRotateX(int yOffset){
//        return (headDirection == Direction.LEFT || headDirection == Direction.RIGHT) && yOffset != 0;
//    }

}
