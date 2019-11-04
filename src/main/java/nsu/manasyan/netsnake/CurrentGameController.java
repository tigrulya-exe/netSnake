package nsu.manasyan.netsnake;

import nsu.manasyan.netsnake.models.CurrentGameModel;
import nsu.manasyan.netsnake.out.SnakesProto.*;

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
    }

    public void addSnake(GameState.Snake snake){
        GameState.Builder gameStateBuilder = model.getGameState()
                .toBuilder();
        model.setGameState(gameStateBuilder.addSnakes(snake).build());
    }

    public int getAvailablePlayerId(){
        return model.getGameState().getPlayers().getOthersCount() + 1;
    }
}
