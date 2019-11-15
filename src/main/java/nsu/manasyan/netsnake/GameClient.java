package nsu.manasyan.netsnake;

import nsu.manasyan.netsnake.controllers.GameStateController;
import nsu.manasyan.netsnake.contexts.AnnouncementContext;
import nsu.manasyan.netsnake.models.CurrentGameModel;
import nsu.manasyan.netsnake.contexts.MessageContext;
import nsu.manasyan.netsnake.network.Listener;
import nsu.manasyan.netsnake.network.Sender;
import nsu.manasyan.netsnake.proto.SnakesProto.*;
import nsu.manasyan.netsnake.util.GameObjectBuilder;

import java.io.IOException;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

// TODO turn to singleton
public class GameClient {
    private String name;

    private MulticastSocket socket;

    private Timer timer = new Timer();
    private Map<String, MessageContext> sentMessages = new HashMap<>();

    private CurrentGameModel currentGameModel = new CurrentGameModel();

    private GameStateController controller;

    private Listener listener;

    private Sender sender;

    public GameClient() throws IOException {
        this.controller = GameStateController.getInstance();
        controller.setModel(currentGameModel);
        this.socket = new MulticastSocket();
        this.sender = new Sender(controller, socket, sentMessages, "UNKNOWN");
        this.listener = new Listener(controller, sender, sentMessages, socket);
    }

    public void start(String multicastAddressStr) throws IOException {

    }

    public void startNewGame(GameConfig config) {
        listener.interrupt();
        controller.startNewGame(config);
        scheduleTurns(config.getStateDelayMs());
        //TODO for debug
//        setTimer();
//        listener.listen();
    }

    // TODO
    public void joinGame(AnnouncementContext context) {
        listener.interrupt();
        timer.cancel();
//        GameMessage joinMessage =
//        sender.sendMessage(context.getMasterAddress(), );
        setTimer();
        listener.listen();
    }

//    public void scheduleTurns(int stateDelayMs){
////        TimerTask broadcastState  = new TimerTask() {
////            @Override
////            public void run() {
////                GameState gameState = controller.getGameState();
////                GameMessage stateMessage =  GameObjectBuilder.initStateMessage(gameState);
////                sender.broadcastMessage(stateMessage);
////                controller.setGameState(gameState);
////            }
////        };
////
////        timer.schedule(broadcastState, stateDelayMs, stateDelayMs);
////    }

    public void scheduleTurns(int stateDelayMs){
        TimerTask newTurn  = new TimerTask() {
            @Override
            public void run() {
                controller.newTurn();

            }
        };

        timer.schedule(newTurn, stateDelayMs, stateDelayMs);
    }



    private void setTimer(){
        timer = new Timer();

        TimerTask masterSendPing  = new TimerTask() {
            @Override
            public void run() {
                GameMessage ping =  GameObjectBuilder.initPingMessage();
                sender.broadcastMessage(ping);
            }
        };

        TimerTask playerSendPing  = new TimerTask() {
            @Override
            public void run() {
                GameMessage ping = GameObjectBuilder.initPingMessage();
                sender.sendMessage(controller.getMasterAddress(), ping);
            }
        };

        timer.schedule((controller.getRole() == NodeRole.MASTER) ? masterSendPing : playerSendPing, 100,100);
    }

}
