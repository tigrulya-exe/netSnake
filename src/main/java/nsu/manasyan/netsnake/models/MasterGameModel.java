package nsu.manasyan.netsnake.models;

import nsu.manasyan.netsnake.Wrappers.Player;
import nsu.manasyan.netsnake.proto.SnakesProto;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState.*;
import nsu.manasyan.netsnake.util.GameObjectBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MasterGameModel {
    private static final int MASTER_ID = 0;

    private int stateOrder = 0;

    private SnakesProto.GameConfig config;

    private Map<Integer, nsu.manasyan.netsnake.Wrappers.Snake> snakes = new ConcurrentHashMap<>();

    private Map<Integer, Player> players = new HashMap<>();

    // key - id, value - isAlive
    private Map<Integer, Boolean> alivePlayers = new HashMap<>();

    private List<Coord> foods;

    private Map<Integer, List<SnakesProto.Direction>> playersDirections = new HashMap<>();

    public MasterGameModel(List<Coord> foods, SnakesProto.GameConfig config) {
        this.foods = foods;
        this.config = config;
        initMaster();
    }

    public SnakesProto.GameState toGameState(){
        Collection <SnakesProto.GamePlayer> protoPlayers = players.values().stream().map(Player::toProto).collect(Collectors.toList());
        return GameObjectBuilder.getGameState(protoPlayers, getProtoSnakes(),
                config, stateOrder, foods);
    }

    private void initMaster(){
        players.put(MASTER_ID, GameObjectBuilder.initMaster());
//        snakes.put(MASTER_ID, GameObjectBuilder.initNewSnake(MASTER_ID, field));
        //for master
        snakes.put(MASTER_ID, new nsu.manasyan.netsnake.Wrappers.Snake(MASTER_ID));
        playersDirections.put(MASTER_ID, new ArrayList<>());
        playersDirections.get(MASTER_ID).add(nsu.manasyan.netsnake.Wrappers.Snake.getDefaultDirection());
    }

    private List<SnakesProto.GameState.Snake> getProtoSnakes(){
        return snakes.values().stream().map(nsu.manasyan.netsnake.Wrappers.Snake::toProtoSnake).collect(Collectors.toList());
    }

    public Map<Integer, nsu.manasyan.netsnake.Wrappers.Snake> getSnakes() {
        return snakes;
    }

    public Map<Integer, Player> getPlayers() {
        return players;
    }

    public List<Coord> getFoods() {
        return foods;
    }

    public SnakesProto.GameConfig getConfig(){
        return config;
    }

    public SnakesProto.Direction getPlayerHeadDirection(int playerId){
        List<SnakesProto.Direction> directions = playersDirections.get(playerId);

        if(directions.size() == 0)
            return null;

        return directions.get(0);
    }

    public SnakesProto.Direction popPlayerHeadDirection(int playerId){
        List<SnakesProto.Direction> directions = playersDirections.get(playerId);

        if(directions.size() == 0 )
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

    public void clear(){
        snakes.clear();
        players.clear();
        foods.clear();
        playersDirections.clear();
    }

    public void initPlayerHeadDirections(int id) {
        playersDirections.put(id, new ArrayList<>());
        playersDirections.get(id).add(nsu.manasyan.netsnake.Wrappers.Snake.getDefaultDirection());
    }
}
