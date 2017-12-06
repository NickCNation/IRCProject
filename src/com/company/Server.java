package com.company;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static com.company.MessageOpcode.*;

public class Server implements Runnable {
    private Map<String, User> users;
    private List<String> channels;
    private Map<String,List<User>> channelMembers;
    private List<MessagePacket> messageQueue;
    private int mainPort = 6789;
    private int nextPort;
    private Map<User, ServerSocket> serverSocketMap;
    private Map<User, Socket> socketMap;
    private Map<User, DataOutputStream> outputStreamMap;

    public Server() {
        users = new HashMap<String, User>();
        channels = new ArrayList<String>();
        channelMembers = new HashMap<String, List<User>>();
        channels.add("default");
        messageQueue = new ArrayList<MessagePacket>();
        List<User> empty = new ArrayList<User>();
        channelMembers.put("default", empty);
        nextPort = 6790;

        serverSocketMap = new HashMap<User, ServerSocket>();
        socketMap = new HashMap<User, Socket>();
        outputStreamMap = new HashMap<User, DataOutputStream>();
    }

    @Override
    public void run() {
        try {
            if (Thread.currentThread().getName().equalsIgnoreCase("listener")) {
                this.Listen();
            } else {
                this.HandleStuff();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private int Listen() throws Exception {
        String clientSentence;
        MessagePacket packet;
        User newUser;

        ServerSocket welcomeSocket = new ServerSocket(mainPort, 100);
        Socket connectionSocket = welcomeSocket.accept();
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        DataOutputStream confirmConnection = new DataOutputStream(connectionSocket.getOutputStream());

        while(true) {
            clientSentence = "";
            while((clientSentence = inFromClient.readLine()) != null) {
                packet = new MessagePacket(clientSentence);
                if(packet.getOpcode() == openConnection) {
                    confirmConnection = new DataOutputStream(connectionSocket.getOutputStream());
                    newUser = new User(packet.getUserName(), nextPort, "default");
                    channelMembers.get("default").add(newUser);
                    packet.setMessage(Integer.toString(nextPort));
                    nextPort += 1;
                    users.put(newUser.getUsername(), newUser);
                    confirmConnection.writeBytes(packet.formatMessage() + '\n');
                    confirmConnection.flush();

                    /*try Idea 1)
                    ServerSocket outputSocket = new ServerSocket(newUser.getPort());
                    Socket returnSocket = outputSocket.accept();*/

                    /*Try idea 2)*/
                    ServerSocket outputSocket = new ServerSocket(newUser.getPort());
                    serverSocketMap.put(newUser, outputSocket);
                    Socket returnSocket = outputSocket.accept();
                    socketMap.put(newUser, returnSocket);
                    DataOutputStream outToClient = new DataOutputStream(returnSocket.getOutputStream());
                    outputStreamMap.put(newUser, outToClient);

                    /*
                    * Some ideas:
                    * 1) Open connections here but don't bother passing them in
                    * 2) Open connections here and have them be private member lists so that I don't need to worry about passing them around.
                    * 3) Open connections here and rewrite processing functions to pass in stuff
                    * ZZ) Users are threads. Users are always waiting to talk.
                    */
                } else {
                    synchronized (messageQueue) {
                        System.out.println(clientSentence);
                        messageQueue.add(packet);
                    }
                }
                //inFromClient.close();
                connectionSocket = welcomeSocket.accept();
                inFromClient = new BufferedReader((new InputStreamReader(connectionSocket.getInputStream())));
            }
        }
    }

    private int HandleStuff() {
        MessagePacket packet;
        int size;
        User newUser;
        Collection<String> allNickNames;
        Object nickNames[];
        Object packets[];
        long timeToCheckUsers = System.currentTimeMillis() + User.updateTime;

        while(true) {
            if(messageQueue.isEmpty()) {
                //Great, time to ping the users.
                allNickNames = users.keySet();
                nickNames = allNickNames.toArray();
                for(int i = 0; i < users.size(); ++i) {
                    packet = new MessagePacket(stillHerePing, "", (String) nickNames[i]);
                    synchronized (messageQueue) {
                        messageQueue.add(packet);
                    }
                }
                try {
                    Thread.currentThread().sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {
                //Empty message queue as fast as possible to minimized synchronized time.
                synchronized (messageQueue) {
                    packets = messageQueue.toArray();
                    messageQueue.clear();
                }

                //Process messages
                for(int i = 0; i < packets.length; ++i) {
                    packet = (MessagePacket) packets[i];
                    if(packet.getOpcode() != stillHerePing) {
                        users.get(packet.getUserName()).updateTimeout();
                    } else {
                        //The HERE message means this is a packet from the client so the update is appropriate..
                        if(packet.getMessage().equalsIgnoreCase("HERE")) {
                            users.get(packet.getUserName()).updateTimeout();
                        }
                    }
                    processPacket(packet, users.get(packet.getUserName()));
                }
            }

            //Handle timeouts
            if(timeToCheckUsers < System.currentTimeMillis()) {
                Collection userCollection = users.values();
                Object checkUser[] = userCollection.toArray();
                User nextUser;
                size = checkUser.length;
                for(int i = 0; i < size; ++i) {
                    nextUser = (User) checkUser[i];
                    if(System.currentTimeMillis() > nextUser.getTimeoutTime()) {
                        //Schedule the timed out user to be disconnected.

                        packet = new MessagePacket(closeConnection, "", nextUser.getUsername());
                        synchronized (messageQueue) {
                            messageQueue.add(packet);
                        }
                    }
                }
            }
        }
    }

    private void processPacket(MessagePacket packet, User sender) {
        String channel;
        MessagePacket toSend;
        List<User> usersInChannel;
        int size;

        List<User> sendTo = new ArrayList<User>();
        switch(packet.getOpcode()) {
            case error:
                if(sender.getUsername() != null) {
                    System.out.println("User " + sender.getUsername() + "is a big crybaby, or maybe something is really wrong. I should look into that...");
                } else {
                    System.out.println("Someone is a big crybaby, or maybe something is really wrong. I should look into that...");
                }
                break;
            case sendMessage:
                channel = sender.getChannel();
                sendTo.addAll(channelMembers.get(channel));
                sendTo.remove(sender);
                size = sendTo.size();
                packet.setOpcode(MessageOpcode.distributeMessage);
                try {
                    for (int i = 0; i < size; ++i) {
                        talk(packet, sendTo.get(i));
                    }
                } catch (Exception e) {
                    System.out.println("Failed to send message \"" + packet.getMessage() + "\"");
                    e.printStackTrace();
                }
                break;
            case distributeMessage:
                System.out.println("Error: server received packet with client-only opcode.");
                break;
            case joinChannel:
                channel = sender.getChannel();
                if(channel != null) {
                    if (channelMembers.get(channel).contains(sender)) {
                        channelMembers.get(channel).remove(sender);
                    }
                }
                channel = packet.getMessage();
                if(channelMembers.containsKey(channel)) {
                    channelMembers.get(channel).add(sender);
                } else {
                    System.out.println("Creating channel: " + channel);
                    channels.add(channel);
                    usersInChannel = new ArrayList<User>();
                    usersInChannel.add(sender);
                    channelMembers.put(channel, usersInChannel);
                }
                sender.setChannel(channel);
                break;
            case leaveChannel:
                channel = sender.getChannel();
                if(channelMembers.containsKey(channel)) {
                    channelMembers.get(channel).remove(sender);
                }
                sender.setChannel(null);
                break;
            case listChannelsRequest:
                size = channels.size();
                channel = "";
                for(int i = 0; i < size; ++i) {
                    channel += channels.get(i);
                    channel += "#";
                }
                toSend = new MessagePacket(MessageOpcode.listChannelsResponse, channel, sender.getUsername());
                try {
                    talk(toSend, sender);
                } catch (Exception e) {
                    System.out.println("Failed to send channel listing to " + sender.getUsername());
                    e.printStackTrace();
                }
                break;
            case listChannelsResponse:
                System.out.println("Error: server received packet with server-only opcode.");
                break;
            case openConnection:
                //Handled in the preceding function
                try {
                    talk(packet, sender);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case closeConnection:
                channel = sender.getChannel();
                users.remove(sender.getUsername());
                channelMembers.get(channel).remove(sender);
                socketMap.remove(sender);
                serverSocketMap.remove(sender);
                outputStreamMap.remove(sender);
                break;
            case stillHerePing:
                //Good for you.
                if(!packet.getMessage().equalsIgnoreCase("HERE")) {
                    //Send the packet to the user
                    try {
                        //talk(packet, sender);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void talk(MessagePacket packet, User target) throws Exception {
        ServerSocket outputSocket = serverSocketMap.get(target);
        Socket returnSocket = socketMap.get(target);

        try {
            DataOutputStream outToClient = outputStreamMap.get(target);
            outToClient.writeBytes(packet.formatMessage() + '\n');
            outToClient.flush();
        } catch (Exception e) {
            System.out.println("Packet failed to send in talk()");
            e.printStackTrace();
            throw e;
        }
    }
}
