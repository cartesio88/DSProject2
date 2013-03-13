import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;

import DSProject.ServerRMI;


public class Server {

	public static void main(String[] args) throws Exception {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.rmi.server.codebase", "file:./bin");
		System.setProperty("java.security.policy", "file:./policyfile");
		Scanner scan = new Scanner(System.in); 
		int serverRMIPort = portCheck(args[0],scan);
		
		InetAddress serverIp = getServerIP();
				
		boolean isCoordinator = false;
		ServerRMI server = new ServerRMI(serverIp, serverRMIPort, isCoordinator);	
	}
	
	protected static InetAddress getServerIP() {
		InetAddress serverIp = null;
		try {
			Enumeration<NetworkInterface> nets = NetworkInterface
					.getNetworkInterfaces();

			while (nets.hasMoreElements()) {
				NetworkInterface ni = nets.nextElement();
				if (!ni.isLoopback() && ni.isUp()) {
					serverIp = ni.getInetAddresses().nextElement();
					break;
				}
			}
			System.setProperty("java.rmi.server.hostname",serverIp.getHostAddress());
			
		} catch (SocketException e) {
			System.out.println("ERROR getting the interfaces of the device");
			e.printStackTrace();
		}
		return serverIp;
	}
	
	private static int portCheck(String s, Scanner scan){

		int p = 0;
		boolean badRange = true;
		boolean notInt = true;
				
		if (s.length() == 0) p=1099;
			else {
				while (badRange || notInt) {
			
					try{
						Integer.valueOf(s);
						notInt = false;
						p = Integer.valueOf(s);
						}
					catch(NumberFormatException e){
						notInt = true;
						System.out.println("Port has wrong format try again: ");
						s = scan.nextLine();
					}
				
					badRange = p < 0 || p > 55901;
					if (badRange){
						System.out.println("Port is out of range try again: ");
						p = Integer.valueOf(scan.nextLine());
					}
			
				}
			}
		return p;
	}

}
