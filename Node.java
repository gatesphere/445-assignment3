import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

public class Node implements Serializable {
    
    private static final FILENAME = "data";
    
    protected HashMap<Integer, MusicObject> data;
    
    
    public Node() { 
        this.data = new HashMap<Integer, MusicObject>();
    }
    
    public void addMusicObject(MusicObject song) {
        this.data.put(song.getId(), song); 
    }
    
    protected void writeToFile() {
        try {
            FileOutputStream fos = new FileOutputStream(FILENAME);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            
            oos.write(this.data);
            
            oos.close();
            fos.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    protected void readFromFile() {
        try {
            FileInputStreaInputStream fis = new FileInputStream(FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            
            this.data = (HashMap<Integer, MusicObject>)ois.read(data);
            
            oos.close();
            fos.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
}