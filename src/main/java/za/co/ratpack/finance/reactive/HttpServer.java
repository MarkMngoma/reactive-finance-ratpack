package za.co.ratpack.finance.reactive;

/**
 * @author markmngoma
 * @created at 19:01 on 22/12/2024
 */
public class HttpServer {

  public static final String SERVER_ENVIRONMENT = "RATPACK_ENVIRONMENT";

  public static void main(String[] args) throws Exception {
    ServerCommand serverCommand = new ServerCommand();
    serverCommand.run();
  }
}
