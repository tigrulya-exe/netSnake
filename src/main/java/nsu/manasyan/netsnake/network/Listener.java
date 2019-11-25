package nsu.manasyan.netsnake.network;

import nsu.manasyan.netsnake.Wrappers.Player;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.controllers.MasterController;
import nsu.manasyan.netsnake.util.GameExecutorService;
import nsu.manasyan.netsnake.contexts.MessageContext;
import nsu.manasyan.netsnake.proto.SnakesProto;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import nsu.manasyan.netsnake.proto.SnakesProto.*;
import nsu.manasyan.netsnake.proto.SnakesProto.GameMessage.*;

public class Listener {
    private interface Handler{
        void handle(GameMessage message, InetSocketAddress address) throws IOException;
    }

    private static final int BUF_LENGTH = 65000;

    private MasterController masterController = MasterController.getInstance();

    private ClientController clientController = ClientController.getInstance();

    private Map<String, MessageContext> sentMessages;

    private Map<TypeCase, Handler> handlers = new HashMap<>();

    private byte[] receiveBuf = new byte[BUF_LENGTH];

    private Sender sender;

    private DatagramSocket socket;

    private volatile boolean isInterrupted = false;

//    private FiniteQueue<String> receivedMessageGuids = new FiniteQueue<>(RECEIVED_MESSAGES_BUF_LENGTH);

    public Listener( Sender sender, Map<String, MessageContext> sentMessages, DatagramSocket socket) {
//        this.controller = controller;
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
        JoinMsg joinMsg = message.getJoin();
        NodeRole role = (!joinMsg.hasOnlyView() || !joinMsg.getOnlyView()) ? NodeRole.NORMAL : NodeRole.VIEWER;

        Player player = new Player(joinMsg.getName(), message.getSenderId(),
                address.getHostName(), address.getPort(), role, 0);

        masterController.addPlayer(player);
    }

    private void handleState(GameMessage message, InetSocketAddress address){
        clientController.setGameState(message.getState().getState());
    }

    private void handleAck(GameMessage message, InetSocketAddress address){
        sentMessages.entrySet().removeIf(m -> message.getMsgSeq() == m.getValue().getMessage().getMsgSeq());
    }

    private void handlePing(GameMessage message, InetSocketAddress address){
        masterController.setAlive(message.getSenderId());
    }

    private void handleSteer(GameMessage message, InetSocketAddress address){
        Direction direction = message.getSteer().getDirection();
        masterController.registerPlayerDirection(message.getSenderId(), direction);
    }

    private void handleError(GameMessage message, InetSocketAddress address){
        String errorMessage = message.getError().getErrorMessage();
        clientController.error(errorMessage);
    }

    private void handleRoleChange(GameMessage message, InetSocketAddress address){
        RoleChangeMsg roleChangeMsg = message.getRoleChange();
        if(roleChangeMsg.getSenderRole() == NodeRole.MASTER){
            clientController.setMasterAddress(address);
        }

        if(roleChangeMsg.getSenderRole() == NodeRole.VIEWER){
            masterController.removePlayer(message.getSenderId());
        }

        if(roleChangeMsg.getReceiverRole() == NodeRole.DEPUTY){
            clientController.setRole(NodeRole.DEPUTY);
        }

        if(roleChangeMsg.getReceiverRole() == NodeRole.MASTER){
            clientController.becomeMaster();
        }
    }

    private void initHandlers(){
        handlers.put(TypeCase.ACK, this::handleAck);
        handlers.put(TypeCase.JOIN, this::handleJoinPlay);
        handlers.put(TypeCase.PING, this::handlePing);
        handlers.put(TypeCase.STATE, this::handleState);
        handlers.put(TypeCase.ERROR, this::handleError);
        handlers.put(TypeCase.STEER, this::handleSteer);
        handlers.put(TypeCase.ROLE_CHANGE, this::handleRoleChange);
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
