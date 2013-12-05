import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetWorkIF {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
				System.out.println("DisplayName:" + ni.getDisplayName());
				System.out.println("Name:" + ni.getName());
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					System.out.println("IP:" + ips.nextElement().getHostAddress());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
//		Enumeration<NetworkInterface> netInterfaces = null;
//		try {
//			netInterfaces = NetworkInterface.getNetworkInterfaces();
//			while (netInterfaces.hasMoreElements()) {
//				NetworkInterface ni = netInterfaces.nextElement();
//				System.out.println("DisplayName:" + ni.getDisplayName());
//				System.out.println("Name:" + ni.getName());
//				Enumeration<InetAddress> ips = ni.getInetAddresses();
//				while (ips.hasMoreElements()) {
//					System.out.println("IP:" + ips.nextElement().getHostAddress());
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

}
