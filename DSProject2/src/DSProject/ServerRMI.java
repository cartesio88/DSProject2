package DSProject;

import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

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

	// private LinkedList<Article> articles;
	private BulletinBoard bulletinBoard;
	private int nextArticleId = 0;

	public ServerRMI(InetAddress serverIp, int serverPort,
			boolean isCoordinator, InetAddress coordinatorIp,
			int coordinatorPort, String propagationMethod) throws RemoteException, NotBoundException {
		super();

		this.serverIp = serverIp;
		this.serverPort = serverPort;
		this.isCoordinator = isCoordinator;
		this.coordinatorIp = coordinatorIp;
		this.coordinatorPort = coordinatorPort;
		this.propagationMethod = propagationMethod;
		serverName = serverIp.getHostAddress() + ":" + serverPort;
		if(!isCoordinator) coordinatorName = coordinatorIp.getHostAddress() + ":" + coordinatorPort;

		// Printing info
		System.out.println(" == Server info == ");
		if (isCoordinator)
			System.out.println("Role: COORDINATOR");
		else
			System.out.println("Role: REGULAR SERVER");
		System.out.println("Address: " + serverIp.getHostAddress() + ":" + serverPort);
		System.out.println("Binding Name: \"" + serverName+ "\"");
		
		
		if (!isCoordinator) {
			System.out.println("Coordinator Address: " + coordinatorIp.getHostAddress() + ":"
					+ coordinatorPort);
			System.out.println("Coordinator Binding Name: \"" + coordinatorName + "\"");
		}
		System.out.println("Propagation method: "+propagationMethod);
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

			coordinator = (ServerInterface) coordinatorRegistry.lookup(coordinatorName);
			if(!coordinator.register(serverIp.getHostAddress(), serverPort)){ // Registering with the coordinator
				System.out.println("ERROR Could not register with the coordinator :/");
			}
		}else{ // Coordinator initializer
			coordinator = this;
			servers = new ArrayList<HostRecord>();
		}

	}

	@Override
	public boolean Post(String title, String content) throws RemoteException {
		System.out.println("Posting an article!");
		int articleId = coordinator.getNextId();
		Article article = new Article(articleId, title, content);
		bulletinBoard.addArticle(article);

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

	@Override
	public boolean Reply(int id, String content) throws RemoteException {
		System.out.println("Receiving a response");
		int nextId = coordinator.getNextId();
		boolean success = bulletinBoard.reply(id, nextId, content);
		if(!success){
			System.out.println("ERROR replying!");
		}
		return success;

	}

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
		
		if(!isCoordinator){
			System.out.println("ERROR Asking a server which is not the coordinator to register another server");
			return false;
		}
		
		HostRecord server = new HostRecord(ip, port);
		System.out.println("Registering server: "+server);
		
		if(servers.contains(server)){
			System.out.println("ERROR The server was already registered ["+server+"]");
			return false;
		}
		
		servers.add(server);
		
		return true;
	}

}