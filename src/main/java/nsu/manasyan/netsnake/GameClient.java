package nsu.manasyan.netsnake;

import nsu.manasyan.netsnake.contexts.AnnouncementContext;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.models.ClientGameModel;
import nsu.manasyan.netsnake.contexts.MessageContext;
import nsu.manasyan.netsnake.network.Listener;
import nsu.manasyan.netsnake.network.MulticastListener;
import nsu.manasyan.netsnake.network.Sender;
import nsu.manasyan.netsnake.proto.SnakesProto.*;
import nsu.manasyan.netsnake.util.ErrorListener;
import nsu.manasyan.netsnake.util.GameObjectBuilder;

import java.io.IOException;
import java.net.InetAddress;
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

    private ClientGameModel clientGameModel = new ClientGameModel();

//    private MainController controller;

    private ClientController clientController;

    private Listener listener;

    private Sender sender;

    private MulticastListener multicastListener;

    public GameClient() throws IOException {
        this.clientController = ClientController.getInstance();
        clientController.setModel(clientGameModel);
        this.socket = new MulticastSocket(9192);
        this.sender = new Sender( socket, sentMessages);
        this.multicastListener = new MulticastListener(socket, InetAddress.getByName("239.192.0.4"));
        this.listener = new Listener( sender, sentMessages, socket);
    }

    public void start(String multicastAddressStr) throws IOException {

    }

    public void registerErrorListener(ErrorListener errorListener){
        clientController.registerErrorListener(errorListener);
    }

    public void startNewGame(GameConfig config) {
//        timer = new Timer();
        listener.interrupt();
//        controller.setGameClient(this);
        clientController.startNewGame(config);
//        becomeMaster();
//        scheduleTurns(config.getStateDelayMs());
        //TODO for debug
//        setTimer();
//        listener.listen();
    }
//
//    public void becomeMaster(){
//        timer = new Timer();
//        scheduleTurns(clientGameModel.getCurrentConfig().getStateDelayMs());
//    }

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
//
//    public void scheduleTurns(int stateDelayMs){
//        TimerTask newTurn  = new TimerTask() {
//            @Override
//            public void run() {
//                controller.newTurn();
//            }
//        };
//        timer.schedule(newTurn, stateDelayMs, stateDelayMs);
//    }

    public void stopCurrentGame(){
        timer.cancel();
        clientController.stopCurrentGame();
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
                sender.sendMessage(clientController.getMasterAddress(), ping);
            }
        };

        timer.schedule((clientController.getRole() == NodeRole.MASTER) ? masterSendPing : playerSendPing, 100,100);
    }

}
