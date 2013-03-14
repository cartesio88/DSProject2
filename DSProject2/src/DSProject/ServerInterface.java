package DSProject;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
	/* Client -> Server interface */
	boolean Post(String title, String content) throws RemoteException;
	String Read() throws RemoteException;
	String Choose(int id) throws RemoteException;
	boolean Reply(int id, String content) throws RemoteException;
	
	/* Server -> Coordinator interface */
	int getNextId() throws RemoteException;
	boolean register(String ip, int port) throws RemoteException; // A server register to the coordinator
	
	// Sequential consistency
	boolean updateWritePost(String title, String content) throws RemoteException; // Propagates an update Server -> Coordinator
	boolean updateWriteReply(int id, String content) throws RemoteException; // Propagates an update Server -> Coordinator
	boolean ackWritePost(int id, String title, String content) throws RemoteException; // Perform the actual write Coordinator -> Server
	boolean ackWriteReply(int responseId, int postId, String content) throws RemoteException; // Perform the actual write Coordinator -> Server
	
	// Quorum based consistency
	boolean synch() throws RemoteException; // To update all replicas
} 
