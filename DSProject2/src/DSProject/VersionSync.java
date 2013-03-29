package DSProject;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;

public class VersionSync implements Runnable {
	
	ArrayList<HostRecord> servers;
	LinkedList<UpdateOperation> historic;
	
	public VersionSync(ArrayList<HostRecord> servers, LinkedList<UpdateOperation> historic){
		this.servers = servers;
		this.historic = historic;
	}
	
    public void run() {
    	
    	while(true){
  
    		int latestVersion = historic.size();
    		
    		for (int i = 0; i<servers.size(); i++){
    			try {
    				
    				servers.get(i).rmi.synch(historic.size());
    				int currentVersion = servers.get(i).rmi.getBBVersion();
    				//System.out.println("Server: "+i+"Current Version: " + currentVersion+" Latest Version: " + latestVersion);
    				for (int j = currentVersion; j < latestVersion; j++) {
    					
    					String title = historic.get(j).getArticle().getTitle();
    					String content = historic.get(j).getArticle().getContent();
    					int postId = historic.get(j).getArticle().getParentID();
    					
    					if (historic.get(j).getType() == "Post"){
    							servers.get(i).rmi.ackWritePost(j, title, content);
    						}
    					else if (historic.get(j).getType() == "Response"){
    						System.out.println("postId: "+postId);
    							servers.get(i).rmi.ackWriteReply(j, postId, content);
    					}
    				}
    				
				} catch (RemoteException e) {
					System.out.println("Server is out of reach!");
					e.printStackTrace();
				}
    		}
    		try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }	
}