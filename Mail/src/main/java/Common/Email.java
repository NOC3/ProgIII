package Common;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//oggetto che rappresenta l'email
public class Email implements Serializable {

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

    //Costruttore generico
    public Email(Object id, Object mittente, Object destinatari, Object oggetto, Object testo, Object data) {
        ID = (int) id;
        this.sender = (String) mittente;
        this.recipients = recipientsToList((String) destinatari);
        this.subject = (String) oggetto;
        this.text = (String) testo;

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        Date d = null;
        try {
            d = formatter.parse((String) data);
        } catch (ParseException e) {
            System.out.println("Formato data non riconosciuto");
        } finally {
            this.date = d;
        }
    }

    //metodi get e set
    public Date getDate() {
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

    public void setID(int id) {
        ID = id;
    }

    @Override
    public String toString() {
        return "Mittente: " + sender + "\nData: " + date + "\nDestinatari: " + recipientsToString() + "\nOggetto: " + subject + "\nTesto:\n" + text;
    }

    //converte la stringa dei destinatari in array
    public ArrayList<String> recipientsToList(String s) {
        ArrayList<String> al = new ArrayList<>();
        for (String st : s.split(",")) {
            al.add(st);
        }
        return al;
    }

    //converte l'array dei destinatari in stringa
    public String recipientsToString() {
        ArrayList<String> rec = getRecipients();
        String recStr = "";
        for (String r : rec) {
            recStr += r + ",";
        }
        return recStr.substring(0, recStr.length() - 1);
    }

    //converte la mail JSONObject
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", this.ID);
        json.put("sender", this.sender);
        json.put("recipients", this.recipientsToString());
        json.put("subject", this.subject);
        json.put("text", this.text);
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String data = formatter.format(this.date);
        json.put("date", data);
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Email) {
            return this.ID == ((Email) o).getID();
        } else {
            return false;
        }
    }

    //controllo validità mail tramite regex
    public static boolean validateEmailAddress(String email) {
        Pattern pattern = Pattern.compile("^.+@.+\\..+$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}