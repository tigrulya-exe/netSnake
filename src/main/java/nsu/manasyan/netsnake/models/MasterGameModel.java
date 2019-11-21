package nsu.manasyan.netsnake.models;

import nsu.manasyan.netsnake.controllers.SnakesController;
import nsu.manasyan.netsnake.proto.SnakesProto;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState.*;
import nsu.manasyan.netsnake.util.GameObjectBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nsu.manasyan.netsnake.models.Field.Cell.HEAD;

public class MasterGameModel {
    private static final int MASTER_ID = 0;

    private Field field;

    private int stateOrder = 0;

    private Map<Integer, Snake> snakes = new HashMap<>();

    private Map<Integer, SnakesProto.GamePlayer> players = new HashMap<>();

    // key - id, value - isAlive
    private Map<Integer, Boolean> alivePlayers = new HashMap<>();

    private List<Coord> foods;

    private Map<Integer, List<SnakesProto.Direction>> playersDirections = new HashMap<>();

    public MasterGameModel(List<Coord> foods, SnakesProto.GameConfig config) {
        this.foods = foods;
        this.field = new Field(config.getHeight(), config.getWidth());
        initMaster();
    }

    public SnakesProto.GameState toGameState(SnakesProto.GameConfig config){
        return GameObjectBuilder.getGameState(players.values(), getProtoSnakes(),
                config, stateOrder, foods);
    }

    private void initMaster(){
        players.put(MASTER_ID, GameObjectBuilder.initMaster());
//        snakes.put(MASTER_ID, GameObjectBuilder.initNewSnake(MASTER_ID, field));
        //for master
        snakes.put(MASTER_ID, new Snake(MASTER_ID));
        playersDirections.put(MASTER_ID, new ArrayList<>());
        playersDirections.get(MASTER_ID).add(Snake.getDefaultDirection());
    }

    private List<SnakesProto.GameState.Snake> getProtoSnakes(){
        return snakes.values().stream().map(Snake::toProtoSnake).collect(Collectors.toList());
    }

    public Map<Integer, Snake> getSnakes() {
        return snakes;
    }

    public Map<Integer, SnakesProto.GamePlayer> getPlayers() {
        return players;
    }

    public List<Coord> getFoods() {
        return foods;
    }

    public Field getField() {
        return field;
    }

    public void updateField(){
        field.flush();

        snakes.forEach((k,v) -> {
            SnakesController.useSnakeCoords( v.getPoints(), this::updateSnakePart);
            field.updateField(v.getPoints().get(0), HEAD);
        });

        foods.forEach(c -> field.updateField(c.getX(), c.getY(), Field.Cell.FOOD));
    }

    public SnakesProto.Direction getPlayerHeadDirection(int playerId){
        List<SnakesProto.Direction> directions = playersDirections.get(playerId);

        if(directions.size() == 0)
            return null;

        return directions.get(0);
    }

    public SnakesProto.Direction popPlayerHeadDirection(int playerId){
        List<SnakesProto.Direction> directions = playersDirections.get(playerId);

        if(directions.size() == 0)
            return null;

        SnakesProto.Direction direction = directions.get(0);
        directions.remove(0);
        return direction;
    }

    public Map<Integer, Boolean> getAlivePlayers() {
        return alivePlayers;
    }

    public Map<Integer, List<SnakesProto.Direction>> getPlayersDirections() {
        return playersDirections;
    }

    private void updateSnakePart(int from, int to, int constCoord, boolean isVertical){
        int min = (from < to) ? from : to;
        int max = (from > to) ? from : to;

        for(int coord = min; coord <= max; ++coord){
            if(isVertical)
                field.updateField(constCoord, coord, Field.Cell.SNAKE);
            else
                field.updateField(coord, constCoord, Field.Cell.SNAKE);
        }
    }
}
