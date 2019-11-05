package nsu.manasyan.netsnake;

import nsu.manasyan.netsnake.models.CurrentGameModel;
import nsu.manasyan.netsnake.out.SnakesProto.*;

import java.util.Collections;
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
                .addOthers(player)
                .build();
        model.setGameState(gameStateBuilder.setPlayers(gamePlayers).build());
        model.getAlivePlayers().put(player.getIpAddress() + ":" + player.getPort(), true);
        addSnake(player.getId());
    }

    // TODO tmp
    public void removePlayer(String address, int port){
        int playerId = getPlayerId(address,port);

        model.getAlivePlayers().remove(address + ":" + port);
        GameState gameState = model.getGameState();
        List<GamePlayer> otherPlayers = gameState.getPlayers().getOthersList();
        otherPlayers.removeIf(p -> p.getId() == playerId);

        GamePlayers players = gameState.getPlayers()
                .toBuilder()
                .addAllOthers(otherPlayers)
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
        return model.getGameState().getPlayers().getOthersCount() + 1;
    }

    public void updateGameState(GameState gameState){
        model.setGameState(gameState);
    }

    public void setAlive(String address, int port){
        model.getAlivePlayers().put(address + ":" + port, true);
    }

    private int getPlayerId(String address, int port){
        GamePlayer master = model.getGameState().getPlayers().getMaster();
        if(master.getPort() == port && master.getIpAddress().equals(address))
            return master.getId();

        List<GamePlayer> others = model.getGameState().getPlayers().getOthersList();
        for(GamePlayer player : others){
            if (player.getPort() == port && player.getIpAddress().equals(address)){
                return player.getId();
            }
        }

        return -1;
    }
}
