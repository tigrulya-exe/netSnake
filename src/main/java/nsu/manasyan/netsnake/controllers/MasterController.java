package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.Wrappers.FullPoints;
import nsu.manasyan.netsnake.Wrappers.Player;
import nsu.manasyan.netsnake.models.ClientGameModel;
import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.models.MasterGameModel;
import nsu.manasyan.netsnake.Wrappers.Snake;
import nsu.manasyan.netsnake.network.Sender;
import nsu.manasyan.netsnake.util.GameObjectBuilder;
import nsu.manasyan.netsnake.proto.SnakesProto.*;
import nsu.manasyan.netsnake.util.SnakePartManipulator;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

import static nsu.manasyan.netsnake.models.Field.Cell.HEAD;
import static nsu.manasyan.netsnake.util.GameObjectBuilder.*;

public class MasterController{
    private int masterId = 0;

    private ClientGameModel model;

    private Field field;

    private MasterGameModel masterGameModel;

    private SnakesController snakesController = new SnakesController();

    private SnakePartManipulator manipulator = SnakePartManipulator.getInstance();

    private Sender sender;

    private Timer timer;

    private int availablePlayerId = 1;

    private MasterController() {
        snakesController.setController(this);
    }

    public static MasterController getInstance() {
        return MasterController.SingletonHelper.controller;
    }

    private static class SingletonHelper{
        private static final MasterController controller = new MasterController();
    }

    public void startGame(ClientGameModel currModel, Sender senderIn, Field field){
        var config = currModel.getCurrentConfig();
        masterGameModel = new MasterGameModel(initNewFoods(config, field), config);
        currModel.setGameState(masterGameModel.toGameState());

        init(currModel, senderIn, field, config.getStateDelayMs());
    }

    public void becomeMaster(ClientGameModel currModel, Sender senderIn, Field field){
        masterGameModel = new MasterGameModel(currModel.getGameState());
        init(currModel, senderIn, field, currModel.getCurrentConfig().getStateDelayMs());
    }

    public void init(ClientGameModel currModel, Sender senderIn, Field field, int stateDelayMs){
        model = currModel;
        sender = senderIn;
        masterId = currModel.getPlayerId();
        setField(field);
        model.setCurrentDirection(masterGameModel.getPlayerHeadDirection(masterId));
        scheduleTurns(stateDelayMs);
    }

    public void setField(Field field){
        this.field = field;

        manipulator.setField(field);
        snakesController.setField(field);
    }

    public void removePlayer(int playerId) {
        System.out.println("PLAYER REMOVED: " + playerId);
        masterGameModel.getPlayers().remove(playerId);
        masterGameModel.getSnakes().get(playerId).setSnakeState(GameState.Snake.SnakeState.ZOMBIE);
        model.removeScore(playerId);
        checkDeputyDeath(playerId);
    }

