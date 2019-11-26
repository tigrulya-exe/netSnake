package nsu.manasyan.netsnake.network;

import nsu.manasyan.netsnake.contexts.MessageContext;
import nsu.manasyan.netsnake.controllers.MasterController;
import nsu.manasyan.netsnake.proto.SnakesProto.*;
import nsu.manasyan.netsnake.util.GameExecutorService;

import java.io.IOException;
import java.net.*;
import java.util.Map;

public class Sender {
    private static final int MAX_MSG_BUF_SIZE = 4096;

    private InetAddress multicastAddress;

    private MasterController masterController = MasterController.getInstance();

    private Map<String, MessageContext> sentMessages;

    private MulticastSocket socket;

    private int port;

    public Sender( MulticastSocket socket, Map<String, MessageContext> sentMessages) {
        this.socket = socket;
        this.sentMessages = sentMessages;
    }

    public void broadcastMessage(GameMessage message) {
        GameExecutorService.getExecutorService().submit(() ->
                masterController.getPlayers().forEach(player -> {
//                  putIntoSentMessages(message.getMsgSeq(), new MessageContext(message, ia));
                    try {
                        byte[] buf = message.toByteArray();
                        socket.send(new DatagramPacket(buf, buf.length,
                                new InetSocketAddress(player.getIpAddress(), player.getPort())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
    }

    public void broadcastState(GameMessage.StateMsg state) throws IOException {
        byte[] buf = state.toByteArray();
        socket.send(new DatagramPacket(buf, buf.length,multicastAddress, port));
    }


    public void sendMessage(InetSocketAddress receiverAddress, GameMessage message) {
        try {
            byte[] buf = message.toByteArray();
            socket.send(new DatagramPacket(buf, buf.length, receiverAddress));
        } catch (IOException e){
            System.out.println(receiverAddress);
        }
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
