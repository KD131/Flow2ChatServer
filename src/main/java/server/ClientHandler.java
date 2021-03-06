package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.StringTokenizer;

class ClientHandler implements Runnable
{
    private String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean loggedin;
    FileWriter logger;
    
    public ClientHandler(Socket s, String name, DataInputStream dis, DataOutputStream dos)
    {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.loggedin = false;
        
        try
        {
            this.logger = new FileWriter("log/logfile.log",true);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
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
                        String text = this.name + " has joined the server!";
                        System.out.println(text);
                        logEntry(text);
                    }
                    else
                    {
                        String text = this.name + " | User not found!";
                        System.out.println(text);
                        logEntry(text);
                        dos.writeUTF("CLOSE#2"); //user not found
                        close();
                        break;
                    }
                }
                
                else if (cmd.equals("CLOSE") && loggedin)
                {
                    dos.writeUTF("CLOSE#0"); //normal close
                    String text = this.name + " logged out.";
                    System.out.println(text);
                    logEntry(text);
                    this.loggedin = false;
                    onlineList();
                    close();
                    break;
                }
                /*
                //we were told this was feature-creeping, so we've left it commented out
                else if (cmd.equals("ONLINE") && loggedin)
                {
                    onlineList();
                }
                */
                else if (cmd.equals("SEND") && loggedin)
                {
                    sendMessage(param1, param2);
                }
                
                // no valid command
                else
                {
                    String text = this.name + " | Illegal input!";
                    System.out.println(text);
                    logEntry(text);
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
    
    public String getName()
    {
        return name;
    }
    
    // returns true if successfully logged in.
    // returns false if no name given.
    public boolean connect(String name)
    {
        for (String user : Server.acceptedUsers)
        {
            if (name.equals(user))
            {
                this.name = name;
                this.loggedin = true;
                return true;
            }
        }
        return false;
    }
    
    public void logEntry(String entry) throws IOException
    {
        logger.write(LocalDateTime.now()+" | "+entry+"\n");
        logger.flush();
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
        String text = "MESSAGE#" + this.name + "#" + msg;
        logEntry(text);
    }
    
    public void close() throws IOException
    {
        this.dis.close();
        this.dos.close();
        this.s.close();
        this.logger.close();
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
        sb.deleteCharAt(sb.toString().length() - 1);  //deletes last comma ... it is noget g??gl?
        for (ClientHandler mc : Server.ar)      // sends message to all users logged in
        {
            if (mc.loggedin)
            {
                mc.dos.writeUTF(sb.toString());
            }
        }
        if(sb.toString().equals("ONLINE")){ //it works ??\_(???)_/??
            sb.append("#*eerie silence*");
        }
        String text = sb.toString();
        logEntry(text);
    }
}
