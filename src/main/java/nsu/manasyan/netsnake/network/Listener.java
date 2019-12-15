package nsu.manasyan.netsnake.network;

import nsu.manasyan.netsnake.Wrappers.Player;
import nsu.manasyan.netsnake.contexts.SentMessagesKey;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.controllers.MasterController;
import nsu.manasyan.netsnake.util.GameExecutorService;
import nsu.manasyan.netsnake.contexts.MessageContext;
import nsu.manasyan.netsnake.proto.SnakesProto;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
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

    private Map<SentMessagesKey, MessageContext> sentMessages;

    private Map<TypeCase, Handler> handlers = new HashMap<>();

    private byte[] receiveBuf = new byte[BUF_LENGTH];

    private Sender sender;

    private MulticastSocket socket;

    private InetAddress multicastAddress;

    private volatile boolean isInterrupted = false;

    private volatile long joinMsgSeq = -1;

    public Listener( Sender sender, Map<SentMessagesKey, MessageContext> sentMessages, MulticastSocket socket, InetAddress multicastAddress) {
        this.multicastAddress = multicastAddress;
        this.sender = sender;
        this.sentMessages = sentMessages;
        this.socket = socket;
        initHandlers();
        sender.registerJoinMsgSeqListeners(jms -> this.joinMsgSeq = jms);
    }

    public void listen(){
        GameExecutorService.getExecutorService().submit(() -> {
            GameMessage message;
            TypeCase type;

            DatagramPacket packetToReceive = new DatagramPacket(receiveBuf, BUF_LENGTH);
            isInterrupted = false;
            try {
                socket.joinGroup(multicastAddress);
                while (!isInterrupted) {
                    socket.receive(packetToReceive);
                    message = SnakesProto.GameMessage.parseFrom(Arrays.copyOf(receiveBuf, packetToReceive.getLength()));

                    type = message.getTypeCase();
                    if(type!=TypeCase.PING)
                        System.out.println("[" + message.getMsgSeq() + "] Received type: " + type);

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

    public void reload(){
        joinMsgSeq = -1;
    }

    private void handleJoinPlay(GameMessage message, InetSocketAddress address){
        System.out.println("JOIN ADDress: " + address);
        JoinMsg joinMsg = message.getJoin();
        NodeRole role = (!joinMsg.hasOnlyView() || !joinMsg.getOnlyView()) ? NodeRole.NORMAL : NodeRole.VIEWER;
        Player player = new Player(joinMsg.getName(),
                address.getHostName(), address.getPort(), role, 0);
        System.out.println("ADDRESS: " + player.getIpAddress() + " : " + player.getPort());

        masterController.addPlayer(player);
        sender.sendAck(address, masterController.getAvailablePlayerId() - 1, message.getMsgSeq());
    }

    private void handleState(GameMessage message, InetSocketAddress address){
        clientController.setGameState(message.getState().getState());
        clientController.updateAllFullPoints();

        sender.sendAck(address, clientController.getMasterId(), message.getMsgSeq());
    }

    private void handleAck(GameMessage message, InetSocketAddress address){
        if(joinMsgSeq == message.getMsgSeq()) {
            System.out.println("GET JOIN ACK");
            clientController.setPlayerId(message.getReceiverId());
        }
//        sentMessages.remove(new SentMessagesKey(message.getMsgSeq(), message.getSenderId()));

        System.out.println(message);
        sentMessages.entrySet().removeIf(e -> e.getKey().getMsgSeq() == message.getMsgSeq() &&
                e.getKey().getPlayerId() == message.getSenderId());
    }

    private void handlePing(GameMessage message, InetSocketAddress address){
        clientController.setPlayerAlive(message.getSenderId());
    }

    private void handleSteer(GameMessage message, InetSocketAddress address){
        Direction direction = message.getSteer().getDirection();
        masterController.registerPlayerDirection(message.getSenderId(), direction);

        sender.sendAck(address, clientController.getMasterId(), message.getMsgSeq());
    }

    private void handleError(GameMessage message, InetSocketAddress address){
        String errorMessage = message.getError().getErrorMessage();
        clientController.error(errorMessage);

        sender.sendAck(address, clientController.getMasterId(), message.getMsgSeq());
    }

    private void handleRoleChange(GameMessage message, InetSocketAddress address){
        RoleChangeMsg roleChangeMsg = message.getRoleChange();
        if(roleChangeMsg.getSenderRole() == NodeRole.MASTER){
            clientController.setMasterAddress(address);
        }

        if(roleChangeMsg.getSenderRole() == NodeRole.VIEWER){
            masterController.removePlayer(message.getSenderId());
//            masterController.setPlayerAsViewer(message.getSenderId());
        }

        if(roleChangeMsg.getReceiverRole() == NodeRole.DEPUTY){
            clientController.setRole(NodeRole.DEPUTY);
        }

        if(roleChangeMsg.getReceiverRole() == NodeRole.MASTER){
            clientController.becomeMaster();
        }

        sender.sendAck(address, clientController.getMasterId(), message.getMsgSeq());
    }

    private void handleAnnouncement(GameMessage message, InetSocketAddress address){
        clientController.addAvailableGame(message.getAnnouncement(), address);
    }

    private void initHandlers(){
        handlers.put(TypeCase.ACK, this::handleAck);
        handlers.put(TypeCase.JOIN, this::handleJoinPlay);
        handlers.put(TypeCase.PING, this::handlePing);
        handlers.put(TypeCase.STATE, this::handleState);
        handlers.put(TypeCase.ERROR, this::handleError);
        handlers.put(TypeCase.STEER, this::handleSteer);
        handlers.put(TypeCase.ANNOUNCEMENT, this::handleAnnouncement);
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
