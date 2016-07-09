package org.zalando.putittorest;

import java.util.concurrent.TimeUnit;

public final class Timeouts {

    private int connect;
    private TimeUnit connectUnit = TimeUnit.SECONDS;
    private int read;
    private TimeUnit readUnit = TimeUnit.SECONDS;

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

    public TimeUnit getConnectUnit() {
        return connectUnit;
    }

    public void setConnectUnit(final TimeUnit connectUnit) {
        this.connectUnit = connectUnit;
    }

    public int getRead() {
        return read;
    }

    public void setRead(final int read) {
        this.read = read;
    }

    public TimeUnit getReadUnit() {
        return readUnit;
    }

    public void setReadUnit(final TimeUnit readUnit) {
        this.readUnit = readUnit;
    }

}
