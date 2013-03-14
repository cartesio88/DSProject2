package DSProject;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class HostRecord {
	private String ip;
	private int port;
	private String name;
	public ServerInterface rmi = null;
	
	public HostRecord(String ip, int port){
		this.ip = ip;
		this.port = port;
		name = ip+":"+port;
		bind();
	}
	
	public String getIP(){return ip;}
	public int getPort(){return port;}
	public String getBiningName(){ return name; }
	public ServerInterface getRMI() { return rmi; }
	
	public void setIP(String ip){this.ip = ip;}
	public void setPort(int port){this.port = port;}
	
	private boolean bind(){
		Registry registry;
		try {
			registry = LocateRegistry.getRegistry(ip, port);
			rmi = (ServerInterface) registry.lookup(name);
		} catch (RemoteException e) {
			rmi = null;
			e.printStackTrace();
			return false;
		} catch (NotBoundException e) {
			System.out.println("Server not available "+toString());
			rmi = null;
			return false;
		}
		return true;
	}
	
	public String toString(){
		return name;
	}
	
	public boolean equals(Object o){
		HostRecord c = (HostRecord) o;
		return ip.equals(c.getIP()) && port == c.getPort();
	}
}
