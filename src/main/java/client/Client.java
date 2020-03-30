package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
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
     * 
     * @param Pseudo
     * @throws IOException 
     * Ce constructeur prend simplement le pseudo en paramètres en cas d'interruption pour la recreation d'un socket 
     */
    
    public Client(String Pseudo) throws IOException{
            pseudo=Pseudo;
            scanner = new Scanner(System.in);
        socket = new Socket("localhost", 6666);
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
        ErrorPseudo=false;
        //sendPseudo();
        
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
        output.flush();
 
        
    }
    
    private void sendPseudoAfterServerDc() throws IOException{
        ErrorPseudo=false;
        output.writeByte(1);
        output.writeUTF(pseudo);
        output.flush();
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
                        try {
                            TimeUnit.SECONDS.sleep(1);//le temps que le serveur renvoie la réponse, sinon le thread retourne dans sendPseudo meme si le pseudo est bon
                        } catch (InterruptedException ex) {
                           
                        }
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
                    output.flush();
                }
                }
                catch (IOException e) {
                    //e.printStackTrace();
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
        Thread readMessage;
        readMessage = new Thread(() ->
        {
            while (running) {
                try {
                    
                    String msg = input.readUTF();
                    
                    
                    boolean Fautpasprint=false;
                    if(msg.contains("PseudoError")){
                        Fautpasprint=true;
                    }
                    //si on ne fait pas ça, l'utilisateurs et tout les utilisateurs recoivent  un message de PseudoError 
                    // avec son futur pseudo ( qui marche )
                    
                    if (msg.equals("le pseudo est déjà pris")){
                        System.out.println("Le pseudo est déjà pris");
                        ErrorPseudo=true;
                  
                    }else if (Fautpasprint==false){
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
                        
                        while(true){
                            try{
                                Client NewClient=new Client(this.pseudo);
                                
                                this.finalize();
                                //
                                System.out.println("reprise connexion avec le serveur");
                                NewClient.sendPseudoAfterServerDc();
                                NewClient.readMessage();
                                NewClient.sendMessage();
                                System.out.println("Appuyer sur entrée  pour reprendre la conversation");
                                break;
                            }catch (IOException exed){
                                //reconnecte failed
                                try{
                                    TimeUnit.SECONDS.sleep(7);// on retente dans 7 secondes 
                                }catch(InterruptedException ie){
                                    //interrupted
                                }
                                    
                            } catch (Throwable ex) {
                                
                            }
                        }
                        
                    } catch (IOException ex) {
                        
                    }
                }
            }
        });
        
        readMessage.start();
    }

}


