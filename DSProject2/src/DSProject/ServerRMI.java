package DSProject;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerRMI extends UnicastRemoteObject implements ServerInterface{

	private static final long serialVersionUID = 1L;
	private int serverPort = 0;
	private InetAddress serverIp = null;
	private boolean isCoordinator = false;
	private String serverName = "";
	private ServerInterface coordinator = null;

	//private LinkedList<Article> articles;
	private BulletinBoard bulletinBoard;
	private int nextArticleId = 0;

	public ServerRMI(InetAddress serverIp, int serverPort, boolean isCoordinator)
			throws RemoteException {
		super();

		this.serverIp = serverIp;
		this.serverPort = serverPort;
		this.isCoordinator = isCoordinator;
		
		serverName = serverIp+":"+serverPort;
		
		System.setProperty("java.rmi.server.hostname",
				serverIp.getHostAddress());

		System.out.println("Starting the Server: "
				+ System.getProperty("java.rmi.server.hostname"));
		System.setProperty("java.net.preferIPv4Stack", "true");

		bulletinBoard = new BulletinBoard();

		Registry registry = LocateRegistry.createRegistry(serverPort);
		registry.rebind(serverName, this);
		
		if(!isCoordinator){ // Binding with the coordinator
			// TODO
		}
	}

	@Override
	public boolean Post(String title, String content) throws RemoteException {
		System.out.println("Posting an article!");
		int articleId = coordinator.getNextId();
		Article article = new Article(articleId, title, content);
		bulletinBoard.addArticle(article);
		
		return false;
	}

	@Override
	public String Read() throws RemoteException {
		System.out.println("Reading the articles!");
		return bulletinBoard.ReadArticlesList();
	}
	
	@Override
	public String Choose(int id) throws RemoteException {
		Article a = bulletinBoard.getArticle(id);
		if(a == null){
			System.out.println("The article with ID: "+id+" does no exist in this server!");
			return "";
		}
		return a.completeArticle();
	}

	@Override
	public boolean Reply(int id, String content) throws RemoteException {
		int nextId = coordinator.getNextId();
		return bulletinBoard.reply(id, nextId, content);
		
	}

	@Override
	public int getNextId() throws RemoteException {
		if(!isCoordinator){
			System.out.println("Asking for the next Id to a server that is not the coordinator!");
		}else{
			nextArticleId++;
		}			
		return nextArticleId-1;
	}


}