package Model;

import javafx.scene.control.Dialog;

import java.io.*;
import java.util.HashMap;

public class GestoreTag {
    public static HashMap<String, String> map;
    private static BufferedReader leggi;
    private static BufferedWriter scrivi;

    public static void init() {
        map = new HashMap<>();
        try {
            leggi = new BufferedReader(new FileReader("tags.txt"));
            while (leggi.ready()) {
                String riga = leggi.readLine();
                String[] entry = riga.split("\\|");
                try {
                    if (!entry[0].isEmpty() && !entry[1].isEmpty()) {
                        if (new File (entry[0]).exists()){
                            map.put(entry[0], entry[1]);
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }
            leggi.close();
        } catch (FileNotFoundException e) {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Errore");
            dialog.setContentText("Errore nella lettura delle TAG");
        } catch (IOException e) {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Errore");
            dialog.setContentText("Errore nella lettura del file delle TAG");
        };
    }

    public static String getValue(String chiave) {
        if (map.containsKey(chiave)) {
            return map.get(chiave);
        }
        return "";
    }

    public static void setValue(String chiave, String value) {
        if(value.isEmpty())
            map.remove(chiave);
        else{
            map.put(chiave, value);
        }

        map.put(chiave, value);
        try {
            scrivi = new BufferedWriter(new FileWriter("tags.txt"));
            for (String key : map.keySet()) {
                if(!map.get(key).isEmpty()){
                    scrivi.write(key + "|" + map.get(key));
                    scrivi.newLine();
                }

            }
            scrivi.close();
        } catch (IOException ignored) {
        }
    }

    public static HashMap<String, String> getMap() {
        HashMap<String, String> mapClone = new HashMap<>();
        mapClone.putAll(map);
        return mapClone;
    }
}
