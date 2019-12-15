package nsu.manasyan.netsnake.network;

import nsu.manasyan.netsnake.contexts.MessageContext;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.controllers.MasterController;
import nsu.manasyan.netsnake.proto.SnakesProto.*;
import nsu.manasyan.netsnake.util.GameObjectBuilder;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Sender {
    private static final int MAX_MSG_BUF_SIZE = 4096;

    private static final int MASTER_ID = 0;

    private InetAddress multicastAddress;

    private MasterController masterController = MasterController.getInstance();

    private ClientController clientController = ClientController.getInstance();

    private Map<Long, MessageContext> sentMessages;

    private MulticastSocket socket;

    private int multicastPort = 9192;

    private volatile boolean needToSendPing = true;

    private Timer timer;

    public Sender( MulticastSocket socket, Map<Long, MessageContext> sentMessages, InetAddress multicastAddress) {
        this.socket = socket;
        this.sentMessages = sentMessages;
        this.multicastAddress = multicastAddress;
    }

    public void broadcastMessage(GameMessage message) {
        needToSendPing = false;
        System.out.println("Broadcast: ");
//        System.out.println(message);

//        GameExecutorService.getExecutorService().submit(() ->
                masterController.getPlayers().forEach(player -> {
                    try {
                        if (player.getId() == MASTER_ID) {
                            return;
                        }
                        System.out.println("addr: " + player.getIpAddress());
                        byte[] buf = message.toByteArray();

                        InetSocketAddress socketAddress = new InetSocketAddress(player.getIpAddress(), player.getPort());
                        socket.send(new DatagramPacket(buf, buf.length, socketAddress));
                        putIntoSentMessages(message.getMsgSeq(), new MessageContext(message, socketAddress));
                    } catch (IOException | IllegalArgumentException e) {
                        System.out.println("EXCEPTION: " + e.getLocalizedMessage());
                        e.printStackTrace();
                    }

                });
    //);
    }

    public void broadcastAnnouncement(GameMessage announcementMsg) {
        try {
            System.out.println("Announcement:");
            byte[] buf = announcementMsg.toByteArray();
            socket.send(new DatagramPacket(buf, buf.length, multicastAddress, multicastPort));
        } catch (IOException e) {
            System.out.println("Error broadcasting announcement");
            e.printStackTrace();
        }
    }

    public void sendMessage(InetSocketAddress receiverAddress, GameMessage message) {
        needToSendPing = false;

        System.out.println("Send to " + receiverAddress + ": ");
        System.out.println(message);
        try {
            byte[] buf = message.toByteArray();
            socket.send(new DatagramPacket(buf, buf.length, receiverAddress));
        } catch (IOException e){
            System.out.println(receiverAddress);
        }
    }

    public Map<Long, MessageContext> getSentMessages() {
        return sentMessages;
    }

    public void setMasterTimer(int pingDelayMs, int nodeTimeoutMs){
        timer = new Timer();
        GameMessage announcementMsg = GameObjectBuilder.getAnnouncementMessage();

        TimerTask masterSendPing  = new TimerTask() {
            @Override
            public void run() {
                if(!needToSendPing){
                    needToSendPing = true;
                    return;
                }
                GameMessage ping =  GameObjectBuilder.initPingMessage();
                broadcastMessage(ping);
            }
        };

        TimerTask broadcastAnnouncement  = new TimerTask() {
            @Override
            public void run() {
                broadcastAnnouncement(announcementMsg);
            }
        };

        TimerTask checkAlivePlayers = getCheckAlivePlayersTask();

        timer.schedule(masterSendPing, pingDelayMs, pingDelayMs);
        timer.schedule(checkAlivePlayers, nodeTimeoutMs, nodeTimeoutMs);
        timer.schedule(broadcastAnnouncement, 1000, 1000);
    }

    public void stop(){
        if(timer != null)
            timer.cancel();
    }

    public void setClientTimer(InetSocketAddress masterAddress, int pingDelayMs, int nodeTimeoutMs){
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
                sendMessage(masterAddress, ping);
            }
        };

        TimerTask checkMaster = new TimerTask() {
            @Override
            public void run() {
                if(!clientController.isMasterAlive()){
                    clientController.changeMaster();
                    return;
                }

                clientController.setMasterAlive(false);
            }
        };

        timer.schedule(playerSendPing, pingDelayMs, pingDelayMs);
        timer.schedule(checkMaster, nodeTimeoutMs, nodeTimeoutMs);
    }

    private void putIntoSentMessages(long msgId, MessageContext context){
        if(sentMessages.size() >= MAX_MSG_BUF_SIZE){
            return;
        }

        sentMessages.put(msgId, context);
    }

    private TimerTask getCheckAlivePlayersTask(){
        return new TimerTask() {
            @Override
            public void run() {
                var alivePlayers = masterController.getAlivePlayers();

                for(var iter = alivePlayers.entrySet().iterator(); iter.hasNext(); ) {
                    var entry = iter.next();
                    if(entry.getValue()) {
                        alivePlayers.put(entry.getKey(), false);
                        continue;
                    }
                    iter.remove();
                }
            }
        };
    }
}
