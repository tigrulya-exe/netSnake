package nsu.manasyan.netsnake.network;

import nsu.manasyan.netsnake.contexts.MessageContext;
import nsu.manasyan.netsnake.contexts.SentMessagesKey;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.controllers.MasterController;
import nsu.manasyan.netsnake.observable.Observable;
import nsu.manasyan.netsnake.proto.SnakesProto.*;
import nsu.manasyan.netsnake.util.GameObjectBuilder;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static nsu.manasyan.netsnake.util.GameObjectBuilder.*;

public class Sender {
    private static final int MAX_MSG_BUF_SIZE = 4096;

    private InetAddress multicastAddress;

    private MasterController masterController = MasterController.getInstance();

    private ClientController clientController = ClientController.getInstance();

    private Map<SentMessagesKey, MessageContext> sentMessages;

    private MulticastSocket socket;

    private int multicastPort = 9192;

    private volatile boolean needToSendPing = false;

    private volatile Observable<Long> joinMsgSeq = new Observable<>(0L);

    private Timer timer;

    public Sender( MulticastSocket socket, Map<SentMessagesKey, MessageContext> sentMessages, InetAddress multicastAddress) {
        this.socket = socket;
        this.sentMessages = sentMessages;
        this.multicastAddress = multicastAddress;
    }

    public void broadcastState(GameMessage message){
        broadcastMessage(message, true);
    }

