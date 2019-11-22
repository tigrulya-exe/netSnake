package nsu.manasyan.netsnake.util;

import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.models.Snake;
import nsu.manasyan.netsnake.proto.SnakesProto.*;

import java.util.*;

public class GameObjectBuilder {
    private static String name = "Steve";

    private static int port = -1;

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

    public static List<GameState.Coord> initNewFoods(GameConfig config, Field field){
        List<GameState.Coord> foods = new LinkedList<>();

        for(int i = 0; i < config.getFoodStatic() + config.getFoodPerPlayer(); ++i){
            foods.add(getFreeRandomCoord(config, field));
        }

        // TODO CHECK
        return foods;
    }

    public static GameState.Coord getFreeRandomCoord(GameConfig config, Field field){
        Random random = new Random();
        int x, y;
        do {
            x = random.nextInt(config.getWidth());
            y = random.nextInt(config.getHeight());
        } while (field.getCell(x, y) != Field.Cell.FREE);

        return getCoord(x, y);
    }

    public static GameMessage initMessage(){
        return GameMessage.newBuilder()
                .setMsgSeq(currentGameMsgSeq++)
                .build();
    }

    public static Snake initNewSnake(int playerId, Field field){
        if(field == null){
            return new Snake(playerId);
        }

        // TODO find free 5x5 to place 2x1 snake (head + tail)
        return null;
    }

    public static GameState.Coord getCoord(int x, int y){
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

    public static GameMessage initStateMessage(GameState state){
        GameMessage.StateMsg stateMsg = getStateMsg(state);

        return GameMessage.newBuilder()
                .setState(stateMsg)
                .setMsgSeq(currentGameMsgSeq++)
                .build();
    }

    private static GameMessage.StateMsg getStateMsg(GameState state){
        return GameMessage.StateMsg.newBuilder()
                .setState(state)
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
