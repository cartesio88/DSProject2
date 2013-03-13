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
	boolean synch() throws RemoteException; // To update all replicas
}
