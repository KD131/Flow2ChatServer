package server;
// Java implementation of Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java

/**
 * HOLD C
 * Kasper, Nicklas, Kris
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

// Server class
public class Server
{
    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();
    static List<String> acceptedUsers = new ArrayList<>();

    // counter for clients
    static int i = 0;
    
    public static void main(String[] args) throws IOException
    {
        // hard-coded list of users
        acceptedUsers.add("Hacker-man");
        acceptedUsers.add("Kasper");
        acceptedUsers.add("Nicklas");
        acceptedUsers.add("Kris");
        acceptedUsers.add("Test-user");
        acceptedUsers.add("Lars");
        acceptedUsers.add("Hans");
        acceptedUsers.add("Peter");

        boolean running = true;
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1234);
        
        Socket s;
        // running infinite loop for getting
        // client request
        while (running)
        {
            
            System.out.println("Waiting for client...");
            // Accept the incoming request
            s = ss.accept();
            
            System.out.println("New client request received : " + s);
            
            // obtain input and output streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            
            System.out.println("Creating a new handler for this client...");
            
            // Create a new handler object for handling this request.
            ClientHandler mtch = new ClientHandler(s, "unnamed" + i, dis, dos);
            
            // Create a new Thread with this object.
            Thread t = new Thread(mtch);
            
            System.out.println("Adding this client to active client list");
            
            // add this client to active clients list
            
            ar.add(mtch);
            
            // start the thread.
            t.start();
            
            // increment i for new client.
            // i is used for naming only, and can be replaced
            // by any naming scheme
            i++;
            
        }
        ss.close();
    }
}
