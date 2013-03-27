package DSProject;

import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class ServerRMIQuorumCons extends UnicastRemoteObject implements
		ServerInterface {

	private static final long serialVersionUID = 1L;

	private int QUORUM_NR = 1;
	private int QUORUM_NW = 1;

	private String serverName = "";
	InetAddress coordinatorIp;
	int coordinatorPort = 0;

	private boolean isCoordinator = false;
	private String coordinatorName = "";
	private ServerInterface coordinator = null;

	private BulletinBoard bulletinBoard;
	private int nextArticleId = 0;
	private ArrayList<HostRecord> servers = null;

	/* Quorum consistency */
	private int dbVersion = 0;
	private LinkedList<UpdateOperation> historic = null;

	public ServerRMIQuorumCons(InetAddress serverIp, int serverPort,
			boolean isCoordinator, InetAddress coordinatorIp,
			int coordinatorPort) throws RemoteException, NotBoundException {
		super();

		this.isCoordinator = isCoordinator;

		serverName = serverIp.getHostAddress() + ":" + serverPort;
		if (!isCoordinator)
			coordinatorName = coordinatorIp.getHostAddress() + ":"
					+ coordinatorPort;

		// Printing info
		System.out.println(" == Server info == ");
		if (isCoordinator)
			System.out.println("Role: COORDINATOR");
		else
			System.out.println("Role: REGULAR SERVER");
		System.out.println("Address: " + serverIp.getHostAddress() + ":"
				+ serverPort);
		System.out.println("Binding Name: \"" + serverName + "\"");

		if (!isCoordinator) {
			System.out.println("Coordinator Address: "
					+ coordinatorIp.getHostAddress() + ":" + coordinatorPort);
			System.out.println("Coordinator Binding Name: \"" + coordinatorName
					+ "\"");
		}
		/* ------------------- */

		/* Version Synchronization */

		if (isCoordinator) {
			System.out.println("Run version syncronization....");
			VersionSync sync = new VersionSync();
			sync.start();
		}

		System.setProperty("java.rmi.server.hostname",
				serverIp.getHostAddress());
		System.out.println("Starting the Server: "
				+ System.getProperty("java.rmi.server.hostname"));
		System.setProperty("java.net.preferIPv4Stack", "true");

		bulletinBoard = new BulletinBoard();

		// Creating local registry
		Registry localRegistry = LocateRegistry.createRegistry(serverPort);
		localRegistry.rebind(serverName, this);

		// Regular server initializer
		if (!isCoordinator) { // Binding with the coordinator
			Registry coordinatorRegistry = LocateRegistry.getRegistry(
					coordinatorIp.getHostAddress(), coordinatorPort);

			coordinator = (ServerInterface) coordinatorRegistry
					.lookup(coordinatorName);
			if (!coordinator.register(serverIp.getHostAddress(), serverPort)) { // Registering
																				// with
																				// the
																				// coordinator
				System.out
						.println("ERROR Could not register with the coordinator :/");
			}
		} else { // Coordinator initializer
			coordinator = this;
			servers = new ArrayList<HostRecord>();
			HostRecord coordinator = new HostRecord(serverIp.getHostAddress(),
					serverPort);
			servers.add(coordinator);
		}
	}

	/* Client -> Server interface */
	@Override
	public boolean Post(String title, String content) throws RemoteException {
		System.out.println("Receiving a Post [Quorum consistency]");

		if (isCoordinator) { // I am the coordinator
			Random rnd = new Random();
			System.out.println("Number of servers total:" + servers.size());

			ArrayList<Integer> quorum = new ArrayList<Integer>();
			for (int i = 0; i < servers.size(); i++)
				quorum.add(i);

			// Selecting the quorum
			while (quorum.size() > QUORUM_NW) {
				int i = rnd.nextInt(quorum.size());
				quorum.remove(i);
			}

			int maxVersion = -1;
			HostRecord selectedServer = servers.get(0);

			for (int i = 1; i < quorum.size(); i++) {
				int currentVersion = servers.get(i).rmi.getBBVersion();
				if (currentVersion > maxVersion) {
					maxVersion = currentVersion;
					selectedServer = servers.get(i);
				}
			}

			System.out.println("Latest version: " + maxVersion
					+ " in the server: " + selectedServer);
			System.out.println("Posting in that server :)");

			int nextId = this.getNextId();
			selectedServer.rmi.ackWritePost(nextId, title, content);

		} else { // I am a regular server
			System.out
					.println("ERROR, in QUORUM consistency, the client is supposed to connect to the coordinator directly");
		}

		return true;
	}

	@Override
	public boolean Reply(int id, String content) throws RemoteException {
		System.out.println("Receiving a response [Quorum consistency]");

		if (isCoordinator) { // I am the coordinator
			Random rnd = new Random();
			System.out.println("Number of servers total:" + servers.size());

			ArrayList<Integer> quorum = new ArrayList<Integer>();
			for (int i = 0; i < servers.size(); i++)
				quorum.add(i);

			// Selecting the quorum
			while (quorum.size() > QUORUM_NW) {
				int i = rnd.nextInt(quorum.size());
				quorum.remove(i);
			}

			int maxVersion = -1;
			HostRecord selectedServer = servers.get(0);

			for (int i = 1; i < quorum.size(); i++) {
				int currentVersion = servers.get(i).rmi.getBBVersion();
				if (currentVersion > maxVersion) {
					maxVersion = currentVersion;
					selectedServer = servers.get(i);
				}
			}

			System.out.println("Latest version: " + maxVersion
					+ " in the server: " + selectedServer);
			System.out.println("Replying in that server :)");

			int nextId = this.getNextId();
			selectedServer.rmi.ackWriteReply(nextId, id, content);

		} else { // I am a regular server
			System.out
					.println("ERROR, in QUORUM consistency, the client is supposed to connect to the coordinator directly");
		}
		return true;
	}

	@Override
	public String Read() throws RemoteException {
		System.out.println("Reading the articles!");

		if (isCoordinator) { // I am the coordinator
			Random rnd = new Random();
			System.out.println("Number of servers total:" + servers.size());

			ArrayList<Integer> quorum = new ArrayList<Integer>();
			for (int i = 0; i < servers.size(); i++)
				quorum.add(i);

			// Selecting the quorum
			while (quorum.size() > QUORUM_NR) {
				int i = rnd.nextInt(quorum.size());
				quorum.remove(i);
			}

			int maxVersion = -1;
			HostRecord selectedServer = servers.get(0);

			for (int i = 1; i < quorum.size(); i++) {
				int currentVersion = servers.get(i).rmi.getBBVersion();
				if (currentVersion > maxVersion) {
					maxVersion = currentVersion;
					selectedServer = servers.get(i);
				}
			}

			System.out.println("Latest version: " + maxVersion
					+ " in the server: " + selectedServer);
			System.out.println("Reading from that server :)");

			return selectedServer.rmi.Read();

		} else { // I am a regular server
			System.out
					.println("Reading version: " + bulletinBoard.GetVersion());
			return bulletinBoard.ReadArticlesList();
		}
	}

	@Override
	public String Choose(int id) throws RemoteException {
		if (isCoordinator) { // I am the coordinator
			Random rnd = new Random();
			System.out.println("Number of servers total:" + servers.size());

			ArrayList<Integer> quorum = new ArrayList<Integer>();
			for (int i = 0; i < servers.size(); i++)
				quorum.add(i);

			// Selecting the quorum
			while (quorum.size() > QUORUM_NR) {
				int i = rnd.nextInt(quorum.size());
				quorum.remove(i);
			}

			int maxVersion = -1;
			HostRecord selectedServer = servers.get(0);

			for (int i = 1; i < quorum.size(); i++) {
				int currentVersion = servers.get(i).rmi.getBBVersion();
				if (currentVersion > maxVersion) {
					maxVersion = currentVersion;
					selectedServer = servers.get(i);
				}
			}

			System.out.println("Latest version: " + maxVersion
					+ " in the server: " + selectedServer);
			System.out.println("Choosing from that server :)");

			return selectedServer.rmi.Choose(id);

		} else { // I am a regular server
			System.out
					.println("Reading version: " + bulletinBoard.GetVersion());

			Article a = bulletinBoard.getArticle(id);
			if (a == null) {
				System.out.println("The article with ID: " + id
						+ " does no exist in this server!");
				return "";
			}
			return a.completeArticle();
		}
	}

	/* Server -> Server interface */
	@Override
	public int getNextId() throws RemoteException {
		if (!isCoordinator) {
			System.out
					.println("Asking for the next Id to a server that is not the coordinator!");
		} else {
			nextArticleId++;
		}
		return nextArticleId - 1;
	}

	@Override
	public boolean synch() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean register(String ip, int port) throws RemoteException {

		if (!isCoordinator) {
			System.out
					.println("ERROR Asking a server which is not the coordinator to register another server");
			return false;
		}

		HostRecord server = new HostRecord(ip, port);
		System.out.println("Registering server: " + server);

		if (servers.contains(server)) {
			System.out.println("ERROR The server was already registered ["
					+ server + "]");
			return false;
		}

		servers.add(server);

		// Update the Quorum values
		QUORUM_NW = (int) (servers.size() / 2 + 1);
		QUORUM_NR = servers.size() - QUORUM_NW + 1;

		if (QUORUM_NW > servers.size() || QUORUM_NR > servers.size()) {
			System.out.println("ERROR Asigning invalid values of Quorum!");
		}

		return true;
	}

	@Override
	public boolean ackWritePost(int id, String title, String content)
			throws RemoteException {
		System.out.println("Received an acknoledgement to write a Post");
		Article article = new Article(id, -1, title, content);
		bulletinBoard.addArticle(article);
		return false;
	}

	@Override
	public boolean ackWriteReply(int responseId, int postId, String content)
			throws RemoteException {
		System.out.println("Received an acknoledgement to write a Reply");
		boolean success = bulletinBoard.reply(responseId, postId, content);
		if (!success) {
			System.out.println("ERROR replying!");
		}
		return success;
	}

	@Override
	public boolean updateWritePost(String title, String content)
			throws RemoteException {
		return false;

	}

	@Override
	public boolean updateWriteReply(int id, String content)
			throws RemoteException {
		return false;
	}

	@Override
	public int getBBVersion() throws RemoteException {
		return bulletinBoard.GetVersion();
	}
}