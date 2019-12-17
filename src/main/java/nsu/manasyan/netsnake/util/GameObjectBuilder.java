package nsu.manasyan.netsnake.util;

import nsu.manasyan.netsnake.Wrappers.Player;
import nsu.manasyan.netsnake.controllers.MasterController;
import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.Wrappers.Snake;
import nsu.manasyan.netsnake.proto.SnakesProto.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GameObjectBuilder {
    private static String name = "Steve";

    private static int port = -1;

    private static final int DEFAULT_MASTER_ID = 0;

    private static final String DEFAULT_MASTER_ADDRESS_STR = "";

    private static AtomicInteger currentGameMsgSeq = new AtomicInteger(0);

    public static GameMessage initPingMessage(){
        return GameMessage.newBuilder()
                .setPing(GameMessage.PingMsg.newBuilder().build())
                .setMsgSeq(currentGameMsgSeq.getAndAdd(1))
                .build();
    }

    public static Player initMaster() {
        return new Player(name, DEFAULT_MASTER_ID, DEFAULT_MASTER_ADDRESS_STR, port, NodeRole.MASTER, 0);
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

    public static Snake initNewSnake(int playerId, Field field){
        if(field == null){
            return new  Snake(playerId);
        }

//         TODO find free 5x5 to place 2x1 snake (head + tail)
        for(int x = 0; x < field.getWidth(); ){
            outer: for(int y = 0; y < field.getHeight(); ++y){
                for(int i  = 0; i < 5; ++i){
                    if(!checkLine(field, x, y + i)){
                        continue outer;
                    }
                }

                List<GameState.Coord> points = new ArrayList<>();
                points.add(getCoord(x + 2, y + 2));
                points.add(getCoord(1, 0));
                return new Snake(playerId, points);
            }
        }

        return null;
    }

    private static boolean checkLine(Field field, int startX, int y){
        for(int x = startX; x  < startX + 5; ++x){
//            if(field.getCell(x, y) != Field.Cell.FREE || field.getCell(x, y) != Field.Cell.FOOD){
            if(field.getCell(x, y) != Field.Cell.FREE) {
                return false;
            }
        }

        return true;
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
                .setMsgSeq(currentGameMsgSeq.getAndAdd(1))
                .build();
    }

    public static GameMessage getAnnouncementMessage(){
        GameMessage.AnnouncementMsg announcementMsg = getAnnouncement();

        return GameMessage.newBuilder()
                .setAnnouncement(announcementMsg)
                .setMsgSeq(currentGameMsgSeq.getAndAdd(1))
                .build();
    }

    private static GameMessage.AnnouncementMsg getAnnouncement() {
        GameState gameState = MasterController.getInstance().getGameState();

        return GameMessage.AnnouncementMsg.newBuilder()
                .setCanJoin(true)
                .setConfig(gameState.getConfig())
                .setPlayers(gameState.getPlayers())
                .build();
    }

    public static GameMessage getAckMsg(int senderId, int receiverId, long msgSeq){
        GameMessage.AckMsg ack =  GameMessage.AckMsg.newBuilder().build();

        return GameMessage.newBuilder()
                .setAck(ack)
                .setSenderId(senderId)
                .setMsgSeq(msgSeq)
                .setReceiverId(receiverId)
                .build();
    }


    public static GameMessage getSteerMessage(Direction direction, int playerId){
        GameMessage.SteerMsg steerMsg = GameMessage.SteerMsg.newBuilder()
                .setDirection(direction)
                .build();

        return GameMessage.newBuilder()
                .setSteer(steerMsg)
                .setSenderId(playerId)
                .setMsgSeq(currentGameMsgSeq.getAndAdd(1))
                .build();
    }

    public static GameMessage getRoleChangeMessage(NodeRole senderRole, NodeRole receiverRole, int playerId) {
        GameMessage.RoleChangeMsg.Builder roleChangeBuilder = GameMessage.RoleChangeMsg.newBuilder();
        if (senderRole != null)
            roleChangeBuilder.setSenderRole(senderRole);
        if (receiverRole != null)
            roleChangeBuilder.setReceiverRole(receiverRole);

        return GameMessage.newBuilder()
                .setRoleChange(roleChangeBuilder.build())
                .setSenderId(playerId)
                .setMsgSeq(currentGameMsgSeq.getAndAdd(1))
                .build();
    }

    public static GameMessage getJoinMessage(String name, boolean onlyView) {
        GameMessage.JoinMsg joinMsg = GameMessage.JoinMsg.newBuilder()
                .setName(name)
                .setOnlyView(onlyView)
                .build();

        return GameMessage.newBuilder()
                .setMsgSeq(currentGameMsgSeq.getAndAdd(1))
                .setJoin(joinMsg)
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
}
