package DSProject;

import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;

public class ServerRMISeqCons extends UnicastRemoteObject implements ServerInterface {

	private static final long serialVersionUID = 1L;
	
	private String serverName = "";
	
	private boolean isCoordinator = false;
	private String coordinatorName = "";
	private ServerInterface coordinator = null;
	
	private CoordinatorPropagator propagator = null;

	private BulletinBoard bulletinBoard;
	private int nextArticleId = 0;
	private ArrayList<HostRecord> servers = null;
	
	/* Sequential consistency */
	private LinkedList<UpdateOperation> opQueue = null;
	
	public ServerRMISeqCons(InetAddress serverIp, int serverPort,
			boolean isCoordinator, InetAddress coordinatorIp,
			int coordinatorPort)
			throws RemoteException, NotBoundException {
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
			opQueue = new LinkedList<UpdateOperation>();
			propagator = new CoordinatorPropagator(servers, opQueue);
			propagator.start();			
		}
	}

	/* Client -> Server interface */
	@Override
	public boolean Post(String title, String content) throws RemoteException {
		System.out.println("Receiving a Post [Sequential consistency]");
		if (isCoordinator) { // I am the coordinator
			int articleId = getNextId();
			Article a = new Article(articleId, -1, title, content);
			UpdateOperation op = new UpdateOperation(a, "Post");
			opQueue.add(op);
			ackWritePost(articleId, title, content); // I actually write it
		} else { // I am a regular server
			coordinator.updateWritePost(title, content);
		}

		return true;

	}
	
	
	@Override
	public boolean Reply(int id, String content) throws RemoteException {
			System.out.println("Receiving a response [Sequential consistency]");

		if (isCoordinator) { // I am the coordinator
			int articleId = getNextId();
			Article a = new Article(articleId, id, "", content);
			UpdateOperation op = new UpdateOperation(a, "Response");
			opQueue.add(op);
			ackWriteReply(articleId, id, content);
		} else { // I am a regular server
			coordinator.updateWriteReply(id, content);
		}
		return true;
	}
	
	@Override
	public String Read() throws RemoteException {
		System.out.println("Reading the articles!");
		return bulletinBoard.ReadArticlesList();
	}

	@Override
	public String Choose(int id) throws RemoteException {
		Article a = bulletinBoard.getArticle(id);
		if (a == null) {
			System.out.println("The article with ID: " + id
					+ " does no exist in this server!");
			return "";
		}
		return a.completeArticle();
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
		System.out.println("ERROR Calling the function synch in a Sequential Consistency method.");
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

		return true;
	}

	@Override
	public boolean ackWritePost(int id, String title, String content)
			throws RemoteException {
		System.out.println("Received an acknoledgement to write a Post");
		Article article = new Article(id, -1, title, content);
		bulletinBoard.addArticle(article);
		return true;
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

		if (isCoordinator) { // I am the coordinator
			System.out.println("Received Update from a server to propagate a Post");
			int articleId = getNextId();
			Article a = new Article(articleId, -1, title, content);
			UpdateOperation op = new UpdateOperation(a, "Post");
			opQueue.add(op);
			ackWritePost(articleId, title, content); // I actually write it
		} else { // I am a regular server
			System.out
					.println("ERROR Asking for a regular server to propagate an update");
			return false;
		}
		return true;
	}

	@Override
	public boolean updateWriteReply(int id, String content)
			throws RemoteException {

		if (isCoordinator) { // I am the coordinator
			System.out.println("Received Update from a server to propagate a Reply");
			int articleId = getNextId();
			Article a = new Article(articleId, id, "", content);
			UpdateOperation op = new UpdateOperation(a, "Response");
			opQueue.add(op);
			ackWriteReply(articleId, id, content);
		} else { // I am a regular server
			System.out
					.println("ERROR Asking for a regular server to propagate an update");
			return false;
		}
		return true;
	}

	

	@Override
	public int getBBVersion() throws RemoteException {
		System.out.println("ERROR Asking for BB Version in a sequential consistency context");
		return 0;
	}
}