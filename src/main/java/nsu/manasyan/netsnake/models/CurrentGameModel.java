package nsu.manasyan.netsnake.models;

import nsu.manasyan.netsnake.out.SnakesProto.*;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class CurrentGameModel {
    private static final int MASTER_INDEX = 0;

    private int playerId;

    private NodeRole playerRole;

    private GameConfig currentConfig;

    private GameState gameState;

    private InetSocketAddress masterAddress;

    // key - id, value - isAlive
    private Map<Integer, Boolean> alivePlayers = new HashMap<>();

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

    public Map<Integer, Boolean> getAlivePlayers() {
        return alivePlayers;
    }

    public InetSocketAddress getMasterAddress() {
        return masterAddress;
    }

    public void setMasterAddress(InetSocketAddress masterAddress) {
        this.masterAddress = masterAddress;
    }
}
