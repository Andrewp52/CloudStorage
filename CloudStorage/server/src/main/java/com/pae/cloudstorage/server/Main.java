package com.pae.cloudstorage.server;

import com.pae.cloudstorage.server.data.connectors.DataConnector;
import com.pae.cloudstorage.server.data.connectors.MysqlConnector;
import com.pae.cloudstorage.server.network.NettyServer;
import io.netty.buffer.ByteBufInputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //DataConnector connector = MysqlConnector.getConnector("/database.conf");
       new NettyServer(9999, null);

       /** serialized RW
        List<String> ls = new ArrayList<>();
        ls.add("a");
        ls.add("b");
        FileOutputStream fos = new FileOutputStream("list.o");
        ObjectOutputStream ous = new ObjectOutputStream(fos);
        ous.writeObject(ls);
        fos.flush();

        FileInputStream fis = new FileInputStream("list.o");
        ObjectInputStream ois = new ObjectInputStream(fis);
        List<String> res = (List<String>) ois.readObject();
        System.out.println(res.toString());
        */

    }
}
