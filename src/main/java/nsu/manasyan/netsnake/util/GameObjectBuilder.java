package nsu.manasyan.netsnake.util;

import nsu.manasyan.netsnake.out.SnakesProto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameObjectBuilder {
    private static final int DEFAULT_PORT = 18888;

    private static final int DEFAULT_MASTER_ID = 0;

    private static final String DEFAULT_ADDRESS_STR = "127.0.0.1";

    private static final String DEFAULT_NAME = "Steve";

    private static int currentGameMsgSeq = 0;

    public static GamePlayers initNewGamePlayers() {
        GamePlayer master = initMaster();
        return GamePlayers
                .newBuilder()
                .addPlayers(master)
                .build();
    }

    public static GameMessage initPingMessage(){
        GameMessage message = initMessage();
        message.toBuilder().setPing(GameMessage.PingMsg.newBuilder().build());
        return message;
    }

    public static GamePlayer initMaster() {
        return GamePlayer
                .newBuilder()
                .setId(DEFAULT_MASTER_ID)
                .setIpAddress(DEFAULT_ADDRESS_STR)
                .setPort(DEFAULT_PORT)
                .setName(DEFAULT_NAME)
                .setRole(NodeRole.MASTER)
                .build();
    }

    public static GameState initNewGameState(GameConfig config){
        return GameState
                .newBuilder()
                .setStateOrder(0)
                .setPlayers(initNewGamePlayers())
                .addAllFoods(initFoods(config))
                .addSnakes(initNewSnake(DEFAULT_MASTER_ID, null))
                .build();
    }

    public static List<GameState.Coord> initFoods(GameConfig config){
        Random random = new Random();
        List<GameState.Coord> foods = new ArrayList<>();
        int x, y;

        for(int i = 0; i < config.getFoodStatic() + config.getFoodPerPlayer(); ++i){
            x = random.nextInt(config.getWidth());
            y = random.nextInt(config.getHeight());
            foods.add(getCoord(x,y));
        }

        return foods;
    }

    public static GameMessage initMessage(){
        return GameMessage.newBuilder()
                .setMsgSeq(currentGameMsgSeq++)
                .build();
    }


    public static GameState.Snake initNewSnake(int playerId, GameState gameState){
        if(gameState == null){
            return GameState.Snake.newBuilder()
                    .setPlayerId(playerId)
                    .addPoints(getCoord(1,0))
                    .addPoints(getCoord(0,0))
                    .build();
        }

        // TODO find free 5x5 to place 2x1 snake (head + tail)
        return null;
    }

    private static GameState.Coord getCoord(int x, int y){
        return GameState.Coord.newBuilder().setX(x).setY(y).build();
    }
}
