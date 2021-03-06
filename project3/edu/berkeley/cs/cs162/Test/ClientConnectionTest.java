package edu.berkeley.cs.cs162.Test;

import edu.berkeley.cs.cs162.Server.ClientConnection;
import edu.berkeley.cs.cs162.Writable.Message;
import edu.berkeley.cs.cs162.Writable.MessageFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static org.junit.Assert.*;

public class ClientConnectionTest {

    @Test
    public void testPlayerTimeout() throws IOException, InterruptedException {

        String address = "localhost";
        final int port = 12345;

        final Socket container[] = new Socket[1];
        Thread serverThread = new Thread()  {
            public void run(){
                try {
                ServerSocket server = new ServerSocket(port);
                container[0] = server.accept();
                } catch (IOException e)
                {

                }
            }
        };
        serverThread.start();
        Socket sock1 = new Socket(address, port);
        serverThread.join();
        Socket sock2 = container[0];

        ClientConnection connection = new ClientConnection(sock1, sock2, 0, new PrintStream(new NullOutputStream()));
        connection.setValid();

        Message message = MessageFactory.createGenericOpCodeOnlyMessage();

        try {connection.readReplyFromClient(message, 5000); }
        catch (IOException e) { assertTrue(e instanceof SocketTimeoutException); }

        sock1.close();
        sock2.close();
    }
}
