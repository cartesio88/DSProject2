package DSProject;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class CoordinatorPropagator extends Thread {

	private static  final int MIN_DELAY = 500; // Half a second
	private static  final int MAX_DELAY = 1000; // Five seconds
	
	
	private LinkedList<UpdateOperation> opQueue = null;
	private ArrayList<HostRecord> servers = null;

	public CoordinatorPropagator(ArrayList<HostRecord> servers,
			LinkedList<UpdateOperation> opQueue) {
		this.servers = servers;
		this.opQueue = opQueue;
	}

	@Override
	public void run() {
		System.out.println("CoordinatorPopagator is UP :)...");
		while (true) {
			UpdateOperation op;
			while ((op = opQueue.poll()) != null) {
				System.out.println("Propagating operation: " + op);
				Iterator<HostRecord> serverIt = servers.iterator();
				while (serverIt.hasNext()) {
					HostRecord server = serverIt.next();
					try {
						if (op.getType().equalsIgnoreCase("Post")) {

							server.getRMI().ackWritePost(
									op.getArticle().getID(),
									op.getArticle().getTitle(),
									op.getArticle().getContent());

						} else if (op.getType().equalsIgnoreCase("Response")) {
							server.getRMI().ackWriteReply(
									op.getArticle().getID(),
									op.getArticle().getParentID(),
									op.getArticle().getContent());
						} else {
							System.out
									.println("Unrecognized update operation type");
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {
						System.out
								.print("ERROR Some of the servers could not be binded properly: "
										+ server);
					}
					
					// Fake delay
					try {
						Thread.sleep((long) (MIN_DELAY + Math.random()*MAX_DELAY));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
