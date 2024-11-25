package com.example.nettySocketExample;

import com.corundumstudio.socketio.SocketIOServer;
import com.example.nettySocketExample.object.Player;
import com.example.nettySocketExample.object.PlayerMovementData;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Component
public class NettySocketServerApplication implements CommandLineRunner {

    public final SocketIOServer socketIOServer;
    Map<String, Player> playerMap = new HashMap<>();

    public NettySocketServerApplication(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;
    }

    @Override
    public void run(String... args) {

        socketIOServer.start();

        // broadcast each 50ms
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(50);
                    socketIOServer.getBroadcastOperations().sendEvent("state", playerMap);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();

        // first connection with client
        socketIOServer.addConnectListener(client -> {
            System.out.println("Client connected: " + client.getSessionId());

            Player player = Player.builder()
                    .playerId(client.getSessionId().toString())
                    .x(300)
                    .y(300)
                    .build();

            playerMap.put(client.getSessionId().toString(), player);
            client.sendEvent("currentPlayer", player);
        });

        socketIOServer.addEventListener("playerMove", PlayerMovementData.class,
                (client, data, ackSender) -> {
            System.out.println("playerMove: " + client.getSessionId());
            String playerId = client.getSessionId().toString();
            Player player = playerMap.get(playerId);
            if(player != null) {
                player.setX(data.getX());
                player.setY(data.getY());
                playerMap.put(playerId, player);
            }
        });

        socketIOServer.addDisconnectListener(client -> {
            System.out.println("Client.disconnected: " + client.getSessionId());
        });
    }

    @PreDestroy
    public void stop() {
        System.out.println("System stop");
        socketIOServer.stop();
    }
}