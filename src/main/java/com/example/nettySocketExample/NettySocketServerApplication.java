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

        socketIOServer.addConnectListener(client -> {
            System.out.println("Client connected: " + client.getSessionId());

            Player player = Player.builder()
                    .rotation(0)
                    .x(300)
                    .y(300)
                    .playerId(client.getSessionId().toString())
                    .team("red").build();

            playerMap.put(client.getSessionId().toString(), player);
            client.sendEvent("currentPlayers", playerMap);

            socketIOServer.getBroadcastOperations().sendEvent("newPlayer", player);
        });

        socketIOServer.addEventListener("playerMovement", PlayerMovementData.class,
                (client, movementData, ackSender) -> {

            System.out.println("Player Movement: " + movementData.getX() + ", " + movementData.getY());

            Player player = playerMap.get(client.getSessionId().toString());
            player.setX(movementData.getX());
            player.setY(movementData.getY());
            player.setRotation(movementData.getRotation());

            socketIOServer.getBroadcastOperations().sendEvent("playerMoved", playerMap);
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