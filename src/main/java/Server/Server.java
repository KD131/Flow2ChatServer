package Server;
// Java implementation of Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

// Server class
public class Server
{
    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();
    
    
    // counter for clients
    static int i = 0;
    
    public static void main(String[] args) throws IOException
    {
        boolean running = true;
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1234);
        
        Socket s = null;
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

// ClientHandler class
class ClientHandler implements Runnable
{
    private String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean loggedin;
    
    // constructor
    public ClientHandler(Socket s, String name, DataInputStream dis, DataOutputStream dos)
    {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.loggedin = false;
    }
    
    @Override
    public void run()
    {
        String received;
        while (true)
        {
            try
            {
                // receive the string
                received = dis.readUTF(); //crashes server if client 0 disconnects
                System.out.println(received);
                
                // breaks string into cmd and parameters
                StringTokenizer st = new StringTokenizer(received, "#");
                String cmd = (st.hasMoreTokens()) ? st.nextToken() : "";
                String param1 = (st.hasMoreTokens()) ? st.nextToken() : "";
                String param2 = (st.hasMoreTokens()) ? st.nextToken() : "";
                
                // has to attempt login as first command
                if (cmd.equals("CONNECT") && !loggedin)
                {
                    if (connect(param1))    // successfully logs in
                    {
                        onlineList();
                        System.out.println(this.name + " has joined the server!");
                    }
                    else
                    {
                        System.out.println("User not found!");
                        dos.writeUTF("CLOSE#2"); //user not found
                        close();
                        break;
                    }
                }
                
                else if (cmd.equals("CLOSE") && loggedin)
                {
                    dos.writeUTF("CLOSE#0"); //normal close
                    System.out.println(this.name + " logged out.");
                    onlineList();
                    close();
                    break;
                }
                
                else if (cmd.equals("ONLINE") && loggedin)
                {
                   onlineList();
                }
                
                else if (cmd.equals("SEND") && loggedin)
                {
                    sendMessage(param1, param2);
                }
                
                // no valid command
                else
                {
                    System.out.println("Illegal input!");
                    dos.writeUTF("CLOSE#1"); //illegal input
                    close();
                    break;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    // returns true if successfully logged in.
    // returns false if no name given.
    public boolean connect(String name)
    {
        if (name.equals(""))
        {
            return false;
        }
        this.name = name;
        this.loggedin = true;
        return true;
    }
    
    public void sendMessage(String recipient, String msg) throws IOException
    {
        // search for the recipient in the connected devices list.
        // ar is the vector storing client of active users
        for (ClientHandler mc : Server.ar)
        {
            if (recipient.equalsIgnoreCase("*") && mc != this)      // doesn't send to self
            {
                mc.dos.writeUTF("MESSAGE#" + this.name + "#" + msg);
            }
            // if the recipient is found, write on its
            // output stream
            if (recipient.contains(mc.name) && mc.loggedin && !mc.name.equalsIgnoreCase("*"))
            {
                mc.dos.writeUTF("MESSAGE#" + this.name + "#" + msg);
            }
        }
    }
    
    public void close() throws IOException
    {
        this.dis.close();
        this.dos.close();
        this.s.close();
        this.loggedin = false;
        Server.ar.remove(this);
    }
    
    //ONLINE user list
    public void onlineList() throws IOException
    { //sends a message to all logged-in users about who is online
        StringBuilder sb = new StringBuilder();
        sb.append("ONLINE#");
        for (ClientHandler mc : Server.ar)
        {
            if (mc.loggedin) //checks if a user is logged in
            {
                sb.append(mc.name).append(",");     // constructs message
            }
        }
        sb.deleteCharAt(sb.toString().length() - 1);  //deletes last comma
        for (ClientHandler mc : Server.ar)      // sends message to all
        {
            mc.dos.writeUTF(sb.toString());
        }
    }
}
