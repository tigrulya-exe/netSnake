package nsu.manasyan.netsnake.contexts;

public class SentMessagesKey {
    private long msgSeq;

    private int receicerId;

    public SentMessagesKey(long msgSeq, int receicerId) {
        this.msgSeq = msgSeq;
        this.receicerId = receicerId;
    }

    public long getMsgSeq() {
        return msgSeq;
    }

    public int getPlayerId() {
        return receicerId;
    }
}
