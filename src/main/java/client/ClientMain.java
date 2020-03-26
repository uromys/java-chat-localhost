package client;

import java.io.IOException;

public class ClientMain {

    public static void main(String args[]) throws IOException, java.io.IOException {
        Client client = new Client();
        client.readMessage();
        client.sendMessage();
    }
}
