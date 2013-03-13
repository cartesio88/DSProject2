import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Scanner;

import DSProject.ServerRMI;

public class Server {

	public static void main(String[] args){
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.rmi.server.codebase", "file:./bin");
		System.setProperty("java.security.policy", "file:./policyfile");

		if (args.length != 4 && args.length != 2) {
			System.out
					.println("Usage: java -jar Server.jar server [port] [coordinator ip] [coordinator port]# For regular server");
			System.out
					.println("       java -jar Server.jar coordinator [port] # For coordinator");
			
			return;
		}
		Scanner scan = new Scanner(System.in);

		int coordinatorPort = 0;
		InetAddress coordinatorIp = null;
		int serverPort = portCheck(args[1], scan);
		InetAddress serverIp = getServerIP();
		boolean isCoordinator = false;
		if (args[0].equalsIgnoreCase("server")) {
			System.out.println("Configuring as a regular server.");
			coordinatorPort = portCheck(args[3], scan);
			try {
				coordinatorIp = InetAddress.getByName(args[2]);
			} catch (UnknownHostException e) {
				System.out.println("Unknown coordinator ip: "+coordinatorIp);
				e.printStackTrace();
				return;
			}
			isCoordinator = false;
		} else if (args[0].equalsIgnoreCase("server")) {
			System.out.println("Configuring as the coordinator.");
			isCoordinator = true;
		} else {
			System.out.println("[ERROR] Invalid role: " + args[0]);
			return;
		}

		try {
			@SuppressWarnings("unused")
			ServerRMI server = new ServerRMI(serverIp, serverPort, isCoordinator,
					coordinatorIp, coordinatorPort);
		} catch (RemoteException e) {
			System.out.println("Error creating the RMI object server");
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("Error trying to bind with the coordinator");
			e.printStackTrace();
		}
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
			System.setProperty("java.rmi.server.hostname",
					serverIp.getHostAddress());

		} catch (SocketException e) {
			System.out.println("ERROR getting the interfaces of the device");
			e.printStackTrace();
		}
		return serverIp;
	}

	private static int portCheck(String s, Scanner scan) {

		int p = 0;
		boolean badRange = true;
		boolean notInt = true;

		if (s.length() == 0)
			p = 1099;
		else {
			while (badRange || notInt) {

				try {
					Integer.valueOf(s);
					notInt = false;
					p = Integer.valueOf(s);
				} catch (NumberFormatException e) {
					notInt = true;
					System.out.println("Port has wrong format try again: ");
					s = scan.nextLine();
				}

				badRange = p < 0 || p > 55901;
				if (badRange) {
					System.out.println("Port is out of range try again: ");
					p = Integer.valueOf(scan.nextLine());
				}

			}
		}
		return p;
	}

}
