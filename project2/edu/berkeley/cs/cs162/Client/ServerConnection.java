package edu.berkeley.cs.cs162.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import edu.berkeley.cs.cs162.Writable.Message;
import edu.berkeley.cs.cs162.Writable.MessageFactory;

public class ServerConnection {
    private Socket S2C;
    private Socket C2S;

    DataInputStream iS2C;
    DataInputStream iC2S;
    DataOutputStream oS2C;
    DataOutputStream oC2S;

    private boolean valid;

    public ServerConnection(Socket connection1, Socket connection2) {
        S2C = connection1;
        C2S = connection2;
        valid = false;
    }

    public Socket getS2C() throws RuntimeException {
        if (valid)
            return S2C;
        else
            throw new RuntimeException("Uninitialized Server Connection");
    }

    public Socket getC2S() {
        if (valid)
            return C2S;
        else
            throw new RuntimeException("Uninitialized Server Connection");
    }
    
    /**
     * Tries to receive a 3-way handshake.
     *
     * @param rng The random number generator that will be used.
     * @throws IOException
     */
    public boolean initiate3WayHandshake(Random rng) throws IOException {
        int SYN_ID = rng.nextInt();
        iC2S = new DataInputStream(C2S.getInputStream());
        iS2C = new DataInputStream(S2C.getInputStream());
        oC2S = new DataOutputStream(C2S.getOutputStream());
        oS2C = new DataOutputStream(S2C.getOutputStream());

        oC2S.writeInt(SYN_ID);
        //System.out.println(count1.addAndGet(1) + " syn1 have been sent");
        oS2C.writeInt(SYN_ID);
        //System.out.println(count2.addAndGet(1) + " syn2 have been sent");
        

        int SYN1 = iC2S.readInt();
        int ACK1 = iC2S.readInt();
        int SYN2 = iS2C.readInt();
        int ACK2 = iS2C.readInt();

        if (!(ACK1 == ACK2 && ACK1 == (SYN_ID + 1))) {
            System.out.printf("ACKs do not match! Received %d and %d when expecting %d\n", ACK1, ACK2, SYN_ID + 1);
            return false;
        }
        if (SYN1 > SYN2) {
            //switch the sockets if we have them wrong.
            Socket tempSoc = C2S;
            C2S = S2C;
            S2C = tempSoc;

            iC2S = new DataInputStream(C2S.getInputStream());
            iS2C = new DataInputStream(S2C.getInputStream());
            oC2S = new DataOutputStream(C2S.getOutputStream());
            oS2C = new DataOutputStream(S2C.getOutputStream());
            oC2S.writeInt(SYN2 + 1);
            oS2C.writeInt(SYN1 + 1);
        } else {
            oC2S.writeInt(SYN1 + 1);
            oS2C.writeInt(SYN2 + 1);
        }
        valid = true;
        return true;
    }

    public void close(){
    	valid = false;
    	try {
			S2C.close();
		} catch (IOException e1) {
			// connection already closed. let it go.
		}
        try {
			C2S.close();
		} catch (IOException e) {
			// connection already closed. let it go.
		}
    }
    
    public void sendReplyToServer(Message message) throws IOException {
        message.writeTo(oS2C);
        oS2C.flush();
    }
    
    // TODO Add specialized logic for STATUS_OK replies that need to have extra args, probably through a special ResponseMessages method.
    public Message sendSyncToServer(Message message) throws IOException {
        message.writeTo(oC2S);
        oC2S.flush();
        return MessageFactory.readResponseMessage(iC2S, message);
    }

    public void sendDisconnectToServer() throws IOException {
        MessageFactory.createDisconnectMessage().writeTo(oC2S);
        oC2S.flush();
    }

    public void readFromServer(Message messageContainer) throws IOException {
        messageContainer.readFrom(iS2C);
    }

    public Message readFromServer() throws IOException {
        return MessageFactory.readServerMessage(iS2C);
    }
}
