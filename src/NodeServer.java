/**
 * CSC 445
 * Project 3
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.Scanner;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;

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

	private ForkJoinPool fjp;

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
		
		this.fjp = new ForkJoinPool();

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
				// @TODO Research if this is the best way to determine origin
				if (addr.getAddress().equals(con_addr.getAddress())) {
					from_client = false; break;
				}
			}

			new Request(connection, this, from_client);
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
	 * Gets the ForkJoinPool
	 * @return The FJP
	 */
	public ForkJoinPool getFJP() {
		return this.fjp;
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
	public ArrayList<MusicObject> putRequest(String request) {
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
		MusicObject mobj = new MusicObject(id, artist, album_artist, album, track,
											 title, year, genre, track_length);
		this.node.addMusicObject(mobj);
		ArrayList<MusicObject> retval = new ArrayList<MusicObject>();
		retval.add(mobj);
		return retval;
	}
	
	/**
	 * Handles a KILL request
	 * @param request The request string
	 */
	public void killRequest(String request) {
		// catastropic failure... die immediately
		Runtime.getRuntime().halt(-1);
	}
	
}


class Request extends Thread {
	Socket socket;
	NodeServer svr;
	boolean is_leader;

	public Request(Socket connection, NodeServer svr, boolean is_leader) {
		this.socket = connection;
		this.svr = svr;
		this.is_leader = is_leader;
		this.start();
	}
	
	public void run() {
		System.out.format("\n[%s] %s request from: %s\n", 
				(is_leader ? "C" : "N"), (is_leader ? "Client" : "Node"), this.socket.getInetAddress().toString());
		try {
			DataInputStream dis = new DataInputStream(this.socket.getInputStream());
			String request = dis.readUTF();
			System.out.println("[REQUEST] " + request);
			
			parseRequest(request);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public void parseRequest(String request) throws IOException {
		Scanner sc = new Scanner(request);
		switch(sc.next()) {
			case "PUT": 	svr.putRequest(request); break;
			case "KILL": 	svr.killRequest(request); break; 
			case "GET": 	getRequest(request, is_leader); break;
		}
	}

	/**
	 * Handles a GET request
	 * @param request The request string
	 * @todo This needs to be implemented.
	 */
	public void getRequest(String request, boolean is_leader) throws IOException {
		if (is_leader) {
			// Block until FJP has initialized
			while (this.svr.getFJP() == null) { }
			// Distribute requests
			Collection<MusicObject> replies = this.svr.getFJP().invoke(new GetTask(request, this.svr));
			System.out.println("[%] Received replies");
			// Respond to client
			ObjectOutputStream oos = new ObjectOutputStream(this.socket.getOutputStream());
			oos.writeObject(replies);
			oos.flush();
		} else {
			// Query node
			Collection<MusicObject> results = new ArrayList<MusicObject>(); //this.getNode().query();
			// Respond to leader node
			ObjectOutputStream oos = new ObjectOutputStream(this.socket.getOutputStream());
			oos.writeObject(results);
			oos.flush();
			System.out.format("[<] Sent node response to: %s\n", this.socket.getInetAddress().toString());

		}
	}
}

/**
 * A ForkJoinTask for GET requests
 */
class GetTask extends RecursiveTask<Collection<MusicObject>> {

	/**
	 * The NodeServer
	 */
	private NodeServer svr;
	/**
	 * The socket connection
	 */
	private Socket socket;
	/**
	 * The GET request
	 */
	private String request;

	/**
	 * Constructor for initial request
	 * @param request The GET request
	 * @param svr The NodeServer
	 */
	public GetTask(String request, NodeServer svr) {
		this.request = request;
		this.svr = svr;
	}

	/**
	 * Constructor for each node request
	 * @param request The GET request
	 * @param socket The socket for the node connection
	 */
	public GetTask(String request, Socket socket) {
		this.request = request;
		this.socket = socket;
	}

	/**
	 * Distributes request to all connected nodes and asynchronously 
	 * waits for the responses
	 * @return The result set for the request
	 */
	@SuppressWarnings("unchecked")
	public Collection<MusicObject> compute() {
		if (this.svr != null) {
			// Create task for all node servers
			Collection<GetTask> tasks = new ArrayList<GetTask>();
			for (InetSocketAddress addr : svr.getServers()) {
				Socket s = new Socket();
				try {
					s.connect(addr, NodeServer.SERVER_CONNECT_TIMEOUT);
				} catch (ConnectException e) {
					System.err.format("[!] Connection failed: %s\n", addr.toString());	
					continue;
				} catch (IOException e) { e.printStackTrace(); }
				tasks.add(new GetTask(this.request, s));
			}
			
			// Fork join on the replies
			Collection<Collection<MusicObject>> replies = new ArrayList<Collection<MusicObject>>();
			this.invokeAll(tasks);
			for (GetTask task : tasks) {
				replies.add(task.join());
			}
			
			// Add in server query
			//this.replies.add(this.svr.getNode().query(this.request));

			// @TODO Perform consensus and return results
			for (Collection<MusicObject> result : replies) {
				return result;
			}
		} else if (this.socket != null) {
			try {
				// Send request to node
				DataOutputStream dos = new DataOutputStream(this.socket.getOutputStream());
				dos.writeUTF(this.request);
				dos.flush();
				System.out.format("[<] Sent node request to: %s\n", this.socket.getInetAddress().toString());
				
				// Wait for response
				ObjectInputStream ois = new ObjectInputStream(this.socket.getInputStream());
				Collection<MusicObject> response = (Collection<MusicObject>)ois.readObject();
				System.out.format("[>] Response received from: %s\n", this.socket.getInetAddress().toString());	

				return response;
			} catch (IOException e) { 
				System.out.format("[!] No response from: %s\n", this.socket.getInetAddress().toString());
				return new ArrayList<MusicObject>();
			} catch (ClassNotFoundException e) {
				System.err.println(e.getMessage());
				return new ArrayList<MusicObject>();
			}
		} else {
			throw new IllegalStateException("Invalid task.");
		}
		return null;
	}
}
