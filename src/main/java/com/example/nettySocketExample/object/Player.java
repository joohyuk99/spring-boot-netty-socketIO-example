package com.example.nettySocketExample.object;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class Player {
    private float rotation;
    private float x, y;
    private String playerId;
    private String team;
}
