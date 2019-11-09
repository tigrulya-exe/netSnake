package nsu.manasyan.netsnake.controllers;

import nsu.manasyan.netsnake.util.GameObjectBuilder;
import nsu.manasyan.netsnake.models.CurrentGameModel;
import nsu.manasyan.netsnake.out.SnakesProto.*;

import java.net.InetSocketAddress;
import java.util.List;

public class CurrentGameController {
    private CurrentGameModel model;

    public CurrentGameController(CurrentGameModel model) {
        this.model = model;
    }

    public CurrentGameModel getModel() {
        return model;
    }

    public void setModel(CurrentGameModel model) {
        this.model = model;
    }

    public void addPlayer(GamePlayer player){
        GameState.Builder gameStateBuilder = model.getGameState()
                .toBuilder();
        GamePlayers gamePlayers =  gameStateBuilder.getPlayersBuilder()
                .addPlayers(player)
                .build();
        model.setGameState(gameStateBuilder.setPlayers(gamePlayers).build());
        model.getAlivePlayers().put(player.getId(), true);
        addSnake(player.getId());
    }

    // TODO tmp
    public void removePlayer(int playerId){
        GameState gameState = model.getGameState();
        List<GamePlayer> otherPlayers = gameState.getPlayers().getPlayersList();
        otherPlayers.removeIf(p -> p.getId() == playerId);

        GamePlayers players = gameState.getPlayers()
                .toBuilder()
                .addAllPlayers(otherPlayers)
                .build();

        model.setGameState(gameState.toBuilder().setPlayers(players).build());
        removeSnake(playerId);
    }

    public void addSnake(int playerId){
        GameState.Snake snake = GameObjectBuilder.initNewSnake(playerId, model.getGameState());
        GameState.Builder gameStateBuilder = model.getGameState()
                .toBuilder();
        model.setGameState(gameStateBuilder.addSnakes(snake).build());
    }

    public void removeSnake(int playerId){
        List<GameState.Snake> snakes = model.getGameState().getSnakesList();
        snakes.removeIf(s -> s.getPlayerId() == playerId);

        model.setGameState(model.getGameState()
                .toBuilder()
                .clearSnakes()
                .addAllSnakes(snakes)
                .build());
    }

    public int getAvailablePlayerId(){
        return model.getGameState().getPlayers().getPlayersCount() + 1;
    }

    public void updateGameState(GameState gameState){
        model.setGameState(gameState);
    }

    public void setAlive(int playerId){
        model.getAlivePlayers().put(playerId, true);
    }

    private int getPlayerId(String address, int port){
        List<GamePlayer> others = model.getGameState().getPlayers().getPlayersList();
        for(GamePlayer player : others){
            if (player.getPort() == port && player.getIpAddress().equals(address)){
                return player.getId();
            }
        }

        return -1;
    }

    public InetSocketAddress getMasterAddress(){
        return model.getMasterAddress();
    }

    public NodeRole getRole(){
        return model.getPlayerRole();
    }

    public void setRole(NodeRole role){
        model.setPlayerRole(role);
    }

//    public void becomeMaster(){
//        int playerId = model.getPlayerId();
//        GamePlayers.Builder gamePlayersBuilder = model.getGameState().getPlayers().toBuilder();
//
//
//        int player = gamePlayersBuilder.getOthersList()
//                .stream()
//                .filter(p -> p.getId() == playerId)
//                .findFirst()
//                .get();
//
//        gamePlayersBuilder.
//
//        model.setGameState(model.getGameState().toBuilder().setPlayers(players).build());
//        removeSnake(playerId);
//
//    }
}
