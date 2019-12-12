package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.contexts.AnnouncementContext;
import nsu.manasyan.netsnake.models.ClientGameModel;
import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.network.Sender;
import nsu.manasyan.netsnake.proto.SnakesProto.*;
import nsu.manasyan.netsnake.util.ErrorListener;
import nsu.manasyan.netsnake.util.SnakePartManipulator;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import static nsu.manasyan.netsnake.util.GameObjectBuilder.*;

public class ClientController {
    private static final int MASTER_ID = 0;

    private ClientGameModel model;

    private MasterController masterController;

    private ErrorListener errorListener;

    private Sender sender;

    private String name = "TIGRULYA";

    private Field field;

    private boolean isFirstGameState = true;

    private volatile boolean isMasterAlive = true;

    private static class SingletonHelper{

        private static final ClientController controller = new ClientController();
    }
    private ClientController() {
        model = new ClientGameModel();
    }

    public static ClientController getInstance() {
        return ClientController.SingletonHelper.controller;
    }

    public void error(String errorMessage) {
        errorListener.onError(errorMessage);
    }

    public void registerErrorListener(ErrorListener errorListener){
        this.errorListener = errorListener;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public void setMasterAddress(InetSocketAddress address){
        model.setMasterAddress(address);
    }

    public void becomeMaster() {
        model.setMasterAddress(null);
        model.setPlayerRole(NodeRole.MASTER);
        GameConfig config = model.getCurrentConfig();
        masterController = MasterController.getInstance();
        masterController.startGame(model, sender, field);
        sender.stop();
//        sender.setMasterTimer(config.getPingDelayMs());
    }

    public void restart() {
        model.clear();

        if(model.getPlayerRole() == NodeRole.MASTER) {
            masterController.stopCurrentGame();
            startNewGame(model.getCurrentConfig());
        } else
            joinGame(model.getMasterAddress(), false, model.getCurrentConfig());
    }

    public void startNewGame(GameConfig config) {
        model.setCurrentConfig(config);
        model.setPlayerId(MASTER_ID);

        field = new Field(config.getHeight(), config.getWidth());
        becomeMaster();
    }

    public void stopCurrentGame() {
        model.clear();

        if (model.getPlayerRole() == NodeRole.MASTER) {
            masterController.stopCurrentGame();
            return;
        }

        GameMessage roleChange = getRoleChangeMessage(NodeRole.VIEWER, null, model.getPlayerId());
//        sender.sendMessage(model.getMasterAddress(), roleChange);
    }

    public void changeMaster() {
        isMasterAlive = true;
        if(model.getPlayerRole() == NodeRole.DEPUTY) {
            becomeMaster();
            return;
        }
        model.setMasterAddress(model.getDeputyAddress());
        model.setDeputyAddress(null);
    }

    public void setRole(NodeRole role){
        model.setPlayerRole(role);
    }

    public void setGameState(GameState gameState){
        if(gameState.getStateOrder() <= model.getGameState().getStateOrder())
            return;
        model.setGameState(gameState);
    }

    public List<GameState.Snake> getSnakes(){
        return model.getGameState().getSnakesList();
    }

    public List<GameState.Coord> getFoods(){
        return model.getGameState().getFoodsList();
    }

    public void registerGameStateListener(ClientGameModel.GameStateListener listener){
        model.registerGameStateListener(listener);
    }

    public Map<GameMessage.AnnouncementMsg, AnnouncementContext> getAvailableGames() {
        return model.getAvailableGames();
    }

    public void addAvailableGame(GameMessage.AnnouncementMsg announcementMsg, InetSocketAddress masterAddress){
        model.addAvailableGame(announcementMsg, new AnnouncementContext(masterAddress));
    }

    public void registerAnnouncementListener(ClientGameModel.AnnouncementListener announcementListener){
        model.registerAnnouncementListener(announcementListener);
    }

    public void registerDirection(Direction direction){
        if(!isCorrectDirection(direction))
            return;

        model.setCurrentDirection(direction);
        if(model.getPlayerRole() == NodeRole.MASTER){
            masterController.registerDirection(direction);
            return;
        }

        sender.sendMessage(model.getMasterAddress(), getSteerMessage(direction,model.getPlayerId()));
    }

    public void setStartConfigurations(GameConfig config, InetSocketAddress masterAddress){
        field = new Field(config.getHeight(), config.getWidth());
        SnakePartManipulator.getInstance().setField(field);
        model.setMasterAddress(masterAddress);
        isFirstGameState = false;

    }

    public void joinGame(InetSocketAddress masterAddress, boolean onlyView, GameConfig config){
        model.setMasterAddress(masterAddress);
        sender.sendMessage(masterAddress, getJoinMessage(name, onlyView));
//        sender.setClientTimer(masterAddress, config.getPingDelayMs());
        setStartConfigurations(config, masterAddress);
    }

    public boolean isMasterAlive() {
        return isMasterAlive;
    }

    public void registerConfigListener(ClientGameModel.ConfigListener listener){
        model.registerConfigListener(listener);
    }

    public void setMasterAlive(boolean masterAlive) {
        isMasterAlive = masterAlive;
    }

    public void setPlayerId(int id){
        model.setPlayerId(id);
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
