// NodeClient class
// 20120425

import java.io.*;
import java.util.*;

public class NodeClient {
  private Node node = new Node();
  
  public static void main(String[] args) {
    NodeClient nc = new NodeClient();

	// Initialize node from cache or from data files
    System.out.println("Initializing the node...");
	if ((new File(Node.FILENAME)).exists()) {
		nc.node.readFromFile();
		System.out.println("Node re-initialized...");
	} else {
		nc.loadFiles("data");
		System.out.println("Node initialized...");
	}	
    
    System.out.println("Node contains " + nc.getNode().data.size() + " entries.");
    //for(MusicObject m : nc.getNode().data.values()) System.out.println(m);
	
	// Persist node
	nc.getNode().writeToFile();
	System.out.println("Node persisted...");
  }
  
  public Node getNode() {return this.node;}
  
  public void loadFiles(String dataDir) {
    File[] files = (new File(dataDir)).listFiles();
    for(int i = 0; i < files.length; i++) {
      try {
        File f = files[i];
        String id = f.getName().substring(0, f.getName().lastIndexOf('.'));
        //System.out.println("Adding MusicObject: " + id);
        this.node.addMusicObject(MusicObject.buildFromFile(f, id));
      } catch (Exception ex) {continue;}
    }
  }
}
