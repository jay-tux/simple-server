import java.io.*;
import java.time.format.*;
import java.time.*;

public final class Logger {
	public static Logger log;
	private FileOutputStream logWriter;
	private FileOutputStream userAgentWriter;
	private FileOutputStream visitorWriter;
	
	static {
		log = new Logger();
		try
		{
			String logs = System.getProperty("user.home") + "/.jserv/";
			File logOut = new File(logs + "logFile");
			File logDir = new File(logs);
			if(!logDir.exists())
			{
				if(!logDir.mkdir())
				{
					throw new ServerException("Failed to create non-existent log directory.");
				}
			}
			
			if(!logOut.exists())
			{
				if(!logOut.createNewFile())
				{
					throw new ServerException("Failed to create non-existent log file.");
				}
			}
			log.logWriter = new FileOutputStream(logOut);
			log.log("Message", "LOGMOD", "Logger initialized.");
			
			File logAgent = new File(logs + "userAgents");
			if(!logAgent.exists())
			{
				if(!logAgent.createNewFile())
				{
					throw new ServerException("Failed to create non-existent log file.");					
				}
			}
			log.userAgentWriter = new FileOutputStream(logAgent);
			log.log("Message", "LOGMOD", "UA-Logger initalized.");
			
			File logVisit = new File(logs + "visitors");
			if(!logVisit.exists())
			{
				if(!logVisit.createNewFile())
				{
					throw new ServerException("Failed to create non-existent log file.");
				}
			}
			log.visitorWriter = new FileOutputStream(logVisit);
			log.log("Message", "LOGMOD", "IP-Logger initialized.");
		}
		catch(ServerException se)
		{
			throw se;
		}
		catch(Exception e)
		{
			throw new ServerException("Logger initialization failed.");
		}
	}
	
	public void logVisitor(String IP)
	{
		try
		{
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter form = DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm:ss");
			String total = "Visitor:\t" + IP + "\t@" + form.format(now) + "\n";
			byte[] output = total.getBytes();
			visitorWriter.write(output);
			System.err.print(total);
		}
		catch(Exception e)
		{
			log.log("Error", "VISITORS", "Failed to log IP " + IP + ".");
		}
	}
	
	public void logAgent(String userAgent)
	{
		try
		{
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter form = DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm:ss");
			String total = "User-Agent:\t" + userAgent + "\t@" + form.format(now) + "\n";
			byte[] output = total.getBytes();
			userAgentWriter.write(output);
			System.err.print(total);
		}
		catch(Exception e)
		{
			log.log("Error", "UAGENTS", "Failed to log User-Agent " + userAgent + ".");
		}
	}
	
	public void log(String category, String module, String message)
	{
		try
		{
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter form = DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm:ss");
			String total = category + "\tfrom\tJSERV>" + module + "@" + form.format(now) + "\t" + message + "\n";
			byte[] output = total.getBytes();
			logWriter.write(output);
			//System.err.print(total);
		}
		catch(Exception e)
		{
			throw new ServerException("Failed to log " + category + " from " + module + ": " + message);
		}
	}
	
	public String getLogPath()
	{
		return System.getProperty("user.home") + "/.jserv/logFile";
	}
	
	public void stop()
	{
		try
		{
			log("Message", "LOGMOD", "Shutting down logger...");
			logWriter.close();
		}
		catch(Exception e)
		{
			throw new ServerException("Failed to finalize Logger.");
		}
	}
	
	private Logger() {}
}
