package DSProject;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class CoordinatorPropagator extends Thread {

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
