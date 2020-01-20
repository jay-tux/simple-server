import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.nio.file.*;

public class Response {

	public final float VERSION = 1.1f;
	public final String SERVER = "jSERV@0.1";
	private int status;
	private String content_type;
	private String content;
	private static Map<Integer, String> statuscodes;
	private static Map<Integer, Integer> statusSeverities;
	private static Response defaultResponse;
	private boolean contentFromFile;
	
	static {
		statuscodes = new HashMap<Integer, String>();
		statusSeverities = new HashMap<Integer, Integer>();
		statuscodes.put(100, "Continue");
		statuscodes.put(101, "Switching Protocol");
		statuscodes.put(103, "Early Hints");
		
		statuscodes.put(200, "OK");
		statuscodes.put(201, "Created");
		statuscodes.put(202, "Accepted");
		statuscodes.put(203, "Non-Authoritative Information");
		statuscodes.put(204, "No Content");
		statuscodes.put(205, "Reset Content");
		statuscodes.put(206, "Partial Content");
		
		statuscodes.put(300, "Multiple Choice");
		statuscodes.put(301, "Moved Permanently");
		statuscodes.put(302, "Found");
		statuscodes.put(303, "See Other");
		statuscodes.put(304, "Not Modified");
		statuscodes.put(305, "Use Proxy");
		statuscodes.put(307, "Temporary Redirect");
		statuscodes.put(308, "Permanent Redirect");
		
		statuscodes.put(400, "Bad Request");
		statuscodes.put(401, "Unauthorized");
		statuscodes.put(402, "Payment Required");
		statuscodes.put(403, "Forbidden");
		statuscodes.put(404, "Not Found");
		statuscodes.put(405, "Method Not Allowed");
		statuscodes.put(406, "Not Acceptable");
		statuscodes.put(407, "Proxy Authentication Required");
		statuscodes.put(408, "Request Timeout");
		statuscodes.put(409, "Conflict");
		statuscodes.put(410, "Gone");
		statuscodes.put(411, "Length Required");
		statuscodes.put(412, "Precondition Failed");
		statuscodes.put(413, "Payload Too Large");
		statuscodes.put(414, "URI Too Long");
		statuscodes.put(415, "Unsupported Media Type");
		statuscodes.put(418, "I'm A Teapot");
		
		statuscodes.put(500, "Internal Server Error");
		statuscodes.put(501, "Not Implemented");
		statuscodes.put(502, "Bad Gateway");
		statuscodes.put(503, "Service Unavailable");
		statuscodes.put(504, "Gateway Timeout");
		statuscodes.put(505, "HTTP Version Not Supported");
		
		defaultResponse = new Response();
		defaultResponse.content = "<html><head></head><body><h4>No content generated for this page.</h4></body></html>";
		defaultResponse.content_type = "text/html";
		defaultResponse.status = 204;
		
	}
	
	protected Response() { }
	
	public void changeStatus(int newStatus)
	{
		if(statuscodes.keySet().contains(newStatus))
		{
			status = newStatus;
		}
	}
	
	public static int severity(int status)
	{
		if(statusSeverities.keySet().contains(status))
		{
			return statusSeverities.get(status);
		}
		else
		{
			Logger.log.log("Warning", "STATUSCODE", "Severity of unused status code requested.");
			return Integer.MIN_VALUE;
		}
	}
	
	private String getContent(String filepath)
	{
		Logger.log.log("Message", "RESPONSE", "Getting content from source file: " + filepath + "...");
		File f = new File(filepath);
		if(f.exists() && f.canRead())
		{
			try
			{
				contentFromFile = true;
				return Files.readString(f.toPath());
			}
			catch(IOException e)
			{
				Logger.log.log("Error", "RESPONSE", "Failed to construct response: can't read source file.");
				status = 500;
				return "<html><head></head><body><h4>500 - Internal Server Error.</h4></body></html>";
			}
		}
		else
		{
			status = 404;
			return "<html><head></head><body><h4>404 - File Not Found.</h4></body></html>";
		}
	}
	
