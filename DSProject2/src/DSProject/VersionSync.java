package DSProject;

public class VersionSync extends Thread {

    public void run() {
    while(true){
        //System.out.println("Version Synchronization!");
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
    }
}