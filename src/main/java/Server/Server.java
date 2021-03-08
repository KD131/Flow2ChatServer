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
        boolean run = true;
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1234);

        Socket s = null;
        // running infinite loop for getting
        // client request
        while (run)
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
            ClientHandler mtch = new ClientHandler(s,"unnamed"+i, dis, dos);

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
        s.close();
    }
}

// ClientHandler class
class ClientHandler implements Runnable
{
    private String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean isloggedin;

    // constructor
    public ClientHandler(Socket s, String name, DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
    }

    @Override
    public void run() {
        boolean doOnce = true;
        String received;
        while (true)
        {
            try
            {
                // receive the string
                received = dis.readUTF(); //crashes server if client 0 disconnects
                System.out.println(received);
                
                if(received.startsWith("CONNECT#") && doOnce){
                    doOnce = false;
                    try
                    {
                        this.name = received.split("#")[1];
                        this.isloggedin=true;
                        onlineList();
                    }
                    catch (ArrayIndexOutOfBoundsException ex){ //if name is entered blank
                        System.out.println("User not found!");
                        dos.writeUTF("CLOSE#2"); //user not found
                        close();
                        break;
                    }
                    System.out.println(this.name+" has joined the server!");
                }
                
                else if(received.equalsIgnoreCase("CLOSE#") && !doOnce){
                    dos.writeUTF("CLOSE#0"); //normal close
                    System.out.println(this.name+" logged out.");
                    onlineList();
                    close();
                    break;
                }
                
                else if(received.startsWith("ONLINE#") && !doOnce){
                    StringBuilder sb = new StringBuilder();
                    sb.append("ONLINE#");
                    for (ClientHandler mc : Server.ar){
                        if(mc.isloggedin) //checks if a user is logged in
                        {
                            sb.append(mc.name).append(",");
                        }
                    }
                    sb.deleteCharAt(sb.toString().length()-1);  //deletes last comma
                    dos.writeUTF(sb.toString());
                }
                
                else if(received.startsWith("SEND#") && !doOnce){
                    // break the string into message and recipient part
                    StringTokenizer st = new StringTokenizer(received, "#");
                    st.nextToken(); //skips command
                    String recipient = (st.hasMoreTokens()) ? st.nextToken() : "none";
                    String MsgToSend = (st.hasMoreTokens()) ? st.nextToken() : "";
    
                    // search for the recipient in the connected devices list.
                    // ar is the vector storing client of active users
                    for (ClientHandler mc : Server.ar)
                    {
                        if(recipient.equalsIgnoreCase("*") && mc != this){
                            mc.dos.writeUTF("MESSAGE#" + this.name + "#" + MsgToSend);
                        }
                        // if the recipient is found, write on its
                        // output stream
                        if (recipient.contains(mc.name) && mc.isloggedin && !mc.name.equalsIgnoreCase("*"))
                        {
                            mc.dos.writeUTF("MESSAGE#" + this.name + "#" + MsgToSend);
                        }
                    }
                }
                else {
                    System.out.println("Illegal input!");
                    dos.writeUTF("CLOSE#1"); //illegal input
                    close();
                    break;
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void close()
    {
        try
        {
            this.dis.close();
            this.dos.close();
            this.s.close();
            this.isloggedin = false;
            Server.ar.remove(this);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    
    }
    
    //ONLINE user list
    public void onlineList(){ //sends a message to all logged-in users about who is online
        StringBuilder sb = new StringBuilder();
        sb.append("ONLINE#");
        for (ClientHandler mc : Server.ar)
        {
            if(mc.isloggedin) //checks if a user is logged in
            {
                sb.append(mc.name).append(",");
            }
        }
        sb.deleteCharAt(sb.toString().length()-1);  //deletes last comma
        for (ClientHandler mc : Server.ar)
        {
            try
            {
                mc.dos.writeUTF(sb.toString());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
