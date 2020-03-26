package server;


import java.net.ServerSocket;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author anael
 */
public final class Server {

    private static final int SERVER_PORT = 6666;
    
    private final ServerSocket socketManager;
   
    //private final Set<ClientHandler> userContainer = new Set<>();
    Set<ClientHandler> userContainer = new HashSet<>();

    public Server() throws IOException {
        socketManager = new ServerSocket(SERVER_PORT);
    }

    public String getConnectedUsers() {
        System.out.println("Connected users: ");
        String result = "";

        for (ClientHandler client : userContainer) {
            result = result + " " + client.toString() + " ";
            System.out.println(result);
        }
        return result;
    }

    public void start() {
        while (true) {
            try {
                
                //accept() blocks program when waiting for connection
                if (userContainer.size() == 0) {
                    System.out.println("En attente de connexion d'un premier client");
                }
                //String pseudo =(string)
                ClientHandler user = new ClientHandler(socketManager.accept(), this);
                userContainer.add(user);

                Thread t = new Thread(user);
                t.start();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnectUser(ClientHandler user) {
        userContainer.remove(user);
        //ID_CREATOR--;
    }

    public Set<ClientHandler> getUserContainer() {
        return userContainer;
    }
}
