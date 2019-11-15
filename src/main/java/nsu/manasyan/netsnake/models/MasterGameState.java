package nsu.manasyan.netsnake.models;

import nsu.manasyan.netsnake.proto.SnakesProto;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState.*;
import nsu.manasyan.netsnake.util.GameObjectBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MasterGameState {
    private static final int MASTER_ID = 0;

    private Field field;

    private int stateOrder = 0;

    private Map<Integer, Snake> snakes = new HashMap<>();

    private Map<Integer, SnakesProto.GamePlayer> players = new HashMap<>();

    // key - id, value - isAlive
    private Map<Integer, Boolean> alivePlayers = new HashMap<>();

    private List<Coord> foods;

    public MasterGameState(List<Coord> foods, SnakesProto.GameConfig config) {
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

    public Map<Integer, Boolean> getAlivePlayers() {
        return alivePlayers;
    }
}
