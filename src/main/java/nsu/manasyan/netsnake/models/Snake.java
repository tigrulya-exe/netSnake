package nsu.manasyan.netsnake.models;

import nsu.manasyan.netsnake.proto.SnakesProto;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState.Snake.*;

import java.util.ArrayList;
import java.util.List;

import static nsu.manasyan.netsnake.util.GameObjectBuilder.getCoord;

public class Snake {
    private SnakeState snakeState = SnakeState.ALIVE;

    private int playerId;

    private List<SnakesProto.GameState.Coord> points = new ArrayList<>();

    private SnakesProto.Direction headDirection = SnakesProto.Direction.RIGHT;

    public Snake(int playerId){
        this.playerId = playerId;
        points.add(getCoord(0,0));
        points.add(getCoord(1,0));
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

    public List<SnakesProto.GameState.Coord> getPoints() {
        return points;
    }

    public void setPoints(List<SnakesProto.GameState.Coord> points) {
        this.points = points;
    }

    public SnakesProto.Direction getHeadDirection() {
        return headDirection;
    }

    public void setHeadDirection(SnakesProto.Direction headDirection) {
        this.headDirection = headDirection;
    }

    public SnakesProto.GameState.Snake toProtoSnake(){
        return SnakesProto.GameState.Snake.newBuilder()
                .setState(snakeState)
                .setHeadDirection(headDirection)
                .addAllPoints(points)
                .setPlayerId(playerId)
                .build();
    }
}
