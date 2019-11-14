package nsu.manasyan.netsnake.models;

import nsu.manasyan.netsnake.proto.SnakesProto.NodeRole;
import nsu.manasyan.netsnake.proto.SnakesProto.GameConfig;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class CurrentGameModel {

    public interface GameStateListener{
        void onUpdate(GameState gameState);
    }

    private int playerId;

    private NodeRole playerRole;

    private GameConfig currentConfig;

    private GameState gameState;

    private InetSocketAddress masterAddress;

    private List<GameStateListener> gameStateListeners = new ArrayList<>();

    public CurrentGameModel(){

    }

    public CurrentGameModel(int playerId, GameConfig currentConfig, GameState gameState, NodeRole playerRole) {
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
        gameStateListeners.forEach(l -> l.onUpdate(gameState));
    }
}
