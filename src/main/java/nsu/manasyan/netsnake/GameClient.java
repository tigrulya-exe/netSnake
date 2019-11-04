package nsu.manasyan.netsnake;

import nsu.manasyan.netsnake.models.CurrentGameModel;
import nsu.manasyan.netsnake.out.SnakesProto.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.MulticastSocket;


public class GameClient {


    private MulticastSocket multicastSocket;

    private DatagramSocket socket;

    private CurrentGameModel currentGameModel;

    private CurrentGameController controller;

    private Listener listener;

    public GameClient() throws IOException {
        this.socket = new DatagramSocket();
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
