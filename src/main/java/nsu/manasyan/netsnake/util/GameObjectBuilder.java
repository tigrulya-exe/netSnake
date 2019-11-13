package nsu.manasyan.netsnake.util;

import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.proto.SnakesProto.*;

import java.util.*;

public class GameObjectBuilder {
    private static String name;

    private static int port;

    private static final int DEFAULT_PORT = 18888;

    private static final int DEFAULT_MASTER_ID = 0;

    private static final String DEFAULT_ADDRESS_STR = "127.0.0.1";

    private static final String DEFAULT_MASTER_ADDRESS_STR = "";

    private static final String DEFAULT_NAME = "Steve";

    private static int currentGameMsgSeq = 0;

    public static GameMessage initPingMessage(){
        GameMessage message = initMessage();
        message.toBuilder().setPing(GameMessage.PingMsg.newBuilder().build());
        return message;
    }

    public static GamePlayer initMaster() {
        return GamePlayer
                .newBuilder()
                .setId(DEFAULT_MASTER_ID)
                .setIpAddress(DEFAULT_MASTER_ADDRESS_STR)
                .setPort(port)
                .setName(name)
                .setRole(NodeRole.MASTER)
                .build();
    }

    public static List<GameState.Coord> initNewFoods(GameConfig config){
        Random random = new Random();
        List<GameState.Coord> foods = new ArrayList<>();
        int x, y;

        for(int i = 0; i < config.getFoodStatic() + config.getFoodPerPlayer(); ++i){
            x = random.nextInt(config.getWidth());
            y = random.nextInt(config.getHeight());
            foods.add(getCoord(x,y));
        }

        // TODO CHECK
        return foods;
    }

    public static GameMessage initMessage(){
        return GameMessage.newBuilder()
                .setMsgSeq(currentGameMsgSeq++)
                .build();
    }

    public static GameState.Snake initNewSnake(int playerId, Field field){
        if(field == null){
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


    public static GameState getGameState(Collection<GamePlayer> players, Collection<GameState.Snake> snakes,
                                          GameConfig config, int stateOrder, Collection<GameState.Coord> foods){
        GamePlayers gamePlayers = getGamePlayers(players);
        return GameState.newBuilder()
                .setPlayers(gamePlayers)
                .addAllSnakes(snakes)
                .setConfig(config)
                .setStateOrder(stateOrder)
                .addAllFoods(foods)
                .build();
    }

    private static GamePlayers getGamePlayers(Collection<GamePlayer> players){
        return GamePlayers.newBuilder()
                .addAllPlayers(players)
                .build();
    }


    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        GameObjectBuilder.name = name;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        GameObjectBuilder.port = port;
    }
}
