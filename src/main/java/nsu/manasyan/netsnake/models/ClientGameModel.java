package nsu.manasyan.netsnake.models;

import nsu.manasyan.netsnake.proto.SnakesProto;
import nsu.manasyan.netsnake.proto.SnakesProto.NodeRole;
import nsu.manasyan.netsnake.proto.SnakesProto.GameConfig;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientGameModel {

    public interface GameStateListener{
        void onUpdate(Map<Integer, Integer> scores);
    }

    private int playerId;

    private NodeRole playerRole;

    private Map<Integer, Integer> scores = new HashMap<>();

    private GameConfig currentConfig;

    private GameState gameState;

    private InetSocketAddress masterAddress;

    private SnakesProto.Direction currentDirection;

    private List<GameStateListener> gameStateListeners = new ArrayList<>();

    public ClientGameModel(){

    }

    public ClientGameModel(int playerId, GameConfig currentConfig, GameState gameState, NodeRole playerRole) {
        this.playerId = playerId;
        this.currentConfig = currentConfig;
        this.gameState = gameState;
        this.playerRole =  playerRole;
    }

    public GameConfig getCurrentConfig() {
        return currentConfig;
    }

    public void setCurrentConfig(GameConfig currentConfig) {
        this.currentConfig = currentConfig;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        notifyAllGameStateListeners();
    }

    public NodeRole getPlayerRole() {
        return playerRole;
    }

    public void setPlayerRole(NodeRole playerRole) {
        this.playerRole = playerRole;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public InetSocketAddress getMasterAddress() {
        return masterAddress;
    }

    public void setMasterAddress(InetSocketAddress masterAddress) {
        this.masterAddress = masterAddress;
    }

    public void registerGameStateListener(GameStateListener listener){
        gameStateListeners.add(listener);
    }

    public void notifyAllGameStateListeners(){
        gameStateListeners.forEach(l -> l.onUpdate(scores));
    }

    public SnakesProto.Direction getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(SnakesProto.Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    public void addScore(int playerId, String playerName, int newPoints){
        Integer oldScore = scores.get(playerId);
        if(oldScore == null){
            scores.put(playerId, 0);
            oldScore = 0;
        }
        // TODO add name to map))
        scores.put(playerId,oldScore + newPoints);
    }
}
