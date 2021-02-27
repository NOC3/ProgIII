package Common;

public class Message {


    //
    public static final short ERROR=-1;
    public static final short SUCCESS=0;
    public static final short NEW_EMAIL=1;
    public static final short REMOVE_EMAIL=2;
    public static final short LOGIN = 3;



    private final int operation;
    private final Object obj;
    private final String message;


    public Message(int operation, Object obj, String message) {
        this.operation = operation;
        this.obj = obj;
        this.message = message;
    }

    public int getOperation() {
        return operation;
    }

    public Object getObj() {
        return obj;
    }
}