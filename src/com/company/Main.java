package com.company;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Thread listener;
        Thread talker;
        Client client;
        Server server;
        String input;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Are you a (s)erver or a (c)lient?");

            input = reader.readLine();
            input = input.toLowerCase();

            if('s' == input.toLowerCase().charAt(0)) {
                server = new Server();
                listener = new Thread(server);
                talker = new Thread(server);
                listener.setName("listener");
                talker.setName("talker");
                listener.start();
                talker.start();
            } else {
                System.out.println("What's your nick name?");
                input = reader.readLine();

                client = new Client(input);
                if(!client.initializeConnection()) {
                    System.out.println("Failed to initialize Server connection.");
                } else {
                    System.out.println("Connected to server\nJoined channel\"default\"\nType \\help for available commands.");
                    listener = new Thread(client);
                    talker = new Thread(client);
                    listener.setName("listener");
                    talker.setName("talker");
                    listener.start();
                    talker.start();
                }
            }

            while(true);

        } catch (RuntimeException thisMeansWeQuitVoluntarily) {
            System.out.println("Have a nice day.");
            return;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }
    }
}
