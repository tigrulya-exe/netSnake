package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.models.ClientGameModel;
import nsu.manasyan.netsnake.proto.SnakesProto.*;
import nsu.manasyan.netsnake.util.ErrorListener;

import java.net.InetSocketAddress;
import java.util.List;

public class ClientController {
    private static final int MASTER_ID = 0;

    private ClientGameModel model;

    private MasterController masterController;

    private ErrorListener errorListener;

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
        masterController.init(config, model);
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

    public void registerDirection(Direction direction){
        if(model.getPlayerRole() == NodeRole.MASTER){
            masterController.registerDirection(direction);
            return;
        }
        model.setCurrentDirection(direction);
        // send direction to master
    }


    public MasterController getMasterController() {
        return masterController;
    }
}
