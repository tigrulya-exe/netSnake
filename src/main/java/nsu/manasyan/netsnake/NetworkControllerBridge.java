package nsu.manasyan.netsnake;

import nsu.manasyan.netsnake.contexts.SentMessagesKey;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.models.ClientGameModel;
import nsu.manasyan.netsnake.contexts.MessageContext;
import nsu.manasyan.netsnake.network.Listener;
import nsu.manasyan.netsnake.network.Sender;
import nsu.manasyan.netsnake.util.ErrorListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO turn to singleton
public class NetworkControllerBridge {
    private MulticastSocket socket;

    private Map<SentMessagesKey, MessageContext> sentMessages = new ConcurrentHashMap<>();

    private ClientController clientController;

    private Listener listener;

    private Sender sender;

    public NetworkControllerBridge() throws IOException {

//        this.socket = new MulticastSocket(9192);
        this.socket = new MulticastSocket(9192);
        this.sender = new Sender( socket, sentMessages, InetAddress.getByName("239.192.0.4"));
//        this.multicastListener = new MulticastListener(socket, InetAddress.getByName("239.192.0.4"));
        this.clientController = ClientController.getInstance();
        clientController.setSender(sender);
        this.listener = new Listener( sender, sentMessages, socket, InetAddress.getByName("239.192.0.4"));
        listener.listen();
    }

    public void registerErrorListener(ErrorListener errorListener){
        clientController.registerErrorListener(errorListener);
    }

    public void stopCurrentGame(){
        listener.reload();
//        listener.interrupt();
        clientController.stopCurrentGame();
        sender.stop();
    }

//    public void restartCurrentGame(){
//        sender.stop();
//        listener.reload();
//        clientController.restart();
//    }
}
