package nsu.manasyan.netsnake.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

import nsu.manasyan.netsnake.out.SnakesProto.*;
import nsu.manasyan.netsnake.out.SnakesProto.GameMessage.*;

public class MulticastListener implements Runnable{
    private static final int TIMER_PERIOD_MS = 1000;

    private static final int BUF_LENGTH = 65000;

    private Map<AnnouncementMsg, Boolean> availableGames = new HashMap<>();

    private byte[] receiveBuf = new byte[BUF_LENGTH];

    private Timer timer = new Timer();

    private InetAddress multicastAddress;

    public MulticastListener(InetAddress multicastAddress) {
        this.multicastAddress = multicastAddress;
        setTimer();
    }

    private void setTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                availableGames.entrySet().removeIf(entry -> !entry.getValue());
                availableGames.forEach((k,v) -> availableGames.put(k,false));
            }
        };

        timer.schedule(task, TIMER_PERIOD_MS, TIMER_PERIOD_MS);
    }

    @Override
    public void run() {
        DatagramPacket packetToReceive = new DatagramPacket(receiveBuf, BUF_LENGTH);
        try(MulticastSocket socket = new MulticastSocket()){
            socket.joinGroup(multicastAddress);
            while (true) {
                socket.receive(packetToReceive);
                GameMessage message = GameMessage.parseFrom(packetToReceive.getData());
                handleGameAnnouncement(message.getAnnouncement());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGameAnnouncement(AnnouncementMsg message){
        if(!availableGames.containsKey(message)){
            availableGames.put(message,true);
        }
    }
}
