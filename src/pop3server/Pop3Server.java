package pop3server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import data.Server;
import data.User;
import utils.CustomThread;
import utils.DatabaseUtils;
import utils.NetworkUtils;

public class Pop3Server extends CustomThread {

	private final int PORT = 110;
	public final String TYPE = "SERVER";
	public final String SERVICE = "POP3";
	public final String HOSTNAME = SERVICE + TYPE + ".local";
	private final String DATABASE = HOSTNAME + "_database.db";
	private final String ENDPOINT = "127.0.0.1";
	private final int RETRY_TIME = 2;
	public volatile boolean RUNNING = true;
	public boolean DEMO = false;
	
	private ServerSocket serverSocket;
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	private Server server;
	private User user;

	public void run() {

		System.out.println("INFO: Starting the " + SERVICE + " " + TYPE);
		
		
		server = (Server) DatabaseUtils.Deserialize(DATABASE, DEMO);
		System.out.println("INFO: " + server.getStatus());
		DatabaseUtils.Serialize(server, DATABASE);
		

		while (RUNNING) {

			serverSocket = NetworkUtils.getServerSocket(PORT);
			socket = NetworkUtils.acceptServerSocket(serverSocket);
			input = NetworkUtils.getInput(socket);
			output = NetworkUtils.getOutput(socket);

			System.out.println("INFO: Connected sucessfully");

			NetworkUtils.sendMessage("+OK Service Ready", output);


			do {
				String username = NetworkUtils.waitMessageRegex("(USER) ([a-zA-Z0-9]+)", input);
				NetworkUtils.sendMessage("+OK Password required", output);
				String password = NetworkUtils.waitMessageRegex("(PASS) ([a-zA-Z0-9]+)", input);
				user = server.getUser(username, password);
				if (user != null) {
					NetworkUtils.sendMessage("+OK User logged in.", output);
					// System.out.println(user.getUsername());
				} else {
					NetworkUtils.sendMessage("-ERR Login Failed.", output);
				}
			} while (user == null);

			do {
				String command[] = NetworkUtils.waitMessageRegexArray("(([a-zA-Z]{4})) ?(.*)", input);

				int mailId;

				switch (command[2]) {

				case "STAT":
					NetworkUtils.sendMessage(server.status(user), output);
					break;

				case "DELE":
					mailId = Integer.parseInt(command[3]); // [3] is the argument
					NetworkUtils.sendMessage(server.delete(user, mailId), output);
					break;

				case "RSET":
					NetworkUtils.sendMessage(server.reset(user), output);
					break;

				case "RETR":
					mailId = Integer.parseInt(command[3]); // [3] is the argument
					NetworkUtils.sendMessage(server.retrieve(user, mailId), output);
					break;

				case "LIST":
					NetworkUtils.sendMessage(server.list(user), output);
					break;

				case "QUIT":
					NetworkUtils.sendMessage("+OK", output);
					user = null;
					break;

				case "@error_endpoint_disconnected":
					user = null;
					break;

				default:
					NetworkUtils.sendMessage("-ERR Unknown command.", output);

				}
				
				DatabaseUtils.Serialize(server, DATABASE);
				System.out.println("INFO: " + server.getStatus());
			} while (user != null);

			// while(true) NetworkUtils.waitMessage(input);

			NetworkUtils.closeServerSocket(serverSocket);
			NetworkUtils.closeSocket(socket);

		} // while

	} // run
	
	public Pop3Server() {
		
	}
	
	public Pop3Server(boolean createDemoDatabase) {
		
		if(createDemoDatabase) {
			DEMO = true;
		}
		
	}

	public void CheckUser() {

	}

	public Socket getSocket() {
		return socket;
	}

	public BufferedReader getInput() {
		return input;
	}

	public PrintWriter getOutput() {
		return output;
	}

}
