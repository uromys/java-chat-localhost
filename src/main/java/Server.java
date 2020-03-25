/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author anael
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;


final class Server {
    private static final int SERVER_PORT = 6666;
   

    private static int idCreator = 0;
  

    private final ServerSocket socketManager = new ServerSocket(SERVER_PORT);
   
    //private final Set<ClientHandler> userContainer = new Set<>();
    Set<ClientHandler> userContainer = new HashSet<>();

    private Server() throws IOException {

    }

    String getConnectedUsers() {
        System.out.println("Connected users: ");

        String result = "";
        for (ClientHandler client : userContainer) {
            result = result + " " + client.toString() + " ";
            System.out.println(result);
        }
        return result;
    }

    private void start() {
        while (true) {
            try {
                //new  socket on server to handle new client's connection
                //accept() blocks program when waiting for connection
                if(userContainer.size()==0)
                System.out.println("En attente de connexion d'un premier client");
                
                //String pseudo =(string)
                ClientHandler user = new ClientHandler( socketManager.accept(), this);
                userContainer.add(user);

                Thread t = new Thread(user);
                t.start();

                
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void disconnectUser(ClientHandler user) {
        userContainer.remove(user);
        idCreator--;
    }

    private String createNewUsername() {
        return "user" + idCreator++;
    }
    


    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }

    public Set<ClientHandler> getUserContainer() {
        return userContainer;
    }
}

