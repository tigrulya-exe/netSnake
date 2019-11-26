package nsu.manasyan.netsnake.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import nsu.manasyan.netsnake.contexts.AnnouncementContext;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.proto.SnakesProto.*;
import nsu.manasyan.netsnake.proto.SnakesProto.GameMessage.*;

public class MulticastListener implements Runnable{
    private static final int TIMER_PERIOD_MS = 1000;

    private static final int BUF_LENGTH = 65000;

    private byte[] receiveBuf = new byte[BUF_LENGTH];

    private Timer timer = new Timer();

    private Map<AnnouncementMsg, AnnouncementContext> availableGames = new ConcurrentHashMap<>();

    private InetAddress multicastAddress;

    private MulticastSocket socket;

    private ClientController controller = ClientController.getInstance();

    public MulticastListener(MulticastSocket socket, InetAddress multicastAddress) {
        this.multicastAddress = multicastAddress;
        setTimer();
    }

    private void setTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                availableGames.entrySet().removeIf(entry -> !entry.getValue().isActual());
                availableGames.forEach((k,v) -> {
                    v.setActual(false);
                    availableGames.put(k,v);});
            }
        };

        timer.schedule(task, TIMER_PERIOD_MS, TIMER_PERIOD_MS);
    }

    @Override
    public void run() {
        DatagramPacket packetToReceive = new DatagramPacket(receiveBuf, BUF_LENGTH);
        try{
            socket.joinGroup(multicastAddress);
            while (true) {
                socket.receive(packetToReceive);
                GameMessage message = GameMessage.parseFrom(packetToReceive.getData());
                handleGameAnnouncement(message.getAnnouncement(),(InetSocketAddress) packetToReceive.getSocketAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGameAnnouncement(AnnouncementMsg message, InetSocketAddress socketAddress){
        if(!availableGames.containsKey(message)){
            availableGames.put(message,new AnnouncementContext(socketAddress));
        }
    }
}
