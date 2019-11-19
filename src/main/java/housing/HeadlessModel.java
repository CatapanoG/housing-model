package housing;

/**
 * Wrapper for enabling python support via Py4j for HousingModel
 *
 * This is the class that will be launched by start-java-server.sh
 */

import py4j.GatewayServer;
import housing.Model;
// +++
import java.net.InetAddress;

public class HeadlessModel {
	
	// +++
	private static GatewayServer server;

	public static void main(String[] args) {
		
		int port = Integer.parseInt(args[0]);
		
		Model housingModel = new Model("src/main/resources/config.properties", "/tmp", 100);
			
		server = new GatewayServer.GatewayServerBuilder(housingModel)
							.javaPort(port) // CHANGE
							.build();
			
		server.start();
		System.out.println("This is the Java side. Started the JVM server. Port num: " + port + "."); //CHANGE
	}
}

/*
package py4j;

import java.net.InetAddress;

public class TestApplication {

	public static void main(String[] args) {
		try {
			GatewayServer server1 = new GatewayServer.GatewayServerBuilder()
					.javaPort(10001)
					.javaAddress(InetAddress.getByName("127.0.0.1"))
					.callbackClient(10002, InetAddress.getByName("127.0.0.1"))
					.build();
			GatewayServer server2 = new GatewayServer.GatewayServerBuilder()
					.javaPort(10003)
					.javaAddress(InetAddress.getByName("127.0.0.1"))
					.callbackClient(10004, InetAddress.getByName("127.0.0.1"))
					.build();
			GatewayServer server3 = new GatewayServer.GatewayServerBuilder()
					.javaPort(10005)
					.javaAddress(InetAddress.getByName("127.0.0.1"))
					.callbackClient(10006, InetAddress.getByName("127.0.0.1"))
					.build();
			server1.start();
			server2.start();
			server3.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
*/