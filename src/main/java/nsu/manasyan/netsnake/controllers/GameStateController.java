package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.models.MasterGameState;
import nsu.manasyan.netsnake.models.Snake;
import nsu.manasyan.netsnake.util.GameObjectBuilder;
import nsu.manasyan.netsnake.models.CurrentGameModel;
import nsu.manasyan.netsnake.proto.SnakesProto.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
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
        snakes.values().forEach(s -> {
            Direction direction = masterGameState.getCurrentPlayerDirection(s.getPlayerId());
            if(direction != null)
                s.setHeadDirection(direction);
            snakesController.snakeStep(s);
        });
        model.setGameState(masterGameState.toGameState(model.getCurrentConfig()));
        masterGameState.updateField();
    }

    public void setModel(CurrentGameModel model) {
        this.model = model;
    }

    public void addPlayer(GamePlayer player){
        masterGameState.getPlayers().put(player.getId(), player);
        addSnake(player.getId());
    }

    public void startNewGame(GameConfig config) {
        masterGameState = new MasterGameState(GameObjectBuilder.initNewFoods(config), config);
        snakesController.setMasterGameState(masterGameState);
        model.setCurrentDirection(masterGameState.getCurrentPlayerDirection(MASTER_ID));
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

    public void registerPlayerDirection(int playerId, Direction direction){
        var directions = masterGameState.getPlayersDirections();
        directions.computeIfAbsent(playerId, k -> new ArrayList<>());
        directions.get(playerId).add(direction);
    }

    public void updateField(){
        masterGameState.updateField();
    }

    public void registerDirection(Direction direction){
        if (!isCorrectDirection(direction)){
            return;
        }

        model.setCurrentDirection(direction);
        if(model.getPlayerRole() == NodeRole.MASTER)
            registerPlayerDirection(MASTER_ID, direction);
        // else
        // send direction to master
    }

    public Field getField(){
        return masterGameState.getField();
    }

    private boolean isCorrectDirection(Direction direction) {
        Direction currentDirection = model.getCurrentDirection();

        switch (direction){
            case UP:
                if(currentDirection == Direction.DOWN)
                    return false;
                break;
            case DOWN:
                if(currentDirection == Direction.UP)
                    return false;
                break;
            case LEFT:
                if(currentDirection == Direction.RIGHT)
                    return false;
                break;
            case RIGHT:
                if(currentDirection == Direction.LEFT)
                    return false;
                break;
        }

        return true;
    }


}
