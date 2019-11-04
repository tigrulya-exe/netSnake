package nsu.manasyan.netsnake;

import nsu.manasyan.netsnake.models.CurrentGameModel;
import nsu.manasyan.netsnake.models.MessageContext;
import nsu.manasyan.netsnake.out.SnakesProto.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;


public class GameClient {

    private MulticastSocket multicastSocket;

    private DatagramSocket socket;

    private Map<String, MessageContext> sentMessages = new HashMap<>();

    private CurrentGameModel currentGameModel = new CurrentGameModel();

    private CurrentGameController controller = new CurrentGameController(currentGameModel);

    private Listener listener;

    private Sender sender;

    public GameClient() throws IOException {
        this.multicastSocket = new MulticastSocket();
        this.socket = new DatagramSocket();
        this.sender = new Sender(controller, socket, sentMessages, "UNKNOWN");
        this.listener = new Listener(controller, sender, sentMessages, socket);
    }

    public void start(String multicastAddressStr) throws IOException {

    }

    public void createNewGame(GameConfig config) {
        listener.interrupt();
        currentGameModel = new CurrentGameModel(0, config, GameObjectBuilder.initNewGameState(config));
        controller.setModel(currentGameModel);
        listener.listen();
    }

}
