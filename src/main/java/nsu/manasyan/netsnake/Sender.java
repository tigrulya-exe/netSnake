package nsu.manasyan.netsnake;

import nsu.manasyan.netsnake.models.MessageContext;
import nsu.manasyan.netsnake.out.SnakesProto.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public class Sender {
    private static final int MAX_MSG_BUF_SIZE = 4096;

    private CurrentGameController controller;

    private Map<String, MessageContext> sentMessages;

    private DatagramSocket socket;

    private String name;

    public Sender(CurrentGameController controller, DatagramSocket socket, Map<String, MessageContext> sentMessages, String name) {
        this.socket = socket;
        this.name = name;
        this.sentMessages = sentMessages;
    }

    public void broadcastMessage(GameMessage message) {
        GameExecutorService.getExecutorService().submit(() ->
                getPlayersToBroadcast().forEach(player -> {
//                    if(isConfirmNeed) {
//                        putIntoSentMessages(message.getGUID(), new MessageContext(message, ia));
//                    }
                    try {
                        byte[] buf = message.toByteArray();
                        socket.send(new DatagramPacket(buf, buf.length,
                                new InetSocketAddress(player.getIpAddress(), player.getPort())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
    }

    public void broadcastState(){
        broadcastMessage(GameObjectBuilder.initStateMessage(controller.getModel().getGameState()));
    }

    private List<GamePlayer> getPlayersToBroadcast(){
        return controller.getModel().getGameState().getPlayers().getOthersList();
    }

    public void sendMessage(InetSocketAddress receiverAddress, GameMessage message) throws IOException {
        try {
            byte[] buf = message.toByteArray();
            socket.send(new DatagramPacket(buf, buf.length, receiverAddress));
        } catch (IllegalArgumentException e){
            System.out.println(receiverAddress);
        }
    }

    public String getName() {
        return name;
    }

    public Map<String, MessageContext> getSentMessages() {
        return sentMessages;
    }

    private void putIntoSentMessages(String GUID, MessageContext context){
        if(sentMessages.size() >= MAX_MSG_BUF_SIZE){
            return;
        }

        sentMessages.put(GUID, context);
    }
}