	private String getContentType(String filepath)
	{
		if(contentFromFile)
		{
			String extension = filepath.split("\\.")[filepath.split("\\.").length - 1];
			switch(extension)
			{
				case "js":
				case "jq":
					return "text/javascript";
					
				case "css":
					return "text/css";
					
				case "jpg":
				case "jpeg":
				case "png":
				case "gif":
				case "ico":
					return "image/" + extension;
					
				case "mp3":
				case "wav":
				case "ogg":
					return "audio/" + extension;
					
				case "mp4":
					return "video/" + extension;
			
				case "htm":
				case "html":
				case "txt":
				default:
					return "text/html";
			}
		}
		else
		{
			return "text/html";
		}
	}
	
	public static Response requestToResponse(Request in)
	{
		Response out = new Response();
		out.contentFromFile = false;
		String filepath = FileTable.mainTable.getFilePath(in);
		Logger.log.log("Message", "RESPONSE", "Trying to get data from source file: " + filepath + "...");
		if(filepath.equals(""))
		{
			//404
			out.status = 404;
			out.content = "<html><head></head><body><h4>404 - Page doesn't exist.</h4></body></html>";
			out.content_type = "text/html";
		}
		else
		{
			out.status = in.mostSuggestedStatus();
			out.content = out.getContent(filepath);
			out.content_type = out.getContentType(filepath);
		}
		return out;
	}

	public String getResponse()
	{
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter form = DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm:ss");
		return "HTTP/" + VERSION + " " + status + " " + statuscodes.get(status) + "\nDate: " + form.format(now) +
				"\nServer: " + SERVER + "\nContent-Length: " + content.length() + "\nContent-Type: " + content_type + "\n\n" + content;
	}
	
	public static void getSeverities(String configPath)
	{
		File configDir = new File(configPath);
		if(configDir.exists() && configDir.canRead())
		{
			File configFile = new File(configPath + "/sev.conf");
			if(configFile.exists() && configFile.canRead())
			{
				try
				{
					for(String line : Files.readAllLines(configFile.toPath()))
					{
						if(!line.contains("="))
						{
							Logger.log.log("Error", "STATUSCODE", "Status severity setting should be formatted as <http status>=<severity>");
						}
						else
						{
							try
							{
								int code = Integer.parseInt(line.split("=")[0]);
								int sevr = Integer.parseInt(line.split("=")[1]);
								if(statuscodes.keySet().contains(code))
								{
									statusSeverities.put(code, sevr);
								}
								else
								{
									Logger.log.log("Warning", "STATUSCODE", "Status severity set for unused status code.");
								}
							}
							catch(Exception e)
							{
								Logger.log.log("Error", "STATUSCODE", "Both status code and severity should be integers.");
							}
						}
					}
					
					for(int status : statuscodes.keySet())
					{
						if(!statusSeverities.containsKey(status))
						{
							Logger.log.log("Warning", "STATUSCODE", "Status severity not set for status code " + status + "; setting to 0.");
							statusSeverities.put(status, 0);
						}
					}
				}
				catch(IOException e)
				{
					Logger.log.log("Error", "STATUSCODE", "Failed to get status severities due to I/O error.");
					for(int code : statuscodes.keySet())
					{
						if(code == 404)
						{
							statusSeverities.put(code, 1);
						}
						else
						{
							statusSeverities.put(code, 0);
						}
					}
				}
			}
		}
		else
		{
			Logger.log.log("Error", "STATUSCODE", "Failed to get status severities. Using 0 for all, except 404.");
			for(int code : statuscodes.keySet())
			{
				if(code == 404)
				{
					statusSeverities.put(code, 1);
				}
				else
				{
					statusSeverities.put(code, 0);
				}
			}
		}
	}
}