    private void checkDeputyDeath(int playerId) {
        if (model.getDeputyId() != playerId)
            return;
        try {
            model.setDeputyAddress(null);

            for (var player : masterGameModel.getPlayers().values()) {
                if(player.getRole() != NodeRole.NORMAL)
                    continue;

                InetAddress inetAddress = InetAddress.getByName(player.getIpAddress());
                InetSocketAddress newDeputyAddress = new InetSocketAddress(inetAddress, player.getPort());
                model.setDeputyAddress(newDeputyAddress);
                model.setDeputyId(player.getId());

                sendRoleChangeToDeputy(newDeputyAddress, player.getId());
                break;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void scheduleTurns(int stateDelayMs){
        timer = new Timer();

        TimerTask newTurn  = new TimerTask() {
            @Override
            public void run(){
                newTurn();
                GameState gameState = model.getGameState();
                GameMessage stateMessage = GameObjectBuilder.initStateMessage(gameState);
                sender.broadcastState(stateMessage);
            }
        };
        timer.schedule(newTurn, stateDelayMs, stateDelayMs);
    }

    public void addScore(int playerId, int newPoints){
        if(masterGameModel.getPlayers().get(playerId) == null)
            System.out.println("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{");
        masterGameModel.getPlayers().get(playerId).addScore(newPoints);
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
        updateFullPoints(snakes.values());
        snakesController.checkSnakes(snakes.values(), model.getFullPoints());

        model.setGameState(masterGameModel.toGameState());
        generateFood();
        updateField();
    }

    private void updateFullPoints(Collection<Snake> snakes) {
        List<FullPoints> fullPoints = model.getFullPoints();
        int height = field.getHeight();
        int width = field.getWidth();

        fullPoints.clear();
        snakes.forEach(s -> fullPoints.add(new FullPoints(s.getPoints(), s.getPlayerId(), height, width)));
    }

    private void generateFood() {
        List<GameState.Coord> foods = masterGameModel.getFoods();
        int playersCount = masterGameModel.getPlayers().size();

        GameConfig config = model.getCurrentConfig();
        for(int i = foods.size(); i < config.getFoodStatic() + playersCount * config.getFoodPerPlayer(); ++i){
            foods.add(GameObjectBuilder.getFreeRandomCoord(config,field));
        }
    }

    private void updateField(){
        field.flush();

        masterGameModel.getSnakes().forEach((k,v) -> {
            manipulator.useSnakeCoords( v.getPoints(), snakesController::updateSnakePart);
            field.updateField(v.getPoints().get(0), HEAD);
        });

        masterGameModel.getFoods().forEach(c ->
                field.updateField(c.getX(), c.getY(), Field.Cell.FOOD));
    }

    public int addPlayer(String name, String address, int port, boolean onlyView){
        NodeRole role = (onlyView ) ? NodeRole.VIEWER : NodeRole.NORMAL;
        Player player = new Player(name,  availablePlayerId++ ,address, port, role, 0);
        System.out.println("ADDRESS: " + player.getIpAddress() + " : " + player.getPort());

        masterGameModel.getPlayers().put(player.getId(), player);
        model.addScore(player.getId(), player.getName(), 0);
        masterGameModel.initPlayerHeadDirections(player.getId());
        addSnake(player.getId());

        return player.getId();
    }

    public void checkDeputy(InetSocketAddress address, int id) {
        if (model.getDeputyAddress() != null)
            return;

        model.setDeputyAddress(address);
        model.setDeputyId(id);

        sendRoleChangeToDeputy(address, id);
    }

    public void removeSnake(int playerId){
        masterGameModel.getSnakes().remove(playerId);
    }

    public void addSnake(int playerId){
        Snake newSnake = GameObjectBuilder.initNewSnake(playerId, field);
        masterGameModel.getSnakes().put(playerId, newSnake);
    }

    public void setPlayerAlive(int playerId, boolean isAlive){
        masterGameModel.setPlayerAlive(playerId, isAlive);
    }

    public void registerPlayerDirection(int playerId, Direction direction){
        var directions = masterGameModel.getHeadDirections();
        directions.computeIfAbsent(playerId, k -> new ArrayList<>());
        directions.get(playerId).add(direction);
    }

    public void registerDirection(Direction direction){
        registerPlayerDirection(masterId, direction);
    }

    public void gameOver(int playerId){

        Snake deadSnake = masterGameModel.getSnakes().get(playerId);
        turnDeadSnakeIntoFood(deadSnake);
//        removeSnake(playerId);

        setPlayerAsViewer(playerId);
        model.removeScore(playerId);
    }

    public void stopCurrentGame(){
        availablePlayerId = 1;
        timer.cancel();
        masterGameModel.clear();
    }

    private void sendRoleChangeToDeputy(InetSocketAddress address, int id){
        var roleChangeMsg = getRoleChangeMessage(null, NodeRole.DEPUTY, id);
        sender.sendConfirmRequiredMessage(address, roleChangeMsg, id);
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
        Player player = players.get(playerId);

        if(player.getRole() == NodeRole.MASTER && model.getDeputyAddress() != null){
            var roleChangeMsg = getRoleChangeMessage(NodeRole.VIEWER, NodeRole.MASTER, model.getPlayerId());
            sender.sendConfirmRequiredMessage(model.getDeputyAddress(), roleChangeMsg, model.getDeputyId());
            stopCurrentGame();
            return;
        }

        players.get(playerId).setRole(NodeRole.VIEWER);
    }

    // getters

    public GameState getGameState(){
        return model.getGameState();
    }

    public Field getField(){
        return field;
    }

    public int getAvailablePlayerId(){
        return availablePlayerId;
    }

    public Collection<Player> getPlayers(){
        return masterGameModel.getPlayers().values();
    }

    public List<GameState.Coord> getModifiableFoods(){
        return masterGameModel.getFoods();
    }

    public Map<Integer, Boolean> getAlivePlayers(){
        return masterGameModel.getAlivePlayers();
    }

}