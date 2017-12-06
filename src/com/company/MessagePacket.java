package com.company;


import static com.company.MessageOpcode.*;

public class MessagePacket {
    private MessageOpcode opcode;
    private String message;
    private String userName;

    public MessagePacket() {
        opcode = error;
        message = null;
    }

    public MessagePacket(MessageOpcode type, String message, String nickName) {
        this.message = message;
        opcode = type;
        userName = nickName;
    }

    public MessagePacket(String inputString) {
        String splitMessage[] = inputString.split("%", 2);
        String userAndMessage[];
        if(splitMessage[0] == null)
            return;
        switch(opcodeBuilder(splitMessage[0])) {
            case error:
                opcode = error;
                message = splitMessage[1];
                break;
            case sendMessage:
                opcode = sendMessage;
                userAndMessage = splitMessage[1].split("#%", 2);
                userName = userAndMessage[0];
                message = userAndMessage[1];
                break;
            case distributeMessage:
                opcode = distributeMessage;
                userAndMessage = splitMessage[1].split("#%", 2);
                userName = userAndMessage[0];
                message = userAndMessage[1];
                break;
            case joinChannel:
                opcode = joinChannel;
                userAndMessage = splitMessage[1].split("#%", 2);
                userName = userAndMessage[0];
                message = userAndMessage[1];
                break;
            case leaveChannel:
                opcode = leaveChannel;
                userAndMessage = splitMessage[1].split("#%", 2);
                userName = userAndMessage[0];
                message = userAndMessage[1];
                break;
            case listChannelsRequest:
                opcode = listChannelsRequest;
                userAndMessage = splitMessage[1].split("#%", 2);
                userName = userAndMessage[0];
                message = userAndMessage[1];
                break;
            case listChannelsResponse:
                opcode = listChannelsResponse;
                userAndMessage = splitMessage[1].split("#%", 2);
                userName = userAndMessage[0];
                message = userAndMessage[1];
                break;
            case openConnection:
                opcode = openConnection;
                userAndMessage = splitMessage[1].split("#%", 2);
                userName = userAndMessage[0];
                message = userAndMessage[1];
                break;
            case closeConnection:
                opcode = closeConnection;
                userAndMessage = splitMessage[1].split("#%", 2);
                userName = userAndMessage[0];
                message = userAndMessage[1];
                break;
            case stillHerePing:
                opcode = stillHerePing;
                userAndMessage = splitMessage[1].split("#%", 2);
                userName = userAndMessage[0];
                message = userAndMessage[1];
                break;
            default:
                opcode = error;
                break;
        }

    }

    public String formatMessage() {
        String formattedString;

        switch(opcode) {
            case error:
                formattedString = "ERROR" + "%" + message;
                break;
            case sendMessage:
                formattedString = "SENDM" + "%" + userName + "#%" + message;
                break;
            case distributeMessage:
                formattedString = "DISTM" + "%" + userName + "#%" + message;
                break;
            case joinChannel:
                formattedString = "JOINC" + "%" + userName + "#%" + message;
                break;
            case leaveChannel:
                formattedString = "EXITC" + "%" + userName + "#%" + message;
                break;
            case listChannelsRequest:
                formattedString = "LISTCREQ" + "%" + userName + "#%" + message;
                break;
            case listChannelsResponse:
                formattedString = "LISTCRES" + "%" + userName + "#%" + message;
                break;
            case openConnection:
                formattedString = "OPENCON" + "%" + userName + "#%" + message;
                break;
            case closeConnection:
                formattedString = "CLOSCON" + "%" + userName + "#%" + message;
                break;
            case stillHerePing:
                formattedString = "ONLINE" + "%" + userName + "#%" + message;
                break;
            default:
                formattedString = "ERROR" + "%" + userName + "#%" + message;
                break;
        }
        return formattedString;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public MessageOpcode getOpcode() {
        return opcode;
    }

    public void setOpcode(MessageOpcode opcode) {
        this.opcode = opcode;
    }
}
