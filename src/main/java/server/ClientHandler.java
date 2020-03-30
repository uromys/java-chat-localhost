package server;

import utils.Utils;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *Classe créé a chaque fois qu'un client se connecte par la classe Server, est stocké dans un set 
 * 
 * 
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

    
    /**
     * Run  a deux comportements : lorsque le bit est a 1 , le message reçu correspond a l'envoie d'un pseudo
     * 
     * 2  le message contenu est soit un message a envoyé à un destinataire ( ou a tout les destinataires ) ou 
     * le serveur doit renvoyer des informations( liste des utilisateurs actuellement connecté  par exemple ) 
     */
    @Override
    public void run() {
        try {
            String receivedMsg;
            while (true) {
                
                byte messageType = input.readByte();
                receivedMsg = input.readUTF();
                
                //pour savoir le type de message que l'on viens de recevoir
                if(messageType == 1){
                    boolean existe=false;
                     for (ClientHandler user : ownerServer.getUserContainer()) {
                         //System.out.println("test "+user.pseudo);
                         if(user.pseudo.equals(receivedMsg) ) {// le pseudo existe déjà
                              existe=true;
                         }
                     }
                         if (existe==false){
                         this.pseudo = receivedMsg;
                         this.output.writeUTF("Bienvenu "+this.pseudo);
                         }
                         //|| this.pseudo=="PseudoError"
                         else if ( existe==true  ) {
                             //System.out.println("normalement il a envoyé l'erreur");
                             this.pseudo="PseudoError";
                             this.output.writeUTF("le pseudo est déjà pris");
                             //this.output.flush();
                         }
                    System.out.println("Nouveau client : "+ this.toString());
                    continue;
                }
                
                
                if(messageType==2){
                System.out.println(receivedMsg);
                
                
                
                
                
                
                if(Utils.fonctionUtile.chainControl(receivedMsg)){ //si le message reçu n'a  pas un comportement specifique 
                sendMessage(receivedMsg);
                }
                
                if(receivedMsg.contains("@")){
                    String[] arrayOfMsg=receivedMsg.split("@",2); // on split que deux fois 
                    sendToOne(arrayOfMsg[1],arrayOfMsg[0]);
                    
                }
                
                //TODO  LES STRING "CONSTANT" fait une class de constant static
                if (receivedMsg.equals(Utils.EXIT)) {
                    output.writeUTF("Exit"); 
                    
                    
                    ownerServer.getUserContainer().forEach(user-> {
                        try {
                            user.output.writeUTF(pseudo + " ...a quitté la conversation");
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    this.closeConnection();
                    ownerServer.disconnectUser(this);
                    //ownerServer.getUserContainer().remove(this);
                    break;
                } else if (receivedMsg.equals(Utils.CONNECTE)) {
                    String connectedUsers = ownerServer.getConnectedUsers();
                    output.writeUTF(connectedUsers);
                    continue;
                } else if (receivedMsg.equals(Utils.QUISUISJE)) {
                    output.writeUTF("ton nom est : " + pseudo);
                    continue;
                }
            }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
                    ownerServer.getUserContainer().forEach(user-> {
                        try {
                            user.output.writeUTF(pseudo + "a quitté la conversation");
                        }
                        catch (IOException a) {
                            a.printStackTrace();
                        }
                    });
            try {
                this.closeConnection();
            } catch (IOException ex) {
                
            }
                    ownerServer.disconnectUser(this);
                    //ownerServer.getUserContainer().remove(this);
                    
            
        }
    }

    private void closeConnection() throws IOException {
        System.out.println(this.socket + " sends exit...");
        System.out.println("Fermeture de la connexion");

        this.isLoggedIn = false;
        input.close();
        output.close();
        this.socket.close();

        System.out.println("Connexion fermé:" + pseudo);
    }
    
    private void sendMessage(String msgToSend) throws IOException {
         for (ClientHandler user : ownerServer.getUserContainer()) {
             //if(this.pseudo!=user.pseudo)
            user.output.writeUTF(this.pseudo + " : " + msgToSend);
        }
    }
    
//a implementer
    private boolean sendToOne(String msgToSend, String recipient) throws IOException {
        
        for (ClientHandler user : ownerServer.getUserContainer()) {
            if (user.pseudo.equals(recipient)) {
                user.output.writeUTF(this.pseudo + " (private ): " + msgToSend);
                return true;
            }
        }
        output.writeUTF(recipient +": doesnt exist !");
        return false;
    }
    
   

  
    


    @Override
    public String toString() {
        return pseudo +" :"+ socket.getInetAddress();
    }
}
