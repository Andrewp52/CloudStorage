package com.pae.cloudstorage.client.network;

import com.pae.cloudstorage.common.CallBack;
import com.pae.cloudstorage.common.Command;
import com.pae.cloudstorage.common.FSObject;

import java.io.*;
import java.net.Socket;

import static com.pae.cloudstorage.common.Command.*;

/**
 * Class provides network connection and basic methods
 * to interact with remote host
 */
public class Connector {
    //TODO:Move it to config
    private static final String COMMDELIM = "%";        // Command args delimiter
    private static final String FRMDELIM = "$_";        // Frame delimiter (for netty server)
    private final String host = "localhost";
    private final int port = 9999;
    private final CallBack callBack;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public Connector(CallBack callBack) {
        this.callBack = callBack;
    }

    public static String getDelimiter() {
        return COMMDELIM;
    }

    // Opens connection and initializes IO streams.
    public void start() throws IOException {
            socket = new Socket(host, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
    }

    // Sends request to remote host and returns received object.
    public Object requestObjectDirect(Command cmd, String arg){
        if(!isConnectionAlive()){
            callBack.call("ERROR", "Connection is not established");
            return null;
        }
        String command = arg == null || arg.isBlank()? cmd.name() + FRMDELIM : cmd.name() + COMMDELIM + arg + FRMDELIM;
        try {
            out.write((command).getBytes());
            return readObject();
        } catch (IOException e) {
            callBack.call("ERROR", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Sends download request and provides input stream for download
    public DataInputStream getDownloadStream(FSObject source){
        if(!isConnectionAlive()){
            callBack.call("ERROR", "Connection is not established");
            return null;
        }
        try {
            out.write((FILE_DOWNLOAD.name() + COMMDELIM + source.getPath() + FRMDELIM).getBytes());
            return in;
        } catch (IOException e) {
            callBack.call("ERROR", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Sends upload request and provides output stream or null depends on answer
    public DataOutputStream getUploadStream(FSObject source){
        if(!isConnectionAlive()){
            callBack.call("ERROR", "Connection is not established");
            return null;
        }
        String args = String.join(
                COMMDELIM
                , source.getName()
                , source.getPath()
                , String.valueOf(source.getSize())
        );
        Command ans = (Command) requestObjectDirect(FILE_UPLOAD, args);
        if (ans.equals(FILE_UPLOAD)){
            return out;
        } else if(ans.equals(FILE_SKIP)){
            return null;
        }
        // TODO: Throw some exception
        return null;
    }

    // Reads and returns received object from input stream.
    public Object readObject(){
        if(!isConnectionAlive()){
            callBack.call("ERROR", "Connection is not established");
            return null;
        }
        Object o = null;
        try{
            ObjectInputStream ois = new ObjectInputStream(in);
            o = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            callBack.call("ERROR", e.getMessage());
            e.printStackTrace();
        }
        return o;
    }

    // Sends bye message to remote server and closes connection.
    public void stop(){
        try {
            if(isConnectionAlive()){
                out.write((AUTH_OUT.name() + FRMDELIM).getBytes());
                out.flush();
                socket.close();
            }
        } catch (IOException e) {
            callBack.call("ERROR", e.getMessage());
            e.printStackTrace();
        }
    }

    // Checks connection
    private boolean isConnectionAlive(){
        return (socket != null && !socket.isClosed()) && (in != null && out != null);
    }

}
