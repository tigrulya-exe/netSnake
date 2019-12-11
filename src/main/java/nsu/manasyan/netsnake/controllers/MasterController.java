package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.Wrappers.Player;
import nsu.manasyan.netsnake.models.ClientGameModel;
import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.models.MasterGameModel;
import nsu.manasyan.netsnake.Wrappers.Snake;
import nsu.manasyan.netsnake.network.Sender;
import nsu.manasyan.netsnake.util.GameObjectBuilder;
import nsu.manasyan.netsnake.proto.SnakesProto.*;
import nsu.manasyan.netsnake.util.SnakePartManipulator;

import java.util.*;

import static nsu.manasyan.netsnake.models.Field.Cell.HEAD;
import static nsu.manasyan.netsnake.util.GameObjectBuilder.*;

public class MasterController{
    private static final int MASTER_ID = 0;

    private ClientGameModel model;

    private Field field;

    private MasterGameModel masterGameModel;

    private SnakesController snakesController = new SnakesController();

    private SnakePartManipulator manipulator = SnakePartManipulator.getInstance();

    private Sender sender;

    private Timer timer;

    private MasterController() {
        snakesController.setController(this);
    }

    public static MasterController getInstance() {
        return MasterController.SingletonHelper.controller;
    }

    private static class SingletonHelper{
        private static final MasterController controller = new MasterController();
    }

    public void init(GameConfig config, ClientGameModel currModel, Sender senderIn, Field field){
        timer = new Timer();
        model = currModel;
        sender = senderIn;
        this.field = field;
        masterGameModel = new MasterGameModel(initNewFoods(config, field), config);

        manipulator.setField(field);
        model.setGameState(masterGameModel.toGameState());
        model.setCurrentDirection(masterGameModel.getPlayerHeadDirection(MASTER_ID));
        snakesController.setField(field);
        scheduleTurns(config.getStateDelayMs());
    }

    public void scheduleTurns(int stateDelayMs){
        TimerTask newTurn  = new TimerTask() {
            @Override
            public void run(){
                newTurn();
                GameState gameState = model.getGameState();
                GameMessage stateMessage = GameObjectBuilder.initStateMessage(gameState);
                sender.broadcastMessage(stateMessage);
            }
        };
        timer.schedule(newTurn, stateDelayMs, stateDelayMs);
    }

    public void addScore(int playerId, int newPoints){
        String playerName = masterGameModel.getPlayers().get(playerId).getName();
        model.addScore(playerId, playerName, newPoints);
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

        model.setGameState(masterGameModel.toGameState());
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

    public void updateField(){
        field.flush();

        masterGameModel.getSnakes().forEach((k,v) -> {
            manipulator.useSnakeCoords( v.getPoints(), snakesController::updateSnakePart);
            field.updateField(v.getPoints().get(0), HEAD);
        });

        masterGameModel.getFoods().forEach(c ->
                field.updateField(c.getX(), c.getY(), Field.Cell.FOOD
                ));
    }

    public void addPlayer(Player player){
        masterGameModel.getPlayers().put(player.getId(), player);
        model.addScore(player.getId(), player.getName(), 0);
        masterGameModel.initPlayerHeadDirections(player.getId());
        addSnake(player.getId());
    }

    public Collection<Player> getPlayers(){
        return masterGameModel.getPlayers().values();
    }


    public void removeSnake(int playerId){
        masterGameModel.getSnakes().remove(playerId);
    }

    public void addSnake(int playerId){
        Snake newSnake = GameObjectBuilder.initNewSnake(playerId, field);
        masterGameModel.getSnakes().put(playerId, newSnake);
    }

    public int getAvailablePlayerId(){
        return model.getGameState().getPlayers().getPlayersCount() + 1;
    }

    public void setAlive(int playerId){
        masterGameModel.getAlivePlayers().put(playerId, true);
    }

    public List<GameState.Coord> getModifiableFoods(){
        return masterGameModel.getFoods();
    }

    public void registerPlayerDirection(int playerId, Direction direction){
        var directions = masterGameModel.getPlayersDirections();
        directions.computeIfAbsent(playerId, k -> new ArrayList<>());
        directions.get(playerId).add(direction);
    }

    public void registerDirection(Direction direction){
        registerPlayerDirection(MASTER_ID, direction);
    }

    public void gameOver(int playerId){
        setPlayerAsViewer(playerId);
        Snake deadSnake = masterGameModel.getSnakes().get(playerId);
        turnDeadSnakeIntoFood(deadSnake);
        removeSnake(playerId);
        model.removeScore(playerId);
    }

    public Field getField(){
        return field;
    }

    public void stopCurrentGame(){
        timer.cancel();
        masterGameModel.clear();
    }

    private void turnDeadSnakeIntoFood(Snake snake) {
        manipulator.useSnakeCoords(snake.getPoints(),this::turnDeadSnakePartIntoFood);
    }

    private void turnDeadSnakePartIntoFood(int from, int to, int constCoord, boolean isVertical){
        GameConfig config = masterGameModel.getConfig();
        Random random = new Random();
        List<GameState.Coord> foods = masterGameModel.getFoods();

        int min = (from < to) ? from : to;
        int max = (from > to) ? from : to;

        for(int coord = min; coord <= max; ++coord){
            if(random.nextInt(100) > config.getDeadFoodProb() * 100)
                continue;

            if(isVertical)
                foods.add(GameObjectBuilder.getCoord(constCoord, coord));
            else
                foods.add(GameObjectBuilder.getCoord(coord, constCoord));
        }
    }

    public void setPlayerAsViewer(int playerId) {
        Map<Integer, Player> players = masterGameModel.getPlayers();
        players.get(playerId).setRole(NodeRole.VIEWER);
    }
}
