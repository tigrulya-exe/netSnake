package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.models.MasterGameState;
import nsu.manasyan.netsnake.models.Snake;
import nsu.manasyan.netsnake.util.GameObjectBuilder;
import nsu.manasyan.netsnake.models.CurrentGameModel;
import nsu.manasyan.netsnake.proto.SnakesProto.*;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;


public class GameStateController {
    private static final int MASTER_ID = 0;

    private CurrentGameModel model;

    private MasterGameState masterGameState;

    private SnakesController snakesController = new SnakesController();

    private GameStateController() {}

    private static class SingletonHelper{

        private static final GameStateController controller = new GameStateController();
    }
    public static GameStateController getInstance() {
        return GameStateController.SingletonHelper.controller;
    }

    public CurrentGameModel getModel() {
        return model;
    }

    public void newTurn() {
        Map<Integer, Snake> snakes = masterGameState.getSnakes();
        snakes.values().forEach(s -> snakesController.snakeStep(s));
        model.setGameState(masterGameState.toGameState(model.getCurrentConfig()));
    }

    public void setModel(CurrentGameModel model) {
        this.model = model;
    }

    public void addPlayer(GamePlayer player){
        masterGameState.getPlayers().put(player.getId(), player);
    }

    public void startNewGame(GameConfig config) {
        masterGameState = new MasterGameState(GameObjectBuilder.initNewFoods(config), config);
        model.setGameState(masterGameState.toGameState(config));
        model.setPlayerId(MASTER_ID);
        model.setCurrentConfig(config);
        model.setPlayerRole(NodeRole.MASTER);
    }

    public void removePlayer(int playerId){
        masterGameState.getPlayers().remove(playerId);
        masterGameState.getSnakes().remove(playerId);
        // TODO add some magic to turn snake into food
    }

    public void addSnake(int playerId){
        Snake newSnake = GameObjectBuilder.initNewSnake(playerId,masterGameState.getField());
        masterGameState.getSnakes().put(playerId, newSnake);
    }

    public void removeSnake(int playerId){
        masterGameState.getSnakes().remove(playerId);
    }

    public int getAvailablePlayerId(){
        return model.getGameState().getPlayers().getPlayersCount() + 1;
    }

    public void updateGameState(GameState gameState){
        model.setGameState(gameState);
    }

    public void setAlive(int playerId){
        masterGameState.getAlivePlayers().put(playerId, true);
    }

    public NodeRole getRole(){
        return model.getPlayerRole();
    }

    public void setRole(NodeRole role){
        model.setPlayerRole(role);
    }

    public InetSocketAddress getMasterAddress(){
        return model.getMasterAddress();
    }

    public GameState getGameState(){
        return model.getGameState();
    }

    public void setGameState(GameState gameState){
        model.setGameState(gameState);
    }

    public List<GameState.Snake> getSnakes(){
        return model.getGameState().getSnakesList();
    }

    public List<GameState.Coord> getFoods(){
        return model.getGameState().getFoodsList();
    }

    public void registerGameStateListener(CurrentGameModel.GameStateListener listener){
        model.registerGameStateListener(listener);
    }
}