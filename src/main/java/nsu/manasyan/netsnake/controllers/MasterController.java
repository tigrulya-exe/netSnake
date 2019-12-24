package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.Wrappers.FullPoints;
import nsu.manasyan.netsnake.Wrappers.Player;
import nsu.manasyan.netsnake.exceptions.MasterDeadException;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static nsu.manasyan.netsnake.models.Field.Cell.HEAD;
import static nsu.manasyan.netsnake.proto.SnakesProto.Direction.*;
import static nsu.manasyan.netsnake.util.GameObjectBuilder.*;

public class MasterController{
    private ClientGameModel model;

    private Field field;

    private MasterGameModel masterGameModel;

    private SnakesController snakesController = new SnakesController();

    private SnakePartManipulator manipulator = SnakePartManipulator.getInstance();

    private Sender sender;

//    private Timer timer;

    private ExecutorService gameLoop = Executors.newSingleThreadExecutor();

    private int availablePlayerId = 0;

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
        model = currModel;
        availablePlayerId = 0;
        this.field = field;
        var config = model.getCurrentConfig();
        masterGameModel = new MasterGameModel(initNewFoods(config, field),
                config, model.getPlayerId());
        addPlayer(model.getPlayerName(), "", 9192, NodeRole.MASTER);
        masterGameModel.setMasterDirection();

        model.setGameState(masterGameModel.toGameState());
        init(senderIn, field, config.getStateDelayMs());
    }

    public void becomeMaster(ClientGameModel currModel, Sender senderIn, Field field){
        model = currModel;
        masterGameModel = new MasterGameModel(model.getGameState(), model.getPlayerId());
//        masterGameModel.getPlayers().remove(model.getMasterId());
        initOldMasterAddress();
        init(senderIn, field, model.getCurrentConfig().getStateDelayMs());

        int playerId = model.getPlayerId();
        model.setMasterId(playerId);
        masterGameModel.getPlayers().get(playerId).setRole(NodeRole.MASTER);
    }

    private void initOldMasterAddress() {
        Player oldMaster = masterGameModel.getPlayers().get(model.getMasterId());
        oldMaster.setIpAddress(model.getMasterAddress().getHostString());
    }

    public void init(Sender senderIn, Field field, int stateDelayMs){
        sender = senderIn;
        setField(field);
        masterGameModel.setMasterDirection();
        scheduleTurns(stateDelayMs);
    }

    public void setField(Field field){
        this.field = field;

        manipulator.setField(field);
        snakesController.setField(field);
    }

    public void removePlayer(int playerId) {
        System.out.println("PLAYER REMOVED: " + playerId);
        Player player = masterGameModel.getPlayers().get(playerId);

        if(player == null){
            return;
        }

        if(player.getRole() != NodeRole.VIEWER){
            masterGameModel.getSnakes().get(playerId).setSnakeState(GameState.Snake.SnakeState.ZOMBIE);
            model.removeScore(playerId);
        }
        masterGameModel.getPlayers().remove(playerId);
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
        gameLoop = Executors.newSingleThreadExecutor();

        gameLoop.submit(() -> {
            boolean interrupted = false;
            while (!interrupted) {
                try {
                    newTurn();
                    GameState gameState = model.getGameState();
                    GameMessage stateMessage = GameObjectBuilder.initStateMessage(gameState);
                    sender.broadcastState(stateMessage);

                    Thread.sleep(stateDelayMs);
                } catch (MasterDeadException | InterruptedException exception) {
                    System.out.println("DEAD");
                    interrupted = true;
                }
            }
            model.setGameState(masterGameModel.toGameState());
            stopCurrentGame();
            sender.stop();
        });
    }

    public void addScore(int playerId, int newPoints){
        if(masterGameModel.getPlayers().get(playerId) == null)
            System.out.println("{{{{{{{{{{{{{{  {{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{");
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

    public int addPlayer(String name, String address, int port, NodeRole role){
        Player player = new Player(name, availablePlayerId++ ,address, port, role, 0);
        System.out.println("ADDRESS: " + player.getIpAddress() + " : " + player.getPort());

        masterGameModel.addPlayer(player);
        model.addScore(player.getId(), player.getName(), 0);
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

    public void registerPlayerDirection(int playerId, Direction newDirection){
        var snake = masterGameModel.getSnakes().get(playerId);

        if (snake == null ||!isCorrectDirection(newDirection, snake.getHeadDirection())) {
            return;
        }

        var directions = masterGameModel.getHeadDirections();
        directions.computeIfAbsent(playerId, k -> new ArrayList<>());
        directions.get(playerId).add(newDirection);
    }

    public void registerDirection(Direction newDirection){

        if(isCorrectDirection(newDirection, masterGameModel.getMasterDirection())) {
            registerPlayerDirection(model.getMasterId(), newDirection);
            masterGameModel.setMasterDirection(newDirection);

        }
    }

    private boolean isCorrectDirection(Direction newDirection, Direction oldDirection) {
        return !(oldDirection == UP && newDirection == DOWN ||
                oldDirection == DOWN && newDirection == UP ||
                oldDirection == LEFT && newDirection == RIGHT ||
                oldDirection == RIGHT && newDirection == LEFT);
    }


    public void gameOver(int playerId){

        Snake deadSnake = masterGameModel.getSnakes().get(playerId);
        if(deadSnake == null)
            System.out.println("lkn");
        turnDeadSnakeIntoFood(deadSnake);

        if(deadSnake.getSnakeState() != GameState.Snake.SnakeState.ZOMBIE) {
            model.removeScore(playerId);
            setPlayerAsViewer(playerId);
            if (getPlayerRole(playerId) == NodeRole.MASTER) {
                model.setPlayerRole(NodeRole.VIEWER);
                throw new MasterDeadException();
            }
        }
    }

    public void stopCurrentGame(){
        availablePlayerId = 0;
        if(masterGameModel != null){
            gameLoop.shutdownNow();
            masterGameModel.clear();
        }
    }

    private void sendRoleChangeToDeputy(InetSocketAddress address, int id){
        var roleChangeMsg = getRoleChangeMessage(null, NodeRole.DEPUTY, model.getPlayerId(),id);
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
        checkDeputyDeath(playerId);
        sendRoleChangeToDeputy(players.get(playerId));

        players.get(playerId).setRole(NodeRole.VIEWER);
        masterGameModel.getSnakes().get(playerId).setSnakeState(GameState.Snake.SnakeState.ZOMBIE);
    }

    private void sendRoleChangeToDeputy(Player player){
        if(player.getRole() == NodeRole.MASTER && model.getDeputyAddress() != null) {
                var roleChangeMsg = getRoleChangeMessage(NodeRole.VIEWER, NodeRole.MASTER,
                        model.getPlayerId(), player.getId());
                sender.sendConfirmRequiredMessage(model.getDeputyAddress(), roleChangeMsg, model.getDeputyId());
                stopCurrentGame();
                sender.setClientTimer(model.getDeputyAddress(), model.getMasterId());

        }
    }

    private NodeRole getPlayerRole(int playerId){
        Player player = masterGameModel.getPlayers().get(playerId);
        return player.getRole();
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