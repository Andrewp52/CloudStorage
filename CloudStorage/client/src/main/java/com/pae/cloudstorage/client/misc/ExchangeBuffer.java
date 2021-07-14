package com.pae.cloudstorage.client.misc;

import com.pae.cloudstorage.common.FSObject;

import java.nio.file.Path;
import java.util.List;

/**
 * Exchange buffer for Copy, Cut, Paste operations
 */
public class ExchangeBuffer {
       private List<FSObject> list;
       private Path origin;
       private boolean local;
       private boolean move;

    public ExchangeBuffer(List<FSObject> list, Path origin, boolean local, boolean move) {
        this.list = list;
        this.origin = origin;
        this.local = local;
        this.move = move;
    }

    public void setMove(boolean move) {
        this.move = move;
    }

    public List<FSObject> getList() {
        return list;
    }

    public Path getOrigin() {
        return origin;
    }

    public boolean isLocal() {
        return local;
    }

    public boolean isMove() {
        return move;
    }
}
