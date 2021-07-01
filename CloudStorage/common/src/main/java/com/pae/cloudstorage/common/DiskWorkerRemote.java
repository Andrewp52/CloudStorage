package com.pae.cloudstorage.common;

import java.io.IOException;
import java.nio.file.*;
import java.util.StringJoiner;
import java.util.stream.Stream;


// Class for disk operations.
// Needs Callback implementation.
// TODO: remove user data fom here when dataservice and full auth will work
public class DiskWorkerRemote extends DiskWorker{
    static final Path SRVROOT = Path.of("server");
    String nick;
    Path usrRoot;

    public DiskWorkerRemote(String nick, CallBack callBack) throws IOException {
        super(callBack);
        this.nick = nick;
        this.usrRoot = SRVROOT.resolve(Path.of(nick));
        if(!Files.exists(usrRoot)){
            Files.createDirectory(SRVROOT.resolve(Path.of(nick)));
        }
        location = usrRoot;
    }

    public Path getLocation() {
        return location;
    }

    // Changes root directory when user changes it`s profile data (nickname)
    public void changeUserRoot(String newNick) throws IOException {
        Path newRoot = SRVROOT.resolve(Path.of(newNick));
        if(!Files.exists(newRoot)){
            Files.move(usrRoot, newRoot, StandardCopyOption.ATOMIC_MOVE);
            this.location = newRoot;
            this.nick = newNick;
            callBack.call("ok\n");
        } else {
            callBack.call("nickname is occupied\n");
        }
    }

    @Override
    public void getFilesList(){
        StringJoiner sj = new StringJoiner("*", "<DIR_LIST>", "");
        Stream<Path> sp = null;
        try {
            sp = Files.list(location);
            sp.forEach(path ->
                    sj.add(Files.isDirectory(path) ? "<D>" + path.getFileName().toString() : path.getFileName().toString()
                    ));
            sp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        callBack.call(sj.toString());
    }

    // Changes client`s location directory (goes back up to client`s root directory)
    // Calls getFilesList at and for client`s list (remote) update
    @Override
    public void changeDirectory(String dir){
        Path current = location;
        if("..".equals(dir)){
            if(!current.equals(usrRoot)){
                location = current.getParent();
            }
        } else if("~".equals(dir)){
            location = usrRoot;
        } else {
            Path newLocation = location.resolve(dir);
            if(Files.exists(newLocation)){
                location = newLocation;
            }
        }
        getFilesList();
    }

    public Path usrRoot() {
        return usrRoot;
    }
}
