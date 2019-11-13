package nsu.manasyan.netsnake;

import nsu.manasyan.netsnake.controllers.CurrentGameController;
import nsu.manasyan.netsnake.models.AnnouncementContext;
import nsu.manasyan.netsnake.models.CurrentGameModel;
import nsu.manasyan.netsnake.models.MessageContext;
import nsu.manasyan.netsnake.network.Listener;
import nsu.manasyan.netsnake.network.Sender;
import nsu.manasyan.netsnake.out.SnakesProto.*;
import nsu.manasyan.netsnake.util.GameObjectBuilder;

import java.io.IOException;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class GameClient {

    private MulticastSocket socket;

    private Timer timer = new Timer();
    private Map<String, MessageContext> sentMessages = new HashMap<>();

    private CurrentGameModel currentGameModel = new CurrentGameModel();

    private CurrentGameController controller = new CurrentGameController(currentGameModel);

    private Listener listener;

    private Sender sender;

    public GameClient() throws IOException {
        this.socket = new MulticastSocket();
        this.sender = new Sender(controller, socket, sentMessages, "UNKNOWN");
        this.listener = new Listener(controller, sender, sentMessages, socket);
    }

    public void start(String multicastAddressStr) throws IOException {

    }

    public void createNewGame(GameConfig config) {
        listener.interrupt();
        currentGameModel = new CurrentGameModel(0, config,
                GameObjectBuilder.initNewGameState(config), NodeRole.MASTER);
        controller.setModel(currentGameModel);
        setTimer();
        listener.listen();
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
