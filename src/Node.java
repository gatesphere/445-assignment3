/**
 * CSC 445
 * Project 3
 */
 
// Joe Mirizio
// Jacob Peck

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
import java.util.Collection;
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
  protected Collection<MusicObject> query(HashMap<String, String> filter) {
    Collection<MusicObject> resultset = new ArrayList<MusicObject>();
    for(Map.Entry<String, MusicObject> e : data.entrySet()) {
      MusicObject m = e.getValue();
      if(m.matches(filter)) resultset.add(m);
    }
    return resultset;
  }
  
  protected HashMap<String, String> parseRequest(String request) {
    HashMap<String, String> filter = new HashMap<String, String>();

    boolean looking = false;
    boolean master = false;
    boolean seekAll = false;

    int begin = 0;
    int filled = 0;
    int saughtSize = 0;//this is how many responsed are desired

    String[] parsedRequest = new String[10];
    String singleQ = "\'";

    for (int c = 0; c < request.length(); c++) {
      char ch = request.charAt(c);
      if (ch == ',') {
        if (begin < (c - 1)) {
          parsedRequest[filled] = request.substring(begin, c);
          begin = c + 1;
          filled = filled + 1;
        }
        while (c < (request.length() - 1) && request.charAt(c + 1) == ' ') {
          c = c + 1;
          begin = c + 1;
        }
        if (c >= (request.length() - 1)) {
          break;
        }
      } else if (ch == '\'') {
        c = c + 1;
        while (c < (request.length()) && request.charAt(c) != '\'') {
          c = c + 1;
        }
        if (c == (request.length() - 1)) {
          parsedRequest[filled] = request.substring(begin, c + 1);
          filled = filled + 1;
        }
      } else if (ch == ' ') {
        if (begin < (c)) {
          parsedRequest[filled] = request.substring(begin, c);
          begin = c + 1;
          filled = filled + 1;
        }
      }
    } //done building the array of commands

    if (parsedRequest[0].compareToIgnoreCase("get") == 0) {
      if (parsedRequest[1].compareToIgnoreCase("all") == 0) {
        master = true;
        seekAll = true;
      } else if (parsedRequest[1].compareTo("n") == 0) {
        master = false;
        seekAll = true;
      } else {
        try {
          int x = Integer.parseInt(parsedRequest[1]);
          saughtSize = x;
        } catch (NumberFormatException nfe) {
          System.out.println("Number Format Exception: " + nfe.getMessage());
        }
      }

      String[] getFields = null;

      for (int pointer = 2; pointer < filled; pointer++) {//identifies the fields and gets the objects with those clasifications
        System.out.println("parsedRequest at " + pointer + " is " + parsedRequest[pointer]);
        getFields = parsedRequest[pointer].split("=");
        if (getFields[0].compareToIgnoreCase("artist") == 0) {
          filter.put("ARTIST", getFields[1].substring(1, getFields[1].length()-1));
        } else if ((getFields[0].compareToIgnoreCase("albumartist") == 0) || (getFields[0].compareToIgnoreCase("album_artist") == 0)) {
          filter.put("ALBUM_ARTIST", getFields[1].substring(1, getFields[1].length()-1));
        } else if (getFields[0].compareToIgnoreCase("album") == 0) {
          filter.put("ALBUM", getFields[1].substring(1, getFields[1].length()-1));
        } else if (getFields[0].compareToIgnoreCase("track") == 0) {
          filter.put("TRACK", getFields[1].substring(1, getFields[1].length()-1));
        } else if (getFields[0].compareToIgnoreCase("title") == 0) {
          filter.put("TITLE", getFields[1].substring(1, getFields[1].length()-1));
        } else if (getFields[0].compareToIgnoreCase("year") == 0) {
          filter.put("YEAR", getFields[1].substring(1, getFields[1].length()-1));
        } else if (getFields[0].compareToIgnoreCase("genre") == 0) {
          filter.put("GENRE", getFields[1].substring(1, getFields[1].length()-1));
        } else if ((getFields[0].compareToIgnoreCase("tracklength") == 0) || (getFields[0].compareToIgnoreCase("track_length") == 0)) {
          filter.put("TRACK_LENGTH", getFields[1].substring(1, getFields[1].length()-1));
        } else {
          //Improper field label;
        }
      }
    }
    return filter;
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
        MusicObject mobj = MusicObject.buildFromFile(f, id);
        if(mobj.validate()){
          this.addMusicObject(mobj);
          System.out.println(mobj);
        }
      } catch (Exception ex) {continue;}
    }
  }
}
