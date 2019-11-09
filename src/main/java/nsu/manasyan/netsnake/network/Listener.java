package nsu.manasyan.netsnake.network;

import nsu.manasyan.netsnake.util.GameExecutorService;
import nsu.manasyan.netsnake.controllers.CurrentGameController;
import nsu.manasyan.netsnake.models.MessageContext;
import nsu.manasyan.netsnake.out.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import nsu.manasyan.netsnake.out.SnakesProto.*;
import nsu.manasyan.netsnake.out.SnakesProto.GameMessage.*;

public class Listener {
    private interface Handler{
        void handle(GameMessage message, InetSocketAddress address) throws IOException;
    }

    private static final int BUF_LENGTH = 65000;

    private CurrentGameController controller;

    private Map<String, MessageContext> sentMessages;

    private Map<TypeCase, Handler> handlers = new HashMap<>();

    private byte[] receiveBuf = new byte[BUF_LENGTH];

    private Sender sender;

    private DatagramSocket socket;

    private volatile boolean isInterrupted = false;

//    private FiniteQueue<String> receivedMessageGuids = new FiniteQueue<>(RECEIVED_MESSAGES_BUF_LENGTH);

    public Listener(CurrentGameController controller, Sender sender, Map<String, MessageContext> sentMessages, DatagramSocket socket) {
        this.controller = controller;
        this.sender = sender;
        this.sentMessages = sentMessages;
        this.socket = socket;
        initHandlers();
    }

    public void listen(){
        GameExecutorService.getExecutorService().submit(() -> {
            GameMessage message;
            TypeCase type;

            DatagramPacket packetToReceive = new DatagramPacket(receiveBuf, BUF_LENGTH);
            try {
                while (!isInterrupted) {
                    socket.receive(packetToReceive);
                    message = SnakesProto.GameMessage.parseFrom(packetToReceive.getData());
                    type = message.getTypeCase();

//                if(checkIsDuplicate(type, message.getGUID())){
//                    continue;
//                }

                    handlers.get(type).handle(message, (InetSocketAddress) packetToReceive.getSocketAddress());
                    packetToReceive.setLength(BUF_LENGTH);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void interrupt(){
        isInterrupted = true;
    }

    private void handleJoinPlay(GameMessage message, InetSocketAddress address){
        // TODO we don't know name(why)
        GamePlayer player = GamePlayer.newBuilder()
                .setId(controller.getAvailablePlayerId())
                .setName("UNKNOWN")
                // TODO
                .setIpAddress(address.getHostName())
                .setPort(address.getPort())
                .build();
        controller.addPlayer(player);
    }

    private void handleState(GameMessage message, InetSocketAddress address){
        controller.updateGameState(message.getState().getState());
    }

    private void handleAck(GameMessage message, InetSocketAddress address){
        sentMessages.entrySet().removeIf(m -> message.getMsgSeq() == m.getValue().getMessage().getMsgSeq());
    }

    private void handlePing(GameMessage message, InetSocketAddress address){
        controller.setAlive(message.getSenderId());
    }

    private void handleRoleChange(){

    }

    private void initHandlers(){
        handlers.put(TypeCase.JOIN, this::handleJoinPlay);
        handlers.put(TypeCase.STATE, this::handleState);
        handlers.put(TypeCase.ACK, this::handleAck);
    }


//    private boolean checkIsDuplicate(MessageType messageType, String GUID){
//        if(messageType == MessageType.MESSAGE || messageType == MessageType.HELLO) {
//            if(receivedMessageGuids.contains(GUID)){
//                return true;
//            }
//            receivedMessageGuids.addGUID(GUID);
//        }
//
//        return false;
//    }
}
