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

  private InetSocketAddress local_address;


  /**
   * Constructor specifying the port and the initial server list.
   * @param port The port.
   * @param server_list_file The initial list of all known nodes.
   */
  public NodeServer(int port, String server_list_file) {
    /* Create TCP socket */
    try {
      this.socket = new ServerSocket(port);
      this.local_address = new InetSocketAddress(this.socket.getInetAddress().getLocalHost(), this.socket.getLocalPort());
    } catch (IOException e) { e.printStackTrace(); System.exit(1); } 
    System.out.println("===================================================");
    System.out.format("\tNode Server: %s\n", this.local_address.toString());
    System.out.println("===================================================");

    /* Connect to other node server */
    this.servers = new ArrayList<InetSocketAddress>();
    // Get list of servers
    Scanner server_list = null;
    try {
      server_list = new Scanner(new File(server_list_file));
    } catch (FileNotFoundException ex) { ex.printStackTrace(); System.exit(1); }
    // Connect to servers and store addresses
    System.out.println("Adding nodes from server file:");
    while (server_list.hasNext()) {
      String svr_host = server_list.next();
      int svr_port = server_list.nextInt();
      InetSocketAddress svr = new InetSocketAddress(svr_host, svr_port);

      if (!this.isSameAddress(this.local_address, svr)) {
        this.servers.add(svr);
        System.out.format("[+] Added node: %s\n", svr.toString());
      }
    }
    System.out.println("---------------------------------------------------");

    /* Initialize node */
    this.node = new Node();
    this.node.readFromFile();
  
    this.start();
  }

  /**
   * Processes the requests.
   */
  public void run() {
    System.out.println("Waiting for requests...");
    while (true) {
      // Accept new TCP connection
      Socket connection = null;
      try { connection = this.socket.accept(); } 
      catch (IOException e) { 
        e.printStackTrace();
        return; //needs to be here to avoid exceptions in the following lines
      }
      
      // Determine origin (client or node)
      InetSocketAddress con_addr = new InetSocketAddress(connection.getInetAddress(), connection.getPort());
      boolean from_client = true;
      for (InetSocketAddress addr : this.servers) {
        //if (this.isSameAddress(addr, con_addr)) {
        // @TODO Research if this is the best way to determine origin
        if (addr.getAddress().equals(con_addr.getAddress())) {
          from_client = false; break;
        }
      }

      // Process request
      if (from_client) {
        new ClientRequest(connection, this);
      } else { 
        new NodeRequest(connection, this);
      }
    }
  }

  /**
   * Determines if two address are the same
   * @param addr1 The first address
   * @param addr2 The second address
   * @return True if the IP address and port are the same
   */
  public boolean isSameAddress(InetSocketAddress addr1, InetSocketAddress addr2) {
    return addr2.getAddress().equals(addr1.getAddress()) && (addr1.getPort() == addr2.getPort());
  }

  /**
   * Gets the list of all other server node addresses
   * @return The list of all other server node addresses
   */
  public List<InetSocketAddress> getServers() {
    return this.servers;
  }

  /**
   * Gets the local address of the server
   * @return The local socket address
   */
  public InetSocketAddress getLocalAddress() {
    return this.local_address;
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
  
  /**
   * Handles a PUT request
   * @param request The request string
   * @todo Paxos? -- Karl
   */
  public void putRequest(String request) {
    Scanner sc = new Scanner(request);
    sc.next(); // throw away the PUT
    sc.useDelimiter(", ");
    String timestamp = sc.next();
    String id = sc.next();
    String artist = sc.next();
    String album_artist = sc.next();
    String album = sc.next();
    int track = sc.nextInt();
    String title = sc.next();
    int year = sc.nextInt();
    String genre = sc.next();
    int track_length = sc.nextInt();
    this.node.addMusicObject(new MusicObject(id, artist, album_artist, album, track,
                                             title, year, genre, track_length));
  }
  
  /**
   * Handles a KILL request
   * @param request The request string
   */
  public void killRequest(String request) {
    // catastropic failure... die immediately
    Runtime.getRuntime().halt(-1);
  }
  
  /**
   * Handles a GET request
   * @param request The request string
   * @todo This needs to be implemented.
   */
  public void getRequest(String request) {
  
  }
}


class ClientRequest extends Thread {
  Socket socket;
  NodeServer svr;

  public ClientRequest(Socket connection, NodeServer svr) {
    System.out.format("\n[C] Client request from: %s\n", connection.getInetAddress().toString());
    this.socket = connection;
    this.svr = svr;
    this.start();
  }
  
  public void run() {
    try {
      System.out.println("[.] Grabbing DataInputStream");
      ObjectOutputStream ois = new ObjectOutputStream(this.socket.getOutputStream());
      DataInputStream dis = new DataInputStream(this.socket.getInputStream());
      String request = dis.readUTF();
      System.out.println(request);
      
      if(request.startsWith("KILL")) svr.killRequest(request);

      for (InetSocketAddress addr : svr.getServers()) {
        Socket s = new Socket();
        try {
          s.connect(addr, NodeServer.SERVER_CONNECT_TIMEOUT);
          //s = new Socket(addr.getAddress(), addr.getPort(), svr.getLocalAddress().getAddress(), svr.getLocalAddress().getPort());
        } catch (ConnectException e) {
          System.err.format("[!] Connection failed: %s\n", addr.toString());  
          continue;
        }
        System.out.println("[.] Grabbing DataOutputStream for " + addr);
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        // @TODO Determine correct action based on request
        dos.writeUTF(request);
        // @TODO Perform consensus
      }
      parseRequest(request);
    } catch (IOException e) { e.printStackTrace(); }
  }
  
  public void parseRequest(String request) {
    Scanner sc = new Scanner(request);
    switch(sc.next()) {
      case "PUT":
        svr.putRequest(request);
        break;
      case "KILL":
        svr.killRequest(request);
        break;
      case "GET":
        svr.getRequest(request);
        break;
    }
  }
}


class NodeRequest extends Thread {
  //ForkJoinPool fjp;
  Socket socket;
  NodeServer svr;

  public NodeRequest(Socket connection, NodeServer svr) {
    System.out.format("\n[N] Node request from: %s\n", connection.getInetAddress().toString());
    this.socket = connection;
    this.svr = svr;
    this.start();
  }
  
  public void run() {
    try {
      DataInputStream dis = new DataInputStream(this.socket.getInputStream());
      String request = dis.readUTF();
      System.out.println(request);
      parseRequest(request);
    } catch (IOException e) { e.printStackTrace(); }
  }
  
  public void parseRequest(String request) {
    Scanner sc = new Scanner(request);
    switch(sc.next()) {
      case "PUT":
        svr.putRequest(request);
        break;
      case "KILL":
        svr.killRequest(request);
        break;
      case "GET":
        svr.getRequest(request);
        break;
    }
  }
}
