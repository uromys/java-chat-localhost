package server;

import utils.Utils;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author anael
 */
public final class ClientHandler implements Runnable {

    private final Server ownerServer;

    private final DataInputStream input;
    private final DataOutputStream output;
    private final Socket socket;
    private String pseudo="";
    private boolean isLoggedIn = true;

    public ClientHandler(Socket socket, Server server) throws IOException {
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
                if(messageType == 1){
                    System.out.println("ntm+ " + receivedMsg);
                    boolean existe=false;
                     for (ClientHandler user : ownerServer.getUserContainer()) {
                         System.out.println("test "+user.pseudo);
                         if(user.pseudo.equals(receivedMsg) ) {// le pseudo existe déjà
                              existe=true;
                         }
                     }
                         if (existe==false){
                         this.pseudo = receivedMsg;
                         }
                         else if ( existe==true || this.pseudo=="PseudoError" ) {
                             System.out.println("normalement il a envoyé l'erreur");
                             this.pseudo="PseudoError";
                             this.output.writeUTF("le pseudo est déjà pris");
                             //this.output.flush();
                         }
                    System.out.println("Nouveau client : "+ this.toString());
                    continue;
                }
                
                
                if(messageType==2){
                System.out.println(receivedMsg);
                
                if(Utils.fonctionUtile.chainControl(receivedMsg)){ //si le message reçu a un comportement specifique 
                sendMessage(receivedMsg);
                }
                //TODO  LES STRING "CONSTANT" fait une class de constant static
                if (receivedMsg.equals(Utils.EXIT)) {
                    output.writeUTF("Exit"); 
                    
                    
                    ownerServer.getUserContainer().forEach(user-> {
                        try {
                            user.output.writeUTF(pseudo + " ...is quitting chat");
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    this.closeConnection();
                    ownerServer.disconnectUser(this);
                    //ownerServer.getUserContainer().remove(this);
                    break;
                } else if (receivedMsg.equals("list users")) {
                    String connectedUsers = ownerServer.getConnectedUsers();
                    output.writeUTF(connectedUsers);
                    continue;
                } else if (receivedMsg.equals("my name")) {
                    output.writeUTF("your name is: " + pseudo);
                    continue;
                }
            }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() throws IOException {
        System.out.println("client.Client " + this.socket + " sends exit...");
        System.out.println("Closing this connection.");

        this.isLoggedIn = false;
        input.close();
        output.close();
        this.socket.close();

        System.out.println("Connection closed for:" + pseudo);
    }
    
    private void sendMessage(String msgToSend) throws IOException {
         for (ClientHandler user : ownerServer.getUserContainer()) {
             //if(this.pseudo!=user.pseudo)
            user.output.writeUTF(this.pseudo + " : " + msgToSend);
        }
    }
    

    private void sendToOne(String msgToSend, String recipient) throws IOException {
        
        for (ClientHandler user : ownerServer.getUserContainer()) {
            if (user.pseudo.equals(recipient)) {
                user.output.writeUTF(this.pseudo + " : " + msgToSend);
                return;
            }
        }
        output.writeUTF(recipient +": doesnt exist !");

    }

  
    


    @Override
    public String toString() {
        return pseudo +" :"+ socket.getInetAddress();
    }
}
