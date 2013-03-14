package DSProject;

import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Queue;

public class ServerRMI extends UnicastRemoteObject implements ServerInterface {

	private static final long serialVersionUID = 1L;
	private int serverPort = 0;
	private InetAddress serverIp = null;
	private int coordinatorPort = 0;
	private InetAddress coordinatorIp = null;
	private boolean isCoordinator = false;
	private String serverName = "";
	private String coordinatorName = "";
	private ServerInterface coordinator = null;
	private String propagationMethod = "";

	private ArrayList<HostRecord> servers = null;
	private Queue<UpdateOperation> opQueue = null;

	// private LinkedList<Article> articles;
	private BulletinBoard bulletinBoard;
	private int nextArticleId = 0;

	public ServerRMI(InetAddress serverIp, int serverPort,
			boolean isCoordinator, InetAddress coordinatorIp,
			int coordinatorPort, String propagationMethod)
			throws RemoteException, NotBoundException {
		super();

		this.serverIp = serverIp;
		this.serverPort = serverPort;
		this.isCoordinator = isCoordinator;
		this.coordinatorIp = coordinatorIp;
		this.coordinatorPort = coordinatorPort;
		this.propagationMethod = propagationMethod;
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
		System.out.println("Propagation method: " + propagationMethod);
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
		}
	}

	/* Client -> Server interface */
	@Override
	public boolean Post(String title, String content) throws RemoteException {
		System.out.println("Posting an article!");

		if(propagationMethod.equalsIgnoreCase("sequential")){
			return PostSeqConsistency(title, content);
		}else if(propagationMethod.equalsIgnoreCase("quorum")){
			
		}else if(propagationMethod.equalsIgnoreCase("read-your-write")){
			
		}
		
		return false;
	}

	public boolean PostSeqConsistency(String title, String content) throws RemoteException{
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
		if(propagationMethod.equalsIgnoreCase("sequential")){
			return ReplySeqConsistency(id, content);
		}else if(propagationMethod.equalsIgnoreCase("quorum")){
			
		}else if(propagationMethod.equalsIgnoreCase("read-your-write")){
			
		}
		
		return false;
	}
	
	public boolean ReplySeqConsistency(int id, String content) throws RemoteException {
		System.out.println("Receiving a response");

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

		return true;
	}

	@Override
	public boolean ackWritePost(int id, String title, String content)
			throws RemoteException {

		Article article = new Article(id, -1, title, content);
		bulletinBoard.addArticle(article);
		return true;
	}

	@Override
	public boolean ackWriteReply(int responseId, int postId, String content)
			throws RemoteException {
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

}