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

}
