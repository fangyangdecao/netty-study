package com.yhy.im.protocol.command;

import lombok.Data;

import static com.yhy.im.protocol.command.Command.LOGIN_REQUEST;

@Data
public class LoginRequestPacket extends Packet{
    private String userId;

    private String username;

    private String password;


    @Override
    public Byte getCommand() {
        return LOGIN_REQUEST;
    }
}
