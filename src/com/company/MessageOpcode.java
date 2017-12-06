package com.company;

public enum MessageOpcode {
    sendMessage,
    distributeMessage,
    joinChannel,
    leaveChannel,
    error,
    listChannelsRequest,
    listChannelsResponse,
    openConnection,
    closeConnection,
    stillHerePing;

    static public MessageOpcode opcodeBuilder(String code) {
        if(code.equalsIgnoreCase("error"))
            return error;
        else if(code.equalsIgnoreCase("sendm"))
            return sendMessage;
        else if(code.equalsIgnoreCase("distm"))
            return distributeMessage;
        else if(code.equalsIgnoreCase("joinc"))
            return joinChannel;
        else if(code.equalsIgnoreCase("exitc"))
            return leaveChannel;
        else if(code.equalsIgnoreCase("listcreq"))
            return listChannelsRequest;
        else if(code.equalsIgnoreCase("listcres"))
            return listChannelsResponse;
        else if(code.equalsIgnoreCase("opencon"))
            return openConnection;
        else if(code.equalsIgnoreCase("closcon"))
            return closeConnection;
        else if(code.equalsIgnoreCase("online"))
            return stillHerePing;
        else
            return null;
    }
}
