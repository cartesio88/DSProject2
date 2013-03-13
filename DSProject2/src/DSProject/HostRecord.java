package DSProject;

public class HostRecord {
	private String ip;
	private int port;
	private String name;
	
	public HostRecord(String ip, int port){
		this.ip = ip;
		this.port = port;
		name = ip+":"+port;
	}
	
	public String getIP(){return ip;}
	public int getPort(){return port;}
	public String getBiningName(){ return name; }
	
	public void setIP(String ip){this.ip = ip;}
	public void setPort(int port){this.port = port;}
	
	
	public String toString(){
		return name;
	}
	
	public boolean equals(Object o){
		HostRecord c = (HostRecord) o;
		return ip.equals(c.getIP()) && port == c.getPort();
	}
}
