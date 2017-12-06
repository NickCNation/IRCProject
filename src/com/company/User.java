package com.company;

public class User {
    private String username;
    private int port;
    private String channel;
    private long timeoutTime;
    public static long updateTime = 1000000;

    public User() {
        username = new String("");
        port = 6789;
        channel = new String("");
        timeoutTime = System.currentTimeMillis() + updateTime;
    }

    public User(String name, int port, String chan) {
        username = name;
        this.port = port;
        channel = chan;
        System.out.println("User " + username + " was assigned port " + port);
        timeoutTime = System.currentTimeMillis() + updateTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public long getTimeoutTime() {
        return timeoutTime;
    }

    public void updateTimeout() {
        this.timeoutTime = System.currentTimeMillis() + updateTime;
    }
}
