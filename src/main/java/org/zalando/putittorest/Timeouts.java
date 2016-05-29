package org.zalando.putittorest;

import java.util.concurrent.TimeUnit;

public final class Timeouts {

    private int connect;
    private int read;

    public Timeouts(final int connect, final int read) {
        this.connect = connect;
        this.read = read;
    }

    public int getConnect() {
        return connect;
    }

    public void setConnect(final int connect) {
        this.connect = connect;
    }

    public int getRead() {
        return read;
    }

    public void setRead(final int read) {
        this.read = read;
    }

    public static int toMillis(final int value) {
        return (int) TimeUnit.SECONDS.toMillis(value);
    }

}
