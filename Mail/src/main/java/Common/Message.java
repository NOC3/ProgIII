package Common;

import java.io.Serializable;

//protocollo di comunicazione client-server
public class Message implements Serializable {

    //flag di operazione
    public static final short ERROR = -1;
    public static final short SUCCESS = 0;
    public static final short SEND_NEW_EMAIL = 1;
    public static final short LOGIN = 3;
    public static final short CHECK_NEW = 4;
    public static final short REMOVE_EMAIL_INBOX = 5;
    public static final short REMOVE_EMAIL_SENT = 6;

    private final int operation;
    private final Object obj;

    public Message(int operation, Object obj) {
        this.operation = operation;
        this.obj = obj;
    }

    //metodi get
    public int getOperation() {
        return operation;
    }

    public Object getObj() {
        return obj;
    }
}