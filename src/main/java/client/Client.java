package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
Client est la classe lancé pour  la parti client de la communication .
* Il est constitué de deux threads, l'un pour recevoir les messages en continu d'autres client et l'autre pour en envoyer .
* 
*/
public class Client {
    
    private boolean running = true;

    private final Scanner scanner;
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;
    private String pseudo;
    private boolean ErrorPseudo;


    public Client() throws IOException {
        scanner = new Scanner(System.in);
        socket = new Socket("localhost", 6666);
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
        ErrorPseudo=false;
        sendPseudo();

    }
    /**
     * 
     * @throws IOException 
     * 
     * 
     * Envoyer a chaque début de connexion le pseudo doit être unique .La parti serveur 
     * reconnait que cela constitue un message d'envoie de Pseudo grâce au bit envoyé(=1)
     * Dans les faits ( non visible a l'utilisateurs) lorsque celui-ci  rentre un mauvais pseudo ,on lui attribue le pseudo "PseudoError" qui permet de gérer ce cas,jusqu'a ce qu'il rentre un pseudo valide 
     */
    private void sendPseudo() throws IOException{
        ErrorPseudo=false;
        System.out.println("Entrez un pseudo");
        Scanner in = new Scanner(System.in);
       pseudo = in.nextLine();
        output.writeByte(1);
        output.writeUTF(pseudo);
        //output.flush();
 
        
    }
/**
 * envoie d'un message, Exit declence l'arret des communications ( sensible a la case )
 * 
 */
    public void sendMessage()  {
        Thread sendMessage = new Thread(() ->
        {
            while (running) {
                try {   
                    if(ErrorPseudo){
                        sendPseudo();   
                    }else {
                    
                    //System.out.print(pseudo+": blabla ");
                    String msg = scanner.nextLine();
                    
                    if (msg.equals("Exit")) {
                        running = false;
                        scanner.close();

                    }
                    // write on the output stream
                     output.writeByte(2);
                    output.writeUTF(msg);
                }
                }
                catch (IOException e) {
                    e.printStackTrace();
                   // System.out.println("finally closed");
                }
            }
               
        });

        sendMessage.start();
    }

    /**
     * 
     * Thread de reception
     */
    public void readMessage() {
        Thread readMessage = new Thread(() ->
        {
            while (running) {
                try {
                    
                    String msg = input.readUTF();
                       if (msg.equals("le pseudo est déjà pris")||msg.contains("PseudoError")){
                           System.out.println("Le pseudo est déjà pris");
                           ErrorPseudo=true;
                           
                         //sendPseudo();

                     }else{
                            System.out.println(msg); 
                       }
                    if (msg.equals("Exit")) {
                        running = false; 
                        input.close();
                        output.close();
                        socket.close();
                       
                    }
                    
                    
                  
                }
                catch (IOException e) {
                    try {
                        
                        System.out.println("Arret de communication avec le serveur");
                        e.printStackTrace();
                        running = false;
                        input.close();
                        output.close();
                        socket.close();
                    } catch (IOException ex) {
                        
                    }
                }
            }
        });
        
        readMessage.start();
    }

}


