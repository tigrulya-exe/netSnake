package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.models.MasterGameModel;
import nsu.manasyan.netsnake.models.Snake;
import nsu.manasyan.netsnake.util.GameObjectBuilder;
import nsu.manasyan.netsnake.models.CurrentGameModel;
import nsu.manasyan.netsnake.proto.SnakesProto.*;
import nsu.manasyan.netsnake.util.SnakePartManipulator;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nsu.manasyan.netsnake.models.Field.Cell.HEAD;


public class GameStateController {
    private static final int MASTER_ID = 0;

    private CurrentGameModel model;

    private Field field;

    private MasterGameModel masterGameModel;

    private SnakesController snakesController = new SnakesController();

    private SnakePartManipulator manipulator = SnakePartManipulator.getInstance();

    private GameStateController() {
        snakesController.setController(this);
    }

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
        Map<Integer, Snake> snakes = masterGameModel.getSnakes();

        for (var iter = snakes.values().iterator(); iter.hasNext(); ) {
            Snake snake = iter.next();
            Direction direction = masterGameModel.popPlayerHeadDirection(snake.getPlayerId());
            if(direction != null)
                snake.setHeadDirection(direction);
            snakesController.moveSnake(snake);
        }

        model.setGameState(masterGameModel.toGameState(model.getCurrentConfig()));
        generateFood();
        updateField();
    }

    private void generateFood() {
        List<GameState.Coord> foods = masterGameModel.getFoods();
        int playersCount = masterGameModel.getPlayers().size();

        GameConfig config = model.getCurrentConfig();
        for(int i = foods.size(); i < config.getFoodStatic() + playersCount * config.getFoodPerPlayer(); ++i){
            foods.add(GameObjectBuilder.getFreeRandomCoord(config,field));
        }
    }

    private void updateSnakePart(int from, int to, int constCoord, boolean isVertical){
        int min = (from < to) ? from : to;
        int max = (from > to) ? from : to;

        for(int coord = min; coord <= max; ++coord){
            if(isVertical)
                field.updateField(constCoord, coord, Field.Cell.SNAKE);
            else
                field.updateField(coord, constCoord, Field.Cell.SNAKE);
        }
    }

    public void updateField(){
        field.flush();

        masterGameModel.getSnakes().forEach((k,v) -> {
            manipulator.useSnakeCoords( v.getPoints(), this::updateSnakePart);
            field.updateField(v.getPoints().get(0), HEAD);
        });

        masterGameModel.getFoods().forEach(c -> field.updateField(c.getX(), c.getY(), Field.Cell.FOOD));
    }

    public void updateSnake(Snake snake){
        manipulator.useSnakeCoords( snake.getPoints(), this::updateSnakePart);
        field.updateField(snake.getPoints().get(0), HEAD);
    }

    public void setModel(CurrentGameModel model) {
        this.model = model;
    }

    public void addPlayer(GamePlayer player){
        masterGameModel.getPlayers().put(player.getId(), player);
        addSnake(player.getId());
    }

    public void startNewGame(GameConfig config) {
        this.field = new Field(config.getHeight(), config.getWidth());
        masterGameModel = new MasterGameModel(GameObjectBuilder.initNewFoods(config, field), config);
        snakesController.setField(field);
        manipulator.setField(field);
        model.setCurrentDirection(masterGameModel.getPlayerHeadDirection(MASTER_ID));
        model.setGameState(masterGameModel.toGameState(config));
        model.setPlayerId(MASTER_ID);
        model.setCurrentConfig(config);
        model.setPlayerRole(NodeRole.MASTER);
    }

    public void removePlayer(int playerId){
        masterGameModel.getPlayers().remove(playerId);
        masterGameModel.getSnakes().remove(playerId);
        // TODO add some magic to turn snake into food
    }

    public void addSnake(int playerId){
        Snake newSnake = GameObjectBuilder.initNewSnake(playerId, field);
        masterGameModel.getSnakes().put(playerId, newSnake);
    }

    public void removeSnake(int playerId){
        masterGameModel.getSnakes().remove(playerId);
    }

    public int getAvailablePlayerId(){
        return model.getGameState().getPlayers().getPlayersCount() + 1;
    }

    public void updateGameState(GameState gameState){
        model.setGameState(gameState);
    }

    public void setAlive(int playerId){
        masterGameModel.getAlivePlayers().put(playerId, true);
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

    public List<GameState.Coord> getModifiableFoods(){
        return masterGameModel.getFoods();
    }

    public void registerGameStateListener(CurrentGameModel.GameStateListener listener){
        model.registerGameStateListener(listener);
    }

    public void registerPlayerDirection(int playerId, Direction direction){
        var directions = masterGameModel.getPlayersDirections();
        directions.computeIfAbsent(playerId, k -> new ArrayList<>());
        directions.get(playerId).add(direction);
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

    public void gameOver(int playerId){
        removePlayer(playerId);
        System.out.println("DEAD");
    }

    public Field getField(){
        return field;
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
