package com.company;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client implements Runnable {
    private String nickName;
    private int listenOnPort;
    private int talkOnPort = 6789;
    private boolean readyToStart;
    private String channel;
    private DataOutputStream outToServer;

    public Client(String nick) {
        nickName = nick;
        listenOnPort = 6787;
        readyToStart = false;
        channel = "default";
    }

    @Override
    public void run() {
        try {
            if (Thread.currentThread().getName().equalsIgnoreCase("listener")) {
                this.listenToServer();
            } else {
                this.talkToServer();
            }
        } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

    }

    public boolean initializeConnection() throws Exception {
        MessagePacket packet;
        String input = "";
        Socket clientSocket = new Socket("localhost", talkOnPort);
        outToServer = new DataOutputStream(clientSocket.getOutputStream());

        //Send message establishing link
        packet = new MessagePacket(MessageOpcode.openConnection, "", nickName);
        outToServer.writeBytes(packet.formatMessage() + '\n');
        outToServer.flush();

        //Get data from server
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        //Read line from server
        if((input = inFromServer.readLine()) != null) {
            //Format it correctly
            packet = new MessagePacket(input);
        } else {
            return false;
        }
        inFromServer.close();

        clientSocket.close();
        listenOnPort = Integer.parseInt(packet.getMessage());

        readyToStart = true;

        return true;
    }

    public int talkToServer() throws Exception {
        String input;
        String splitMessage[];
        MessagePacket packet;

        if ("".equals(nickName))
            return -2;

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        while(!readyToStart) {
            Thread.currentThread().sleep(100);
        }
        do {
            input = inFromUser.readLine();

            Socket clientSocket = new Socket("localhost", talkOnPort);

            outToServer = new DataOutputStream(clientSocket.getOutputStream());

            if(input == null) {
                packet = new MessagePacket("ERROR%" + input);
            } else if(input.charAt(0) == '/') {
                if (input.contains("join")) {
                    splitMessage = input.split(" ", 2);
                    packet = new MessagePacket(MessageOpcode.joinChannel, splitMessage[1], nickName);
                } else if (input.contains("leave")) {
                    splitMessage = input.split(" ", 2);
                    packet = new MessagePacket(MessageOpcode.leaveChannel, splitMessage[1], nickName);
                } else if(input.contains("list")) {
                    packet = new MessagePacket(MessageOpcode.listChannelsRequest, "list", nickName);
                } else if(input.contains("help")) {
                    packet = new MessagePacket(MessageOpcode.stillHerePing, "HERE", nickName);
                    System.out.println("\"/join [CHANNEL NAME]\" - join an existing channel or create and join a new channel.");
                    System.out.println("\"/leave\" - leave your current channel.");
                    System.out.println("\"/list\" - list all channels currently in use.");
                } else {
                    packet = new MessagePacket("ERROR%" + input);
                }
            } else {
                //packet = new MessagePacket("SENDM%" + nickName  + "#%" + input);
                packet = new MessagePacket(MessageOpcode.sendMessage, input, nickName);
            }

            outToServer.writeBytes(packet.formatMessage() + '\n');
            outToServer.flush();
            clientSocket.close();

        } while(!"exit".equalsIgnoreCase(input));

        if("exit".equalsIgnoreCase(input)) {
            throw new RuntimeException("Pack it in, we're done here.");
        }
        return 0;
    }

    public void listenToServer() throws Exception {
        String input;
        MessagePacket packet;
        MessagePacket stillHere = new MessagePacket(MessageOpcode.stillHerePing, "HERE", nickName);

        while(!readyToStart) {
            Thread.currentThread().sleep(100);
        }

        System.out.println(listenOnPort);
        Socket clientSocket = new Socket("localhost", listenOnPort);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        while(true) {
            input = "";
            //Read line from server
            while((input = inFromServer.readLine()) != null) {
                packet = new MessagePacket(input);
                //Process it
                processPacket(packet);
            }
            inFromServer.close();
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }
    }

    private void processPacket(MessagePacket packet) {
        switch(packet.getOpcode()) {
            case error:
                System.out.println("Someone is a big crybaby, or maybe something is really wrong. I should look into that...");
                break;
            case sendMessage:
                System.out.println("Error: Client received packet with server-only opcode \"sendMessage\".");
                break;
            case distributeMessage:
                System.out.println(packet.getUserName() + ": " + packet.getMessage());
                break;
            case joinChannel:
                System.out.println("Error: Client received packet with server-only opcode \"joinChannel\".");
                break;
            case leaveChannel:
                System.out.println("Error: Client received packet with server-only opcode \"leaveChannel\".");
                break;
            case listChannelsRequest:
                System.out.println("Error: Client received packet with server-only opcode \"listChannelsRequest\".");
                break;
            case listChannelsResponse:
                System.out.println("List of channels:");
                String[] channelList = packet.getMessage().split("#");
                for(int i = 0; i < channelList.length; ++i) {
                    System.out.println("" + (i+1) + ") " + channelList[i]);
                }
                break;
            case openConnection:
                System.out.println("Error: Client received packet with server-only opcode \"openConnection\".");
                break;
            case closeConnection:
                System.out.println("Error: Client received packet with server-only opcode \"closeConnection\".");
                break;
            case stillHerePing:
                try {
                    Socket clientSocket = new Socket("localhost", talkOnPort);
                    outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    packet.setMessage("HERE");
                    outToServer.writeBytes(packet.formatMessage() + '\n');
                    outToServer.flush();
                    //clientSocket.close();
                } catch (Exception e) {
                    System.out.println("Failed to send reply ping to server.");
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}






