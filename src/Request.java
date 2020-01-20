import java.util.*;

public class Request {
	
	private String verb;
	private String page;
	private String version;
	private String host;
	private String useragent;
	private String[] accepts;
	private String[] encodings;
	private String conntype;
	private String cache;
	private List<Integer> suggestedStatus;
	private boolean passedChecks;
	protected static Request defaultRequest;
	protected static List<String> validVerbs;
	
	static {
		defaultRequest = new Request();
		defaultRequest.verb = "GET";
		defaultRequest.page = "/";
		defaultRequest.version = "1.1";
		defaultRequest.host = "n/a";
		defaultRequest.useragent = "_default";
		defaultRequest.accepts = new String[] { "text/html" };
		defaultRequest.encodings = new String[] { "n/a" };
		defaultRequest.conntype = "close";
		defaultRequest.cache = "max-age=0";
		defaultRequest.suggestedStatus.add(200);
		
		validVerbs = new ArrayList<String>();
		validVerbs.addAll(Arrays.asList(new String[] { "GET", "PUT", "POST", "DELETE", "HEAD" }));
	}

	protected Request() 
	{ 
		verb = "";
		page = "";
		version = "";
		host = "";
		useragent = "";
		accepts = new String[] { };
		encodings = new String[] { };
		conntype = "";
		cache = "";
		suggestedStatus = new ArrayList<Integer>();
	}
	  
	public String getPage()
	{
		return page;
	}
	
	public int mostSuggestedStatus()
	{
		int savedStatus = 0;
		int savedSeverity = Integer.MIN_VALUE;
		for(int status : suggestedStatus)
		{
			if(Response.severity(status) > savedSeverity)
			{
				savedSeverity = Response.severity(status);
				savedStatus = status;
			}
		}
		Logger.log.log("Message", "STATUSCODE", "Most suggested status code for request: " + savedStatus);
		return savedStatus;
	}
	
	public void addSuggestedStatus(int newStatus)
	{
		if(!suggestedStatus.contains(newStatus))
		{
			suggestedStatus.add(newStatus);
		}
	}

	public static Request fromString(String req)
	{
		Request r = new Request();
		try
		{
			req = req.replace("\r", "");
			String headerline = req.split("\n")[0];
			req = req.substring(req.indexOf('\n') + 1);
			r.verb = headerline.split(" ")[0];
			r.page = headerline.split(" ")[1];
			r.version = headerline.split(" ")[2].split("/")[1];
			for(String line : req.split("\n")) 
			{
				String property = line.split(": ", 2)[0];
				String content = line.split(": ", 2)[1];
				switch(line.split(": ", 2)[0])
				{
					case "Host":
						r.host = content;
						break;
						
					case "User-Agent":
						r.useragent = content;
						break;
						
					case "Accept":
						r.accepts = content.split(",[ ]*");
						break;
						
					case "Accept-Encoding":
						r.encodings = content.split(",[ ]*");
						break;
						
					case "Connection":
						r.conntype = content;
						break;
						
					case "Cache-Control":
						r.cache = content;
						break;
						
					case "Accept-Language": break;
					
					default:
						Logger.log.log("Warning", "REQUEST", "Encountered unknown property while parsing: " + property + ".");
						break;
				}
			}
			Logger.log.log("Message", "REQUEST", "Request parsed entirely.");
			Logger.log.log("Message", "REQUEST", "Checking request integrity...");
			r.passedChecks = r.isCorrect();
			if(r.passedChecks)
			{
				Logger.log.log("Message", "REQUEST", "Request integrity OK.");
				r.suggestedStatus.add(200);
			}
			else
			{
				Logger.log.log("Warning", "REQUEST", "Request integrity compromised. Fallback values have been used.");
			}
			
			Logger.log.log("Message", "REQUEST", "Request checking finished.");
			if(r.suggestedStatus.size() == 0) { r.suggestedStatus.add(200); }
			Logger.log.log("Message", "REQUEST", "Suggested Status codes: " + r.suggestedStatus.toString());
		}
		catch(Exception e)
		{
			Logger.log.log("Error", "REQUEST", "Failed to parse request.");
			r.suggestedStatus.add(400);
		}		
		return r;
	}
	
	public String getUA()
	{
		return useragent;
	}
	
	public boolean isSuggested(int status)
	{
		return suggestedStatus.contains(status);
	}
	
	public boolean validRequest()
	{
		return passedChecks;
	}
	
	public boolean isCorrect()
	{
		boolean correct = true;
		if(verb.equals(""))
		{
			correct = false;
			Logger.log.log("Warning", "VERIFIER_RQ", "Request verb empty. Setting verb to GET...");
			suggestedStatus.add(405);
			verb = "GET";
		}
		else if(!validVerbs.contains(verb))
		{
			correct = false;
			Logger.log.log("Warning", "VERIFIER_RQ", "Invalid request verb. Setting verb to GET...");
			suggestedStatus.add(405);
			verb = "GET";
		}
		
		if(page.equals(""))
		{
			correct = false;
			Logger.log.log("Warning", "VERIFIER_RQ", "Requested page empty. Setting page to /...");
			suggestedStatus.add(404);
			page = "/";
		}
		else if(!page.startsWith("/"))
		{
			correct = false;
			Logger.log.log("Warning", "VERIFIER_RQ", "Requested page is not formatted correctly... Trying to fix...");
			suggestedStatus.add(404);
			page = "/" + page;
		}
		
		if(version.equals(""))
		{
			correct = false;
			Logger.log.log("Warning", "VERIFIER_RQ", "Version is not set. Changing to HTTP/1.1...");
			suggestedStatus.add(505);
			version = "1.1";
		}
		else
		{
			try
			{
				float f = Float.parseFloat(version);
				if(f < 0) throw new Exception();
			}
			catch(Exception e)
			{
				correct = false;
				Logger.log.log("Warning", "VERIFIER_RQ", "Version should be a non-negative float. Setting to HTTP/1.1...");
				suggestedStatus.add(505);
				version = "1.1";
			}
		}
		
		if(host.equals(""))
		{
			correct = false;
			Logger.log.log("Warning", "VERIFIER_RQ", "Host is not set. Changing to default n/a...");
			suggestedStatus.add(503);
			host = "n/a";
		}
		
		if(useragent.equals(""))
		{
			correct = false;
			Logger.log.log("Warning", "VERIFIER_RQ", "User-Agent is not set. Changing to default _default...");
			suggestedStatus.add(401);
			useragent = "_default";
		}
		
		if(accepts.length == 0)
		{
			correct = false;
			Logger.log.log("Warning", "VERIFIER_RQ", "Accept types are not set. Changing to default { \"text/html\" }...");
			suggestedStatus.add(415);
			accepts = new String[] { "text/html" };
		}
		
		if(encodings.length == 0)
		{
			correct = false;
			Logger.log.log("Warning", "VERIFIER_RQ", "Accepted Encodings are not set. Changing to default { \"n/a\" }...");
			suggestedStatus.add(406);
			encodings = new String[] { "n/a" };
		}
		
		if(conntype.equals(""))
		{
			correct = false;
			Logger.log.log("Warning", "VERIFIER_RQ", "Connection type is not set. Changing to default close...");
			conntype = "close";
		}
		
		if(cache.equals(""))
		{
			correct = false;
			Logger.log.log("Warning", "VERIFIER_RQ", "Cache is not set. Changing to default max-age=0...");
			cache = "max-age=0";
		}
		
		return correct;
	}
}