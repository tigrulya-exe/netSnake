package nsu.manasyan.netsnake;

import nsu.manasyan.netsnake.contexts.AnnouncementContext;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.models.ClientGameModel;
import nsu.manasyan.netsnake.contexts.MessageContext;
import nsu.manasyan.netsnake.network.Listener;
import nsu.manasyan.netsnake.network.Sender;
import nsu.manasyan.netsnake.proto.SnakesProto.*;
import nsu.manasyan.netsnake.util.ErrorListener;
import nsu.manasyan.netsnake.util.GameExecutorService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

// TODO turn to singleton
public class NetworkControllerBridge {
    private MulticastSocket socket;

    private Map<Long, MessageContext> sentMessages = new HashMap<>();

    private ClientGameModel clientGameModel = new ClientGameModel();

    private ClientController clientController;

    private Listener listener;

    private Sender sender;

    public NetworkControllerBridge() throws IOException {

//        this.socket = new MulticastSocket(9192);
        this.socket = new MulticastSocket(7777);
        this.sender = new Sender( socket, sentMessages, InetAddress.getByName("239.192.0.4"));
//        this.multicastListener = new MulticastListener(socket, InetAddress.getByName("239.192.0.4"));
        this.clientController = ClientController.getInstance();
        clientController.setSender(sender);
        this.listener = new Listener( sender, sentMessages, socket, InetAddress.getByName("239.192.0.4"));
        listener.listen();
    }

    public void start(String multicastAddressStr) throws IOException {

    }

    public void registerErrorListener(ErrorListener errorListener){
        clientController.registerErrorListener(errorListener);
    }

    // TODO
    public void joinGame(GameMessage.AnnouncementMsg msg, AnnouncementContext context) {
        listener.interrupt();
        clientController.joinGame(context.getMasterAddress(), false, msg.getConfig());
        listener.listen();
    }

    public void stopCurrentGame(){
        sender.stop();
        listener.interrupt();
        clientController.stopCurrentGame();
    }

    public void restartCurrentGame(){
        sender.stop();
        clientController.restart();
    }
}
