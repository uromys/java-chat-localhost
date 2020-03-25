


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;



public class Client {
    //flag to stop thread. Reminder thread stops when run() method from thread returned
    private volatile boolean running = true;

    private final Scanner scanner;
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;


    private Client() throws IOException {
        scanner = new Scanner(System.in);

        

        // establish the connection
        socket = new Socket("localhost", 6666);

        // obtaining input and out streams
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
        sendPseudo();
    }
    
    private void sendPseudo() throws IOException{
        
        System.out.println("Entrez un pseudo");
        Scanner in = new Scanner(System.in);
        String pseudo = in.nextLine();
        output.writeByte(1);
        output.writeUTF(pseudo);
 
    }
    

    private void sendMessage() {
        Thread sendMessage = new Thread(() ->
        {
            while (running) {
                try {
                    String msg = scanner.nextLine();
                    if (msg.equals("Exit")) {
                        running = false;
                        scanner.close();
                        //you exit the run() you kill thread, return above does it
                    }
                    // write on the output stream
                     output.writeByte(2);
                    output.writeUTF(msg);
                }
                catch (IOException e) {
                    e.printStackTrace();
                   // System.out.println("finally closed");
                }
            }
        });

        sendMessage.start();
    }

    private void readMessage() {
        Thread readMessage = new Thread(() ->
        {
            while (running) {
                try {
                    // read the message sent to this client
                    String msg = input.readUTF();
                    if (msg.equals("Exit")) {
                        running = false; // it is not necessary  since send msg thread set this flag on false;
                        input.close();
                        output.close();
                        socket.close();
                        //you exit the run() you kill thread, return above does it
                    }
                    System.out.println(msg);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        readMessage.start();
    }

    public static void main(String args[]) throws IOException, java.io.IOException {
        Client client = new Client();
        client.sendMessage();
        client.readMessage();
    }
}