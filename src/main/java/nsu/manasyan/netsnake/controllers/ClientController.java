package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.Wrappers.FullPoints;
import nsu.manasyan.netsnake.contexts.AnnouncementContext;
import nsu.manasyan.netsnake.models.ClientGameModel;
import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.network.Sender;
import nsu.manasyan.netsnake.proto.SnakesProto.*;
import nsu.manasyan.netsnake.util.ErrorListener;
import nsu.manasyan.netsnake.util.SnakePartManipulator;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static nsu.manasyan.netsnake.util.GameObjectBuilder.*;

public class ClientController {
    private static final int DEFAULT_MASTER_ID = 0;

    private ClientGameModel model;

    private MasterController masterController = MasterController.getInstance();

    private ErrorListener errorListener;

    private Sender sender;

    private Field field;

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

    public void updateAllFullPoints(){
        updateFullPoints(model.getGameState().getSnakesList());
    }

    private void updateFullPoints(Collection<GameState.Snake> snakes) {
        List<FullPoints> fullPoints = getFullPoints();
        int height = field.getHeight();
        int width = field.getWidth();

        fullPoints.clear();
        snakes.forEach(s -> fullPoints.add(new FullPoints(s.getPointsList(), s.getPlayerId(), height, width)));
    }

    public void error(String errorMessage) {
        errorListener.onError(errorMessage);
    }

    public void registerErrorListener(ErrorListener errorListener){
        this.errorListener = errorListener;
    }

    public void setMasterAddress(InetSocketAddress address){
        model.setMasterAddress(address);
    }

    public void initMasterContext() {
        model.setPlayerRole(NodeRole.MASTER);
        sender.stop();

        var config = model.getCurrentConfig();
        sender.setMasterTimer(config.getPingDelayMs(), config.getNodeTimeoutMs(), model.getMasterId());
    }

    public void startNewGame(GameConfig config) {
        model.setCurrentConfig(config);
        model.setPlayerId(DEFAULT_MASTER_ID);
        model.setMasterId(DEFAULT_MASTER_ID);

        field = new Field(config.getHeight(), config.getWidth());
        masterController.startGame(model, sender, field);
        initMasterContext();
    }

    public void becomeMaster() {
        masterController.becomeMaster(model, sender, field);
        initMasterContext();
    }

    public void stopCurrentGame() {
        model.clear();
        model.clearGameStateListeners();

        if (model.getPlayerRole() == NodeRole.MASTER) {
            masterController.stopCurrentGame();
            return;
        }

        GameMessage roleChange = getRoleChangeMessage(NodeRole.VIEWER, null, model.getPlayerId());
        sender.sendConfirmRequiredMessage(model.getMasterAddress(), roleChange, model.getMasterId());
    }

    public void changeMaster() {
        System.out.println("MASKPWDONWJKNIJBDWJVOQWGIVDGQHWD");
        isMasterAlive = true;
        if(model.getPlayerRole() == NodeRole.DEPUTY) {
            becomeMaster();
            return;
        }
        model.setMasterAddress(model.getDeputyAddress());
        model.setDeputyAddress(null);
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
        if(model.getPlayerRole() == NodeRole.MASTER){
            masterController.registerDirection(direction);
            return;
        }

        GameMessage message = getSteerMessage(direction,model.getPlayerId());
        sender.sendConfirmRequiredMessage(model.getMasterAddress(), message, model.getMasterId());
    }

    public void setStartConfigurations(GameConfig config){
        field = new Field(config.getHeight(), config.getWidth());
        SnakePartManipulator.getInstance().setField(field);
    }

    public void joinGame(InetSocketAddress masterAddress, boolean onlyView, GameMessage.AnnouncementMsg announcementMsg){
        GameConfig config = announcementMsg.getConfig();

        model.setCurrentConfig(config);
        model.setPlayerRole(NodeRole.NORMAL);
        model.setMasterId(getMasterId(announcementMsg.getPlayers().getPlayersList()));
        System.out.println("MASTER ADDR: " + masterAddress);
        model.setMasterAddress(masterAddress);
        sender.sendJoin(masterAddress, getJoinMessage(getPlayerName(), onlyView));
        setStartConfigurations(config);
    }

    private int getMasterId(List<GamePlayer> players){
        for(var player : players){
            if(player.getRole() == NodeRole.MASTER)
                return player.getId();
        }

        return  -1;
    }

    public void registerConfigListener(ClientGameModel.ConfigListener listener){
        model.registerConfigListener(listener);
    }


    public void setPlayerAlive(int id){
        if(model.getPlayerRole() == NodeRole.MASTER){
            masterController.setPlayerAlive(id, true);
            return;
        }
        isMasterAlive = true;
    }


    // getters setters

    public GameState getState(){
        return model.getGameState();
    }

    public void setRole(NodeRole role){
        model.setPlayerRole(role);
    }

    public void setGameState(GameState gameState){
//        if(gameState.getStateOrder() <= model.getGameState().getStateOrder())
//            return;
        isMasterAlive = true;
        model.setGameState(gameState);
    }

    public List<GameState.Snake> getSnakes(){
        return model.getGameState().getSnakesList();
    }

    public List<GameState.Coord> getFoods(){
        return model.getGameState().getFoodsList();
    }

    public int getMasterId() {
        return model.getMasterId();
    }

    public void setMasterAlive(boolean masterAlive) {
        isMasterAlive = masterAlive;
    }

    public void setPlayerId(int id){
        model.setPlayerId(id);
    }

    public int getPlayerId(){
        return model.getPlayerId();
    }

    public GameConfig getConfig(){
        return model.getCurrentConfig();
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public String getPlayerName(){
        return model.getPlayerName();
    }

    public List<FullPoints> getFullPoints() {
        return model.getFullPoints();
    }

    public void setPlayerName(String playerName){
        model.setPlayerName(playerName);
    }

    public boolean isMasterAlive() {
        return isMasterAlive;
    }
}
