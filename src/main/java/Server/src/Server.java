// Java implementation of Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java

import java.io.*;
import java.util.*;
import java.net.*;

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
            ClientHandler mtch = new ClientHandler(s,"client " + i, dis, dos);
            System.out.println("\"client "+i+"\" has entered the chatroom.");

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
        this.isloggedin=true;
    }

    @Override
    public void run() {

        String received;
        while (true)
        {
            try
            {
                // receive the string
                received = dis.readUTF(); //crashes server if client 0 disconnects
                System.out.println(received);

                if(received.equalsIgnoreCase("logout")){
                    dos.writeUTF("logout");
                    this.isloggedin=false;
                    this.s.close();
                    break;
                }

                // break the string into message and recipient part
                StringTokenizer st = new StringTokenizer(received, "#");
                String recipient = st.nextToken();
                String MsgToSend = st.nextToken();

                // search for the recipient in the connected devices list.
                // ar is the vector storing client of active users
                for (ClientHandler mc : Server.ar)
                {
                    if(recipient.equalsIgnoreCase("all")){
                        mc.dos.writeUTF("From "+this.name+" : "+MsgToSend);
                    }
                    // if the recipient is found, write on its
                    // output stream
                    if (mc.name.equalsIgnoreCase(recipient) && mc.isloggedin==true && !mc.name.equalsIgnoreCase("all"))
                    {
                        mc.dos.writeUTF("From "+this.name+" : "+MsgToSend);
                        break;
                    }
                }
            } catch (IOException e) {

                e.printStackTrace();
            }

        }
        try
        {
            // closing resources
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

