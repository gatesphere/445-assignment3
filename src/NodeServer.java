/**
 * CSC 445
 * Project 3
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

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
  private List<Socket> servers;
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
    this.servers = new ArrayList<Socket>();
    // Get list of servers
    Scanner server_list = null;
    try {
      server_list = new Scanner(new File(server_list_file));
    } catch (FileNotFoundException ex) { ex.printStackTrace(); System.exit(1); }
    // Connect to servers and store socket connections
    while (server_list.hasNext()) {
      String svr_host = server_list.next();
      int svr_port = server_list.nextInt();
      System.out.format("Connecting to: %s:%d\n", svr_host, svr_port);
      try {
        Socket svr = new Socket();
        svr.connect(new InetSocketAddress(svr_host, svr_port), SERVER_CONNECT_TIMEOUT);
        this.servers.add(svr);
        System.out.format("[+] Connection established: %s:%d\n", svr_host, svr_port);
      } catch (IOException e) { System.out.format("[!] %s: %s:%d\n", e.getMessage(), svr_host, svr_port); }
    }

    /* Initialize node */
    this.node = new Node();
    this.node.readFromFile();

    this.start();
  }

  /**
   * Processes the requests.
   */
  public void run() {
    while (true) {
      // @TODO Receive requests and process actions
      // @TODO Check status of all other nodes
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
    List<MusicObject> results = new ArrayList<MusicObject>();//this.node.query(query);
    for (Socket sock : this.servers) {
      // @TODO Send GET to all other nodes, receive List<MusicObject> through OOS
    }
    // @TODO Perform consensus
  
    // @TODO Limit and return results
    return results;
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