    public void broadcastMessage(GameMessage message, boolean isConfirmNeed) {
        needToSendPing = false;
        int masterId = clientController.getMasterId();
        if (masterController.getPlayers().size() == 1) {
            return;
        }
        System.out.println("Broadcast: " + message.getTypeCase());

//        GameExecutorService.getExecutorService().submit(() ->
        masterController.getPlayers().forEach(player -> {
            try {
                if (player.getId() == masterId) {
                    return;
                }
                System.out.println("addr: " + player.getIpAddress());
                byte[] buf = message.toByteArray();

                InetSocketAddress socketAddress = new InetSocketAddress(player.getIpAddress(), player.getPort());
                socket.send(new DatagramPacket(buf, buf.length, socketAddress));

                if (isConfirmNeed)
                    putIntoSentMessages(message, socketAddress, player.getId());
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("EXCEPTION: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        });
        //);
    }

    public void broadcastAnnouncement(GameMessage announcementMsg) {
        needToSendPing = false;
        try {
            byte[] buf = announcementMsg.toByteArray();
            socket.send(new DatagramPacket(buf, buf.length, multicastAddress, multicastPort));
        } catch (IOException e) {
            System.out.println("Error broadcasting announcement");
            e.printStackTrace();
        }
    }

    public boolean sendMessage(InetSocketAddress receiverAddress, GameMessage message, boolean isConfirmNeed) {
        needToSendPing = false;

        System.out.println("Send to " + receiverAddress + " : " + message.getTypeCase());
        try {
            byte[] buf = message.toByteArray();
            socket.send(new DatagramPacket(buf, buf.length, receiverAddress));
        } catch (IOException e){
            System.out.println(receiverAddress);
            return false;
        }

        return true;
    }

    public void setMasterTimer(int pingDelayMs, int nodeTimeoutMs){
        timer = new Timer();

        TimerTask masterSendPing  = new TimerTask() {
            @Override
            public void run() {
                if(!needToSendPing){
                    needToSendPing = true;
                    return;
                }
                GameMessage ping =  GameObjectBuilder.initPingMessage();
                broadcastMessage(ping, false);
            }
        };

        TimerTask broadcastAnnouncement  = new TimerTask() {
            @Override
            public void run() {
                GameMessage announcementMsg = GameObjectBuilder.getAnnouncementMessage();
                broadcastAnnouncement(announcementMsg);
            }
        };

        timer.schedule(masterSendPing, pingDelayMs, pingDelayMs);
        timer.schedule(getCheckAlivePlayersTask(), nodeTimeoutMs, nodeTimeoutMs);
        timer.schedule(broadcastAnnouncement, 1000, 1000);
        timer.schedule(getCheckSentMessagesTask(), nodeTimeoutMs/2, nodeTimeoutMs/2);
    }

    public void stop(){
        if(timer != null)
            timer.cancel();
    }

    public void sendJoin(InetSocketAddress receiverAddress, GameMessage message){
        int masterId = clientController.getMasterId();
        joinMsgSeq.updateValue(message.getMsgSeq());

        if(sendMessage(receiverAddress, message, true))
            putIntoSentMessages(message, receiverAddress, masterId);
    }

    public void sendAck(InetSocketAddress receiverAddress, int receiverId, long msgSeq){
        GameMessage ackMessage = getAckMsg(clientController.getPlayerId(), receiverId, msgSeq);
        System.out.println("[" + msgSeq + "] ACk: " + receiverAddress);
        sendMessage(receiverAddress, ackMessage, false);
    }

    public void sendConfirmRequiredMessage(InetSocketAddress receiverAddress, GameMessage message, int receiverId){
        if(sendMessage(receiverAddress, message, true))
            putIntoSentMessages(message, receiverAddress, receiverId);
    }

    public void setClientTimer(InetSocketAddress masterAddress){
        timer = new Timer();
        var clientController = ClientController.getInstance();

        TimerTask playerSendPing  = new TimerTask() {
            @Override
            public void run() {
                if(!needToSendPing){
                    needToSendPing = true;
                    return;
                }
                GameMessage ping = GameObjectBuilder.initPingMessage();
                sendMessage(masterAddress, ping, false);
            }
        };

        TimerTask checkMaster = new TimerTask() {
            @Override
            public void run() {
                if(clientController.isMasterAlive()){
                    clientController.setMasterAlive(false);
                    return;
                }

                clientController.changeMaster();
            }
        };

        int nodeTimeoutMs = clientController.getConfig().getNodeTimeoutMs();
        int pingDelayMs = clientController.getConfig().getPingDelayMs();

        timer.schedule(playerSendPing, pingDelayMs, pingDelayMs);
        timer.schedule(checkMaster, nodeTimeoutMs, nodeTimeoutMs);
        timer.schedule(getCheckSentMessagesTask(), nodeTimeoutMs/2, nodeTimeoutMs/2);
    }

    private void putIntoSentMessages(GameMessage message, InetSocketAddress address, int receiverId){
        if(sentMessages.size() >= MAX_MSG_BUF_SIZE){
            return;
        }

        MessageContext context = new MessageContext(message, address);
        SentMessagesKey key = new SentMessagesKey(message.getMsgSeq(), receiverId);
        sentMessages.put(key, context);
    }

    private TimerTask getCheckAlivePlayersTask(){
        int masterId = clientController.getMasterId();
        return new TimerTask() {
            @Override
            public void run() {
                var alivePlayers = masterController.getAlivePlayers();

                for(var iter = alivePlayers.entrySet().iterator(); iter.hasNext(); ) {
                    var entry = iter.next();
                    if(entry.getValue() || entry.getKey() == masterId) {
                        alivePlayers.put(entry.getKey(), false);
                        continue;
                    }

                    masterController.removePlayer(entry.getKey());
                    iter.remove();
                }
            }
        };
    }

    private TimerTask getCheckSentMessagesTask(){
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    for (var iter = sentMessages.entrySet().iterator(); iter.hasNext(); ) {
                        var entry = iter.next();
                        var msgContext = entry.getValue();
                        if (msgContext.isFresh()) {
                            msgContext.setFresh(false);
                            continue;
                        }
                        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(msgContext.getAddress()), msgContext.getPort());
                        sendMessage(address, msgContext.getMessage(), true);

                        iter.remove();
                    }
                } catch(UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void registerJoinMsgSeqListeners(Observable.ValueListener<Long> listener) {
        joinMsgSeq.registerValueListener(listener);
    }
}
