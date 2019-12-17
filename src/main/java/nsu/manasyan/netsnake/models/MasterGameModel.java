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
    private int stateOrder = 0;

    private int masterId = 0;

    private SnakesProto.GameConfig config;

    private Map<Integer, nsu.manasyan.netsnake.Wrappers.Snake> snakes = new ConcurrentHashMap<>();

    private Map<Integer, Player> players = new ConcurrentHashMap<>();

    // key - id, value - isAlive
    private Map<Integer, Boolean> alivePlayers = new ConcurrentHashMap<>();

    private List<Coord> foods;

    private Map<Integer, List<SnakesProto.Direction>> headDirections = new HashMap<>();

    private SnakesProto.Direction masterDirection;

    public MasterGameModel(List<Coord> foods, SnakesProto.GameConfig config) {
        this.foods = foods;
        this.config = config;
        players.put(masterId, GameObjectBuilder.initMaster());
    }

    public MasterGameModel(SnakesProto.GameState gameState, int newMasterId) {
        // this.foods should be mutable
        this.masterId = newMasterId;
        this.foods = new ArrayList<>(gameState.getFoodsList());
        this.config = gameState.getConfig();
        initPlayers(gameState.getPlayers().getPlayersList());
        initAlivePlayers();
        initSnakes(gameState.getSnakesList());
        initHeadDirections();
        setMasterDirection();
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

    public void setPlayerAlive(int playerId, boolean isAlive){
        alivePlayers.put(playerId, isAlive);
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

    public SnakesProto.Direction getMasterDirection() {
        return masterDirection;
    }

    public void setMasterDirection() {
        masterDirection =  getPlayerHeadDirection(masterId);
    }

    public void setMasterDirection(SnakesProto.Direction newDirection) {
        masterDirection =  newDirection;
    }

    public int getMasterId() {
        return masterId;
    }

    public void addPlayer(Player player) {
        players.put(player.getId(), player);
        alivePlayers.put(player.getId(), true);
        initPlayerHeadDirections(player.getId());
    }
}