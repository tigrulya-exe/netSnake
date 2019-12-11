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

    private Field field;

    private boolean isFirstGameState = true;

    private ClientController() {
    }

    public static ClientController getInstance() {
        return ClientController.SingletonHelper.controller;
    }

    public void error(String errorMessage) {
        errorListener.onError(errorMessage);
    }

    private static class SingletonHelper{
        private static final ClientController controller = new ClientController();
    }

    public ClientGameModel getModel() {
        return model;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public void registerErrorListener(ErrorListener errorListener){
        this.errorListener = errorListener;
    }

    public void setMasterAddress(InetSocketAddress address){
        model.setMasterAddress(address);
    }

    public void setModel(ClientGameModel model) {
        this.model = model;
    }

    public void becomeMaster() {
        GameConfig config = model.getCurrentConfig();
        field = new Field(config.getHeight(), config.getWidth());
        masterController.init(config, model, sender, field);
    }

    public void startNewGame(GameConfig config) {
        model.setCurrentConfig(config);
        model.setPlayerId(MASTER_ID);
        model.setPlayerRole(NodeRole.MASTER);

        masterController = MasterController.getInstance();
        becomeMaster();
//        masterController.init(config);
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

    public void stopCurrentGame() {
        model.clear();

        if (model.getPlayerRole() == NodeRole.MASTER) {
            masterController.stopCurrentGame();
            return;
        }

        GameMessage roleChange = getRoleChangeMessage(NodeRole.VIEWER, null, model.getPlayerId());
//        sender.sendMessage(model.getMasterAddress(), roleChange);
    }

    public void setConfigurations(GameConfig config, InetSocketAddress masterAddress){
        field = new Field(config.getHeight(), config.getWidth());
        SnakePartManipulator.getInstance().setField(field);
        model.setMasterAddress(masterAddress);
        isFirstGameState = false;
    }

    public void joinGame(InetSocketAddress masterAddress, boolean onlyView){
        model.setMasterAddress(masterAddress);
        sender.sendMessage(masterAddress, getJoinMessage("TMP", onlyView));
    }

    public void setPlayerId(int id){
        model.setPlayerId(id);
    }

    public MasterController getMasterController() {
        return masterController;
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
