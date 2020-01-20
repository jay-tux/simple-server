import java.io.*;

public class Program {
	private String hostname;
	private int port;
	private String serverhome;
	
	public Program(String name, String port, String home)
	{
		this.hostname = name;
		try
		{
			this.port = Integer.parseInt(port);
			if(this.port < 100 || this.port > 9999)
			{
				throw new Exception();
			}
		}
		catch(Exception e)
		{
			throw new ServerException("Port no. should be an integer between 100 and 9999.");
		}
		
		try
		{
			File f = new File(home);
			if(!f.exists() || !f.isDirectory())
			{
				throw new Exception();
			}
			serverhome = home;
		}
		catch(Exception e)
		{
			throw new ServerException("Server home directory should be an existing directory.");
		}
		
	}
	
	//program [--port port] [--name name] [--home home]
	//program [--config configfile]					NOT YET
	public static void main(String[] args)
	{
		try
		{
			String name = "", port = "", home = "";
			boolean nameSet = false, portSet = false, homeSet = false;
			int index = 0;
			while(index < args.length)
			{
				switch(args[index])
				{
					case "--name":
						nameSet = true;
						if(index + 1 < args.length)
						{
							index++;
							name = args[index];
						}
						else
						{
							throw new ServerException("Option --name requires an argument.");
						}
						break;
						
					case "--port":
						portSet = true;
						if(index + 1 < args.length)
						{
							index++;
							port = args[index];
						}
						else
						{
							throw new ServerException("Option --port requires an argument.");
						}
						break;
						
					case "--home":
						homeSet = true;
						if(index + 1 < args.length)
						{
							index++;
							home = args[index];
						}
						else
						{
							throw new ServerException("Option --home requires an argument.");
						}
						break;
						
					//case "--config":
						//break;
						
					default:
							throw new ServerException("Unknown option. Use --name, --port or --home.");
				}
				index++;
			}
			
			if(!portSet)
			{
				portSet = true;
				port = "4589";
			}
			if(!homeSet)
			{
				homeSet = true;
				home = System.getProperty("user.dir");
			}
			if(!nameSet)
			{
				nameSet = true;
				name = getHostname();
			}
			
			Program p = new Program(name, port, home);
			p.startServer();
		}
		catch(ServerException e)
		{
			System.err.println("Something went wrong while running the server:");
			System.err.println(e.getMessage());
		}
		catch(Exception e)
		{
			System.err.println("An unanticipated exception occured: " + e.getClass().getName());
			System.err.println(e.getMessage());
		}
	}
	
	private static String getHostname()
	{
		try
		{
			Runtime rt = Runtime.getRuntime();
			String[] commands = { "hostname" };
			Process proc = rt.exec(commands);
			
			BufferedReader stdInput = new BufferedReader(new 
				InputStreamReader(proc.getInputStream()));

			return stdInput.readLine();
		}
		catch(IOException e)
		{
			return "Local";
		}
	}
	
	protected void startServer()
	{
		Logger.log.log("Message", "OVERSEER", "Getting file table...");
		FileTable.mainTable.setHomeDir(serverhome);
		Logger.log.log("Message", "OVERSEER", "File table initialised...");
		Logger.log.log("Message", "OVERSEER", "Getting status code severities...");
		Response.getSeverities(serverhome);
		Logger.log.log("Message", "OVERSEER", "Status severities initialised...");
		Logger.log.log("Message", "OVERSEER", "Starting server....");
		Listener server = new Listener(port);
		Logger.log.log("Message", "OVERSEER", "Server initialised...");
		switch(server.run())
		{
			case 0:
				Logger.log.log("Message", "OVERSEER", "Server shut down correctly...");
				break;
				
			case 1:
				Logger.log.log("Warning", "OVERSEER", "Server crashed. Cleanup went okay.");
				break;
				
			case 2:
				Logger.log.log("Warning", "OVERSEER", "Server crashed. Resource-cleanup failed. This might affect future runs.");
				break;
		
		}
		Logger.log.log("Message", "OVERSEER", "Shutting other services down...");
		Logger.log.stop();
	}
}
