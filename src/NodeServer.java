import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class NodeServer extends Thread {

	public static final int DEFAULT_PORT = 2697;
	public static final String DEFAULT_SERVER_LIST = "servers.txt";
	public static final int SERVER_CONNECT_TIMEOUT = 1000;

	private ServerSocket socket;
	private List<Socket> servers;
	private Node node;

	public NodeServer(int port, String server_list_file) {
		/* Create TCP socket */
		try {
			this.socket = new ServerSocket(port);
		} catch (IOException e) { e.printStackTrace(); System.exit(1); } 

		/* Connect to other servers */
		this.servers = new ArrayList<Socket>();
		// Get list of servers
		try {
			Scanner server_list = new Scanner(new File(server_list_file));
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

		this.start();
	}

	public void run() {

	}

	public static void main(String[] args) {
		int port = (args.length > 0) ? Integer.parseInt(args[0]) : DEFAULT_PORT;
		String server_list_file = (args.length > 1) ? args[1] : DEFAULT_SERVER_LIST;
		new NodeServer(port, server_list_file);
	}
}
