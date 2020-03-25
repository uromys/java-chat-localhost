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
import java.net.Socket;
import java.util.Scanner;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


final class ClientHandler implements Runnable {
    private final Server ownerServer;

    private final DataInputStream input;
    private final DataOutputStream output;
    private final Socket socket;

    private  String name;
    private boolean isLoggedIn = true;

    ClientHandler( Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.ownerServer = server;

        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }


    @Override
    public void run() {
        try {
            String receivedMsg;
            
            while (true) {
                byte messageType = input.readByte();
                receivedMsg = input.readUTF();
                
                 
                 //pour savoir le type de message que l'on viens de recevoir
               if(messageType==1){
                    
                    //System.out.println("ça a marche "+receivedMsg);
                    
                   
                     for (ClientHandler user : ownerServer.getUserContainer()) {
                         if(user.name==receivedMsg){// le pseudo existe déjà 
                              user.output.writeUTF("  le pseudo "+receivedMsg+ "est déjà pris ");
                             break;
                         }
                         setName(receivedMsg);
        }
                    
                   
                    System.out.println("Nouveau client : "+ this.toString());
                    continue;
                    
                }
                
                
                
                System.out.println(receivedMsg);
                
                SendMessage(receivedMsg);   
                if (receivedMsg.equals("Exit")) {
                    output.writeUTF("Exit"); //server tells client that it has closed connection for him
                    ownerServer.getUserContainer().forEach(user-> {
                        try {
                            user.output.writeUTF(name + " ...is quitting chat");
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    this.closeConnection();
                    ownerServer.disconnectUser(this);
                    break;
                }
                else if (receivedMsg.equals("list users")) {
                    String connectedUsers = ownerServer.getConnectedUsers();
                    output.writeUTF(connectedUsers);
                    continue;
                }
                else if (receivedMsg.equals("my name")) {
                    output.writeUTF("your name is: " + name);
                    continue;
                }
                /*
                else if (receivedMsg.equals(""))

                //divide receivedMsg into msg to send and recipient to sent to
                if (!receivedMsg.contains("-> ")) {
                    output.writeUTF("wrong format of the msg");
                    continue;
                }
                String[] result = receivedMsg.split("-> ");
                String msgToSend = result[0];
                String recipient = result[1];
                
                if (recipient.equals("all")) {
                    this.sendToAll(msgToSend);
                }
                else {
                    sendToOne(msgToSend, recipient);
                }*/
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() throws IOException {
        System.out.println("Client " + this.socket + " sends exit...");
        System.out.println("Closing this connection.");

        this.isLoggedIn = false;
        input.close();
        output.close();
        this.socket.close();

        System.out.println("Connection closed for:" + name);
    }

    private void sendToAll(String msgToSend) throws IOException {
        for (ClientHandler user : ownerServer.getUserContainer()) {
            user.output.writeUTF(this.name + "( sent to all)" + " : " + msgToSend);
        }

    }
    
    private void SendMessage (String msgToSend )throws IOException {
         for (ClientHandler user : ownerServer.getUserContainer()) {
            user.output.writeUTF(this.name  + " : " + msgToSend);
        } 
        
        
    }
    

    private void sendToOne(String msgToSend, String recipient) throws IOException {
        // search for the recipient in the connected devices list.
        // ar is the vector storing client of active users

        for (ClientHandler user : ownerServer.getUserContainer()) {
            // if the recipient is found, write on its
            // output stream
            if (user.name.equals(recipient)) {
                user.output.writeUTF(this.name + " : " + msgToSend);
                return;
            }
        }
        output.writeUTF(recipient +": doesnt exist !");

    }
    public void setName(String Pseudo)  {
        
        this.name=Pseudo;
        
    }
    
    public boolean chainControl (String toTest )  {
        return  !("Exit".equals(toTest)||"list users".equals(toTest)||"my name".equals(toTest) );
           
        
    }
    


    @Override
    public String toString() {
        return name+" :"+ socket.getInetAddress();
    }
}
