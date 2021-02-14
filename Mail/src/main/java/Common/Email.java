package Common;

import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Email implements Serializable{
    private int ID;
    private final String sender;
    private final ArrayList<String> recipients;
    private final String subject;
    private final String text;
    private final Date date;

    public Email(int id, String mittente, ArrayList<String> destinatari, String oggetto, String testo, Date data) {
        ID = id;
        this.sender = mittente;
        this.recipients = destinatari;
        this.subject = oggetto;
        this.text = testo;
        this.date = data;
    }

    public Date getDate(){
        return date;
    }

    public ArrayList<String> getRecipients() {
        return recipients;
    }

    public int getID() {
        return ID;
    }

    public String getSender() {
        return sender;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public void setID(int id){
        ID = id;
    }

    public void print(){
        System.out.println(this.toString());
    }

    public String toString(){
        return "Mittente: " + sender + "\nDestinatari: " + recipients +"\nOggetto: "+ subject +"\nTesto: "+ text + "\nData: "+ String.valueOf(date);
    }

    public SimpleStringProperty mittenteToProperty(){
        SimpleStringProperty m = new SimpleStringProperty();
        m.set(sender);
        return m;
    }


    public ArrayList<SimpleStringProperty> destinatariToProperty(){
        ArrayList<SimpleStringProperty> a= new ArrayList<>();
        for (String dest : recipients){
            SimpleStringProperty m = new SimpleStringProperty();
            m.set(dest);
            a.add(m);
        }
        return a;
    }

    public SimpleStringProperty dateToProperty(){
        SimpleStringProperty m = new SimpleStringProperty();
        m.set(String.valueOf(date));
        return m;
    }

    public SimpleStringProperty oggettoToProperty(){
        SimpleStringProperty o = new SimpleStringProperty();
        o.set(subject);
        return o;
    }

    public SimpleStringProperty testoToProperty(){
        SimpleStringProperty t = new SimpleStringProperty();
        t.set(text);
        return t;
    }
}
