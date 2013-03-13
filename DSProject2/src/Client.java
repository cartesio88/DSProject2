import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.regex.Pattern;

import DSProject.ServerInterface;

public class Client {

	private static InetAddress clientIp = null;
	private static final String IPv4_REGEX = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	private static Pattern IPv4_PATTERN = Pattern.compile(IPv4_REGEX);

	public static void main(String[] args) throws InterruptedException,
			RemoteException, NotBoundException {
		System.out.println("Starting the Client");
		System.setProperty("java.net.preferIPv4Stack", "true");

		String serverIp = null;
		Integer serverPort = null;
		boolean done = false;

		if (args.length != 2) {
			System.out
					.println("Usage: java -jar Client.jar [server ip] [server port]");
			return;
		}

		getClientIP();

		Scanner scan = new Scanner(System.in);

		serverIp = checkIp(args[0], scan);
		System.out.println("Server IP: " + serverIp);
		serverPort = portCheck(args[1], scan);
		System.out.println("Port: " + serverPort);

		Registry registry = LocateRegistry.getRegistry(serverIp, serverPort);
		String serverName = serverIp + ":" + serverPort;

		ServerInterface server = null;
		server = (ServerInterface) registry.lookup(serverName);

		while (!done) {

			try {

				System.out.println("\nChoose the option: \n" + "1) Post\n"
						+ "2) Read\n" + "3) Choose\n" + "4) Reply\n"
						+ "0) Exit\n");

				String Choice = scan.nextLine();
				Integer Option = Integer.valueOf(Choice);

				switch (Option) {
				case 1: { // Post
					System.out.println("Enter title:");
					String title = scan.nextLine();

					System.out.println("Enter content:");
					String content = scan.nextLine();

					if (server.Post(title, content)) {
						System.out.println("Posted successfully!");
					} else {
						System.out.println("Failed to post... ;(");
					}
					break;
				}
				case 2: { // Read
					String list = server.Read();
					System.out.println(" == Articles ==");
					System.out.println(list);
					System.out.println(" ==============");
					break;
				}
				case 3: { // Choose
					System.out.println("Enter ID:");
					String strId = scan.nextLine();
					int id = Integer.valueOf(strId);
					String article = server.Choose(id);
					if(article.equals("")) System.out.println("Article not found!");
					else System.out.println(article);
					break;
				}
				case 4: { // Reply
					System.out.println("Enter ID of article to reply:");
					String strId = scan.nextLine();
					int id = Integer.valueOf(strId);

					System.out.println("Enter content of the response:");
					String content = scan.nextLine();

					if(server.Reply(id, content)){
						System.out.println("Response sent successfuly!");
					}else{
						System.out.println("Error sending the response");
					}
						
					break;
				}
				case 0: // Exit
					System.out.println("Bye!");
					System.exit(0);
					break;

				}
			} catch (NumberFormatException e) {
			}

		}
	}

	private static void getClientIP() {
		try {
			Enumeration<NetworkInterface> nets = NetworkInterface
					.getNetworkInterfaces();

			while (nets.hasMoreElements()) {
				NetworkInterface ni = nets.nextElement();
				if (!ni.isLoopback() && ni.isUp()) {
					clientIp = ni.getInetAddresses().nextElement();
					break;
				}
			}

		} catch (SocketException e) {
			System.out.println("ERROR getting the interfaces of the device");
			e.printStackTrace();
		}
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

	private static String checkIp(String IP, Scanner scan) {
		while (!IPv4_PATTERN.matcher(IP).matches()) {

			System.out.println("IP has wrong format try again: ");
			IP = scan.nextLine();
		}
		return IP;
	}
}
