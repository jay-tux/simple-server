import java.net.*;
import java.io.*;

public class Listener {
	private ServerSocket serverSocket;
	private PrintWriter out;
	private BufferedReader in;
	private int port;
	
	public Listener(int port)
	{
		this.port = port;
	}
	
	public int run()
	{
		serverSocket = null;
		Socket clientSocket = null;
		out = null;
		in = null;
		try
		{
		    serverSocket = new ServerSocket(port);
		    Logger.log.log("Message", "LISTENER", "Ready to receive requests...");
		    while((clientSocket = serverSocket.accept()) != null)
		    {
		    	Logger.log.log("Message", "LISTENER", "Request accepted.");
			    out = new PrintWriter(clientSocket.getOutputStream(), true);
			    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			    String inputLine;
	            
			    // Initiate conversation with client
			    String data = "";
			    while ((inputLine = in.readLine()) != null && !inputLine.equals("")) {
			        data += inputLine + "\n";
			    }
			    System.out.println("====== REQUEST  DATA ======");
			    System.out.println(data);
			    System.out.println("======  REQUEST END  ======");
			    Request req = Request.fromString(data);
			    
			    Logger.log.log("Message", "LISTENER", "Logging visitor and user-agent...");
			    Logger.log.logAgent(req.getUA());
			    Logger.log.logVisitor(clientSocket.getRemoteSocketAddress().toString());
			    Logger.log.log("Message", "LISTENER", "Done, okay.");
			    
			    Response resp = Response.requestToResponse(req);
			    out.println(resp.getResponse());
			    System.out.println("====== RESPONSE DATA ======");
			    System.out.println(resp.getResponse());
			    System.out.println("====== RESPONSE  END ======");
			    in.close();
			    out.close();
			    clientSocket.close();
		    }
		    Logger.log.log("Message", "LISTENER", "Shutdown message received.");
		    in.close();
		    out.close();
		    serverSocket.close();
		    Logger.log.log("Message", "LISTENER", "Server services stopped.");
		    return 0;
		}
		catch(Exception e)
		{
			Logger.log.log("Error", "LISTENER", "Error " + e.getClass().getName() + ": " + e.getMessage());
			try
			{
				if(in != null)
				{
					in.close();
				}
				
				if(out != null)
				{
					out.close();
				}
				
				if(clientSocket != null)
				{
					clientSocket.close();
				}
				
				if(serverSocket != null)
				{
					serverSocket.close();
				}
			}
			catch(Exception ex)
			{
				Logger.log.log("Error", "LISTENER", "Crash cleanup failed.");
				return 2;
			}
			return 1;
		}

	}
}