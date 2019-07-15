package housing;

/**
 * Wrapper for enabling python support via Py4j for HousingModel
 *
 * This is the class that will be launched by start-java-server.sh
 */

import py4j.GatewayServer;
import housing.Model;

public class HeadlessModel {

  public static void main(String[] args) {
    Model housingModel = new Model("src/main/resources/config.properties", "/tmp");
    // app is now the gateway.entry_point
    GatewayServer server = new GatewayServer(housingModel);
    server.start();
    System.out.println("This is the Java side. Started the JVM server.");
  }
}
