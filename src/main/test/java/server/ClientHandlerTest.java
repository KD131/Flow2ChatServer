package server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerTest
{
    ServerSocket ss;
    Socket clientSocket;
    Socket serverClientSocket;
    DataInputStream clientIn;
    DataOutputStream clientOut;
    DataInputStream serverIn;
    DataOutputStream serverOut;
    ClientHandler user1;
    
    @BeforeEach
    void setUp()
    {
        try
        {
            ss = new ServerSocket(1234);
            InetAddress ip = InetAddress.getByName("localhost");
            clientSocket = new Socket(ip,1234);
            serverClientSocket = ss.accept();
            clientIn = new DataInputStream(clientSocket.getInputStream());
            clientOut = new DataOutputStream(clientSocket.getOutputStream());
            serverIn = new DataInputStream(serverClientSocket.getInputStream());
            serverOut = new DataOutputStream(serverClientSocket.getOutputStream());
            user1 = new ClientHandler(clientSocket,"user1",serverIn,serverOut);
            Server.ar.add(user1);
            Server.acceptedUsers.add("Lars");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    @AfterEach
    void tearDown()
    {
        try
        {
            clientIn.close();
            clientOut.close();
            clientSocket.close();
            serverIn.close();
            serverOut.close();
            serverClientSocket.close();
            ss.close();
            Server.ar.clear();
            Server.acceptedUsers.clear();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    @Test
    void connectSuccess()
    {
        assertFalse(user1.loggedin);
        user1.connect("Lars");
        assertTrue(user1.loggedin);
        assertEquals("Lars",user1.getName());
    }
    
    @Test
    void connectFailure()
    {
        assertFalse(user1.loggedin);
        assertFalse(user1.connect("notRealUser"));
        assertFalse(user1.loggedin);
    }
    
    @Test
    void sendMessage()
    {
        try
        {
            user1.loggedin = true;
            user1.sendMessage("user1","test");
            assertEquals("MESSAGE#user1#test",clientIn.readUTF());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    @Test
    void onlineList()
    {
        try
        {
            user1.loggedin = true;
            user1.onlineList();
            assertEquals("ONLINE#user1",clientIn.readUTF());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
