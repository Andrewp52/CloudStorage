package com.pae.cloudstorage.client.network;

import com.pae.cloudstorage.common.CallBack;
import com.pae.cloudstorage.common.Command;
import com.pae.cloudstorage.common.FSObject;

import java.io.*;
import java.net.Socket;
import static com.pae.cloudstorage.common.Command.AUTH_OUT;
import static com.pae.cloudstorage.common.Command.FILE_DOWNLOAD;

public class Connector {
    private final String DELIM = "$_";
    private final String host = "localhost";
    private final int port = 9999;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public void start(){
        try {
            socket = new Socket(host, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // Sends request to remote server expecting Object answer.
    public void requestObject(Command cmd, String arg, CallBack callBack){
        callBack.call(requestObjectDirect(cmd, arg));
    }

    public Object requestObjectDirect(Command cmd, String arg){
        Object o = null;
        String command = arg == null || arg.isBlank()? cmd.name() + DELIM : cmd.name() + " " + arg + DELIM;
        try {
            out.write((command).getBytes());
            ByteArrayInputStream bis = new ByteArrayInputStream(getBytesFromInput());
            ObjectInputStream ois = new ObjectInputStream(bis);
            o = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return o;
    }

    public DataInputStream getDownloadStream(FSObject source){
        try {
            out.write((FILE_DOWNLOAD.name() + " " + source.getPath() + DELIM).getBytes());
            return in;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Reading byte array fom input stream
    private byte[] getBytesFromInput(){
        byte[] arr;
        try {
            int start = in.readInt();
            int len = in.readInt();
            arr = new byte[len];
            in.read(arr, start, len);
        } catch (IOException e){
            e.printStackTrace();
            return new byte[0];
        }
        return arr;
    }

    // Sends bye message to remote server and closes connection.
    public void stop(){
        try {
            if(isConnectionAlive()){
                out.write((AUTH_OUT.name() + DELIM).getBytes());
                out.flush();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Checks connection
    private boolean isConnectionAlive(){
        return (socket != null && !socket.isClosed()) && (in != null && out != null);
    }

}
