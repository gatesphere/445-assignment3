// MusicObject class
// 20120418

import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

public class Node implements Serializable {
  
  private static final String FILENAME = "data";
  
  protected ConcurrentHashMap<Integer, MusicObject> data;
  
  
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
      
      oos.writeObject(this.data);
      
      oos.close();
    } catch (IOException e) { e.printStackTrace(); }
  }
  
  @SuppressWarnings("unchecked")
  protected void readFromFile() {
    try {
      FileInputStream fis = new FileInputStream(FILENAME);
      ObjectInputStream ois = new ObjectInputStream(fis);
      
      this.data = (HashMap<Integer, MusicObject>)ois.readObject();
      
      ois.close();
    } catch (IOException e) { 
      e.printStackTrace(); 
    } catch (ClassNotFoundException e) { 
      e.printStackTrace();
    }
  }
}
