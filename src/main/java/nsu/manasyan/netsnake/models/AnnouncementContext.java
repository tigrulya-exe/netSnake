package nsu.manasyan.netsnake.models;

import java.net.InetSocketAddress;

public class AnnouncementContext {
    private boolean isActual;

    private InetSocketAddress masterAddress;

    public AnnouncementContext(boolean isActual, InetSocketAddress masterAddress) {
        this.isActual = isActual;
        this.masterAddress = masterAddress;
    }

    public boolean isActual() {
        return isActual;
    }

    public void setActual(boolean actual) {
        isActual = actual;
    }

    public InetSocketAddress getMasterAddress() {
        return masterAddress;
    }

    public void setMasterAddress(InetSocketAddress masterAddress) {
        this.masterAddress = masterAddress;
    }
}
