package nsu.manasyan.netsnake.models;

import nsu.manasyan.netsnake.Wrappers.Player;
import nsu.manasyan.netsnake.proto.SnakesProto;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState.*;
import nsu.manasyan.netsnake.util.GameObjectBuilder;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MasterGameModel {
    private static final int MASTER_ID = 0;

    private int stateOrder = 0;

    private SnakesProto.GameConfig config;

    private Map<Integer, nsu.manasyan.netsnake.Wrappers.Snake> snakes = new ConcurrentHashMap<>();

    private Map<Integer, Player> players = new ConcurrentHashMap<>();

    // key - id, value - isAlive
    private Map<Integer, Boolean> alivePlayers = new HashMap<>();

    private List<Coord> foods;

    private InetSocketAddress deputyAddress;

    private Map<Integer, List<SnakesProto.Direction>> headDirections = new HashMap<>();

    public MasterGameModel(List<Coord> foods, SnakesProto.GameConfig config) {
        this.foods = foods;
        this.config = config;
        initMaster();
    }

    public MasterGameModel(SnakesProto.GameState gameState) {
        // this.foods should be mutable
        this.foods = new ArrayList<>(gameState.getFoodsList());
        this.config = gameState.getConfig();
        initPlayers(gameState.getPlayers().getPlayersList());
        initAlivePlayers();
        initSnakes(gameState.getSnakesList());
        initHeadDirections();
    }

    private void initSnakes(List<Snake> immutableSnakesList) {
        immutableSnakesList.forEach(snake -> snakes.put(snake.getPlayerId(), new nsu.manasyan.netsnake.Wrappers.Snake(snake)));
    }

    private void initHeadDirections() {
        snakes.values().forEach(s -> {
            headDirections.put(s.getPlayerId(), new ArrayList<>());
            headDirections.get(s.getPlayerId()).add(s.getHeadDirection());
        });
    }

    private void initAlivePlayers() {
        players.keySet().forEach(id -> alivePlayers.put(id, true));
    }

    private void initPlayers(List<SnakesProto.GamePlayer> immutablePlayersList){
        immutablePlayersList.forEach(player -> players.put(player.getId(), new Player(player)));
    }

    public SnakesProto.GameState toGameState(){
        Collection <SnakesProto.GamePlayer> protoPlayers = players.values().stream().map(Player::toProto).collect(Collectors.toList());
        return GameObjectBuilder.getGameState(protoPlayers, getProtoSnakes(),
                config, stateOrder++, foods);
    }

    private void initMaster(){
        players.put(MASTER_ID, GameObjectBuilder.initMaster());
//        snakes.put(MASTER_ID, GameObjectBuilder.initNewSnake(MASTER_ID, field));
        //for master
        snakes.put(MASTER_ID, new nsu.manasyan.netsnake.Wrappers.Snake(MASTER_ID));
        headDirections.put(MASTER_ID, new ArrayList<>());
        headDirections.get(MASTER_ID).add(nsu.manasyan.netsnake.Wrappers.Snake.getDefaultDirection());
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
        List<SnakesProto.Direction> directions = headDirections.get(playerId);

        if(directions.size() == 0)
            return null;

        return directions.get(0);
    }

    public SnakesProto.Direction popPlayerHeadDirection(int playerId){
        List<SnakesProto.Direction> directions = headDirections.get(playerId);

        if(directions.size() == 0 )
            return null;

        SnakesProto.Direction direction = directions.get(0);
        directions.remove(0);
        return direction;
    }

    public Map<Integer, Boolean> getAlivePlayers() {
        return alivePlayers;
    }

    public Map<Integer, List<SnakesProto.Direction>> getHeadDirections() {
        return headDirections;
    }

    public void clear(){
        stateOrder = 0;
        snakes.clear();
        players.clear();
        foods.clear();
        headDirections.clear();
    }

    public void initPlayerHeadDirections(int id) {
        headDirections.put(id, new ArrayList<>());
        headDirections.get(id).add(nsu.manasyan.netsnake.Wrappers.Snake.getDefaultDirection());
    }

    public InetSocketAddress getDeputyAddress() {
        return deputyAddress;
    }

    public void setDeputyAddress(InetSocketAddress deputyAddress) {
        this.deputyAddress = deputyAddress;
    }
}