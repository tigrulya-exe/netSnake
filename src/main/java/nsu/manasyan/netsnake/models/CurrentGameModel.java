package nsu.manasyan.netsnake.models;

import nsu.manasyan.netsnake.out.SnakesProto.*;

public class CurrentGameModel {
    private int playerId;

    private GameConfig currentConfig;

    private GameState gameState;

    private boolean isPLayerMaster;

    public CurrentGameModel(int playerId, GameConfig currentConfig, GameState gameState) {
        this.playerId = playerId;
        this.currentConfig = currentConfig;
        this.gameState = gameState;
        this.isPLayerMaster =  playerId == gameState.getPlayers().getMaster().getId();
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

    public boolean isPLayerMaster() {
        return isPLayerMaster;
    }

    public void setPLayerMaster(boolean PLayerMaster) {
        this.isPLayerMaster = PLayerMaster;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
}
