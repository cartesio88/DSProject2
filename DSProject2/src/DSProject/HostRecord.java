package DSProject;

public class HostRecord {
	private String ip;
	private int port;
	
	public HostRecord(String ip, int port){
		this.ip = ip;
		this.port = port;
	}
	
	public String getIP(){return ip;}
	public int getPort(){return port;}
	
	public void setIP(String ip){this.ip = ip;}
	public void setPort(int port){this.port = port;}
	
	public String toString(){
		String string="";
		
		string+=ip+":"+port;
		
		return string;
	}
	
	public boolean equals(Object o){
		HostRecord c = (HostRecord) o;
		return ip.equals(c.getIP()) && port == c.getPort();
	}
}
