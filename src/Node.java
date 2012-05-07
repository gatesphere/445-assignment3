/**
 * CSC 445
 * Project 3
 */

import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * A representation of a persistent data node.
 */
public class Node implements Serializable {
  
  /**
   * The filename for the data persistence.
   */
  protected static final String FILENAME = "data.bin";
  
  /**
   * The data stored in the node.
   */
  protected ConcurrentHashMap<String, MusicObject> data;
  
  
  /**
   * Constructor.
   */
  public Node() { 
    this.data = new ConcurrentHashMap<String, MusicObject>();
  }
  
  /**
   * Add a MusicObject to the node.
   * @param song The song to be added.
   */
  public void addMusicObject(MusicObject song) {
    this.data.put(song.getId(), song); 
  }
  
  /**
   * Persists the data to the file.
   */
  protected void writeToFile() {
    try {
      FileOutputStream fos = new FileOutputStream(FILENAME);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      
      oos.writeObject(this.data);
      
      oos.close();
    } catch (IOException e) { e.printStackTrace(); }
  }
  
  /**
   * Reinitializes the node from the file.
   */
  @SuppressWarnings("unchecked")
  protected void readFromFile() {
    try {
      FileInputStream fis = new FileInputStream(FILENAME);
      ObjectInputStream ois = new ObjectInputStream(fis);
      
      this.data = (ConcurrentHashMap<String, MusicObject>)ois.readObject();
      
      ois.close();
    } catch (IOException e) { 
      e.printStackTrace(); 
    } catch (ClassNotFoundException e) { 
      e.printStackTrace();
    }
  }
  
  /**
   * Query for data.
   * filter - a HashMap<String,String> for matching against.
   */
  protected ArrayList<MusicObject> query(HashMap<String, String> filter) {
    ArrayList<MusicObject> resultset = new ArrayList<MusicObject>();
    for(Map.Entry<String, MusicObject> e : data.entrySet()) {
      MusicObject m = e.getValue();
      if(m.matches(filter)) resultset.add(m);
    }
    return resultset;
  }
  
  /**
   * main - initialize node and save to file
   */
  public static void main(String[] args) {
    Node node = new Node();
    node.loadFiles("data");
    node.writeToFile();
  }
   
  /**
   * Loads all of the files in dataDir into the node
   */
  private void loadFiles(String dataDir) {
    File[] files = (new File(dataDir)).listFiles();
    for(int i = 0; i < files.length; i++) {
      try {
        File f = files[i];
        String id = f.getName().substring(0, f.getName().lastIndexOf('.'));
        //System.out.println("Adding MusicObject: " + id);
        this.addMusicObject(MusicObject.buildFromFile(f, id));
      } catch (Exception ex) {continue;}
    }
  }
}
