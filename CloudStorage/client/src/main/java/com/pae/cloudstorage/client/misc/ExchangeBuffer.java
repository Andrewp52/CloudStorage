package com.pae.cloudstorage.client.misc;

import com.pae.cloudstorage.common.FSObject;

import java.nio.file.Path;
import java.util.List;

/**
 * Exchange buffer for Copy, Cut, Paste operations
 */
public class ExchangeBuffer {
       private List<FSObject> list;
       private boolean local;
       private boolean move;

    public ExchangeBuffer(List<FSObject> list, boolean local, boolean move) {
        this.list = list;
        this.local = local;
        this.move = move;
    }

    public void setMove(boolean move) {
        this.move = move;
    }

    public List<FSObject> getList() {
        return list;
    }

    public boolean isLocal() {
        return local;
    }

    public boolean isMove() {
        return move;
    }
}
