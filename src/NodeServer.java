/**
 * CSC 445
 * Project 3
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class NodeServer extends Thread {

	public static final int DEFAULT_PORT = 2697;
	public static final String DEFAULT_SERVER_LIST = "servers.txt";
	public static final int SERVER_CONNECT_TIMEOUT = 1000;

	/**
	 * The socket connection for listening.
	 */
	private ServerSocket socket;
	/**
	 * A list of all connected nodes.
	 */
	private List<InetSocketAddress> servers;
	/**
	 * The persistent data node.
	 */
	private Node node;


	/**
	 * Constructor specifying the port and the initial server list.
	 * @param port The port.
	 * @param server_list_file The initial list of all known nodes.
	 */
	public NodeServer(int port, String server_list_file) {
		/* Create TCP socket */
		try {
			this.socket = new ServerSocket(port);
		} catch (IOException e) { e.printStackTrace(); System.exit(1); } 

		/* Connect to other servers */
		this.servers = new ArrayList<InetSocketAddress>();
		// Get list of servers
		Scanner server_list = null;
		try {
			server_list = new Scanner(new File(server_list_file));
		} catch (FileNotFoundException ex) { ex.printStackTrace(); System.exit(1); }
		// Connect to servers and store socket connections
		while (server_list.hasNext()) {
			String svr_host = server_list.next();
			int svr_port = server_list.nextInt();
			//try {
			this.servers.add(new InetSocketAddress(svr_host, svr_port));
				//System.out.format("[+] Connection established: %s:%d\n", svr_host, svr_port);
			//} catch (IOException e) { System.out.format("[!] %s: %s:%d\n", e.getMessage(), svr_host, svr_port); }
		}

		/* Initialize node */
		/*this.node = new Node();
		this.node.readFromFile();*/
	
		this.start();
	}

	/**
	 * Processes the requests.
	 */
	public void run() {
		while (true) {
			Socket connection = null;
			try { connection = this.socket.accept(); } 
			catch (IOException e) { 
        e.printStackTrace();
        return; //needs to be here to avoid exceptions in the following lines
      }
			
			// Determine origin
			InetSocketAddress con_addr = new InetSocketAddress(connection.getInetAddress(), connection.getPort());
			boolean from_client = true;
			for (InetSocketAddress addr : this.servers) {
				if (addr.getAddress().equals(con_addr.getAddress()) && (addr.getPort() == con_addr.getPort())) {
					from_client = false; break;
				}
			}
			// Process request
			if (from_client) {
				new ClientRequest(connection, this.node);	
			} else { 
				new NodeRequest(connection, this.node);
			}
		}
	}

	/**
	 * Adds a MusicObject to the node and updates all connected nodes.
	 * @param song The song to be added.
	 */
	private void addMusicObject(MusicObject song) {
		this.node.addMusicObject(song);
		// @TODO Send updates to all nodes (possibly do a consensus check)
	}

	/**
	 * Queries all nodes and returns the appropriate dataset.
	 * @param query The query.
	 * @return The list of all appropriate MusicOjects.
	 */
	private List<MusicObject> queryAll(String query) {
		List<List<MusicObject>> listFromNodes = new ArrayList<List<MusicObject>>();//this.node.query(query); -->uhh...lolwut??
    int numberOfNodes = this.servers.size();
		for (InetSocketAddress addr : this.servers) {
			// make the connection, send the query, receive the List<MusicObject>
      try{
        Socket sock = new Socket(addr.getAddress(), addr.getPort(), InetAddress
                .getLocalHost(), NodeServer.DEFAULT_PORT);
        ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
        oos.writeObject(query);
        Object readObject = ois.readObject();
        if(!(readObject instanceof List)){
          continue;
        }
        listFromNodes.add((List<MusicObject>)readObject);
      } catch (IOException | ClassNotFoundException e){
        e.printStackTrace();
      }
		}
    
    /*
     * lemme break it down playa...(since this is a convoluted way to do this...
     * by all means if anyone can think of a better way to do this then let me
     * know...I write spaghetti code consistently the first time I write anything)
     */
    //list to store all data as the nodes agree on it
    java.util.HashMap <Integer, List<MusicObject>> map = new java.util.HashMap<>();
    //continue to iterate until the list is less than n/2 + 1, since any element
    //past that number cannot recieve concensus, by definition of concensus
    while(listFromNodes.size() > (numberOfNodes / 2 + 1)){
      //pop off the first element
      List <MusicObject> current = listFromNodes.remove(0);
      //variable to maintain how many nodes agree on this value
      int concensusCount = 1;
      //iterate through all remaining lists from all the other nodes
      for (int i = 0; i < listFromNodes.size(); i++) {
        //the list we will be comparing to 
        List <MusicObject> comparingList = listFromNodes.get(i);
        //if the sizes are different then they are inherently different lists-->go to next list
        if(comparingList.size() != current.size()){continue;}
        //varible for after the for loop is finished to increment the concensus count
        boolean listsAreEqual = true;
        //go element by element and make sure the lists contain exactly the same elements,
        //in exactly the same order
        for (int j = 0; j < comparingList.size(); j++) {
          if(comparingList.get(j).compareTo(current.get(i)) != 0){
            listsAreEqual = false;
            break;
          }
        }
        
        if(listsAreEqual){
          concensusCount++;
        }
      }
      //put it in the map...if a key already exists with the same concensus, then it really
      //doesn't matter, because both would be valid return lists in the end
      map.put(concensusCount, current);
    }
    //this is where it gets stupid...sort the keys, and pull out the one with
    //the highest concensus value
    Integer[] keyArray = (Integer [])map.keySet().toArray();
    Arrays.sort(keyArray);
    List<MusicObject> result = map.get(keyArray[keyArray.length - 1]);
    //if it isn't n/2 + 1 concnensus, then return nothing...fail
    if(result.size() < (numberOfNodes / 2 + 1))
      return null;
    else
      return result;
	}

	/**
	 * Starts the server.
	 * @param args[0] The port to listen on.
	 * @param args[1] The initial list of all other known nodes.
	 */
	public static void main(String[] args) {
		int port = (args.length > 0) ? Integer.parseInt(args[0]) : DEFAULT_PORT;
		String server_list_file = (args.length > 1) ? args[1] : DEFAULT_SERVER_LIST;
		new NodeServer(port, server_list_file);
	}


}

class ClientRequest extends Thread {
	Socket socket;

	public ClientRequest(Socket connection, Node n) {
		System.out.format("[+] Client Connection from: %s\n", connection.getInetAddress().toString());
		this.socket = connection;
		this.start();
	}
	
	public void run() {
		try {
			DataInputStream dis = new DataInputStream(this.socket.getInputStream());
			String request = dis.readUTF();
			System.out.println(request);
		} catch (IOException e) { e.printStackTrace(); }
	}
}

class NodeRequest extends Thread {
	//ForkJoinPool fjp;
	Socket socket;

	public NodeRequest(Socket connection, Node n) {
		System.out.format("[+] Server Connection from: %s\n", connection.getInetAddress().toString());
		this.socket = connection;
		this.start();
	}
	
	public void run() {
		try {
			DataInputStream dis = new DataInputStream(this.socket.getInputStream());
			String request = dis.readUTF();
			System.out.println(request);
		} catch (IOException e) { e.printStackTrace(); }
	}

}
