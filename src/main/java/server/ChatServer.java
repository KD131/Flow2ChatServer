package server;

import java.net.UnknownHostException;

public class ChatServer {


    //Call server with arguments like this: 0.0.0.0 8088 logfile.log
    public static void main(String[] args) throws UnknownHostException {
        String ip ="localhost";
        int port = 1234;
        String logFile = "log.txt";

        try {
            if (args.length == 3) {
                ip = args[0];
                port = Integer.parseInt(args[1]);
                logFile = args[2];
            }
            else {
                throw new IllegalArgumentException("Server not provided with the right arguments");
            }
        } catch (NumberFormatException ne) {
            System.out.println("Illegal inputs provided when starting the server!");
            return;
        }
    }
}
