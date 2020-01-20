import java.util.*;
import java.io.*;
import java.nio.file.*;

public class FileTable {
	
	private String tableFile;
	private String homedir;
	private Map<String, String> translations;
	public static FileTable mainTable;
	
	static {
		mainTable = new FileTable();
		mainTable.translations = new HashMap<>();
	}

	protected FileTable() {}

	public void setHomeDir(String homeDir)
	{
		File home = new File(homeDir);
		if(home.exists() && home.isDirectory() && home.canRead())
		{
			homedir = homeDir;
			Logger.log.log("Message", "FILETABLE", "Received new home directory. Getting translation table...");
			File table = new File(home.getPath() + "/.filetable");
			if(table.exists() && table.isFile())
			{
				try
				{
					tableFile = table.getAbsolutePath();
					for(String line : Files.readAllLines(table.toPath()))
					{
						if(!line.contains("="))
						{
							Logger.log.log("Warning", "FILETABLE", "Formatting error in filetable: lines should be <http path>=<file path>.");
						}
						else
						{
							if(line.split("=").length != 2)
							{
								Logger.log.log("Warning", "FILETABLE", "Neither HTTP-paths of filepaths should contain =.");
							}
							else
							{
								translations.put(line.split("=")[0], line.split("=")[1]);
							}
						}
					}
				}
				catch(IOException e)
				{
					Logger.log.log("Error", "FILETABLE", "Failed to read file table. Fallback used: no file table set.");
					
				}
			}
			else
			{
				Logger.log.log("Error", "FILETABLE", "No file table found. Fallback used: no file table set.");
				tableFile = "n/a";
			}
		}
		else
		{
			Logger.log.log("Error", "FILETABLE", "Given directory is non-existent, not a directory or unreadable");
			throw new ServerException("Server home directory issues. See log at " + Logger.log.getLogPath());
		}
	}
	
	public String getFilePath(Request input)
	{
		String reqPage = input.getPage();
		if(translations.containsKey(reqPage))
		{
			File trial = new File(homedir + "/" + translations.get(reqPage));
			if(trial.exists() &&  trial.isFile() && trial.canRead())
			{
				Logger.log.log("Message", "FILETABLE", "Filetable returned source file: " + homedir + translations.get(reqPage) + ".");
				return homedir + translations.get(reqPage);
			}
			else
			{
				input.addSuggestedStatus(404);
				Logger.log.log("Error", "FILETABLE", "Found an entry matching page, but linked file does not exist.");
				return "";
			}
		}
		else
		{
			File trial = new File(homedir + reqPage);
			if(trial.exists() && trial.canRead())
			{
				Logger.log.log("Message", "FILETABLE", "Filetable returned non-linked source file: " + homedir + reqPage + ".");
				return homedir + reqPage;
			}
			else
			{
				input.addSuggestedStatus(404);
				Logger.log.log("Error", "FILETABLE", "Requested page is neither in the filetable, nor an existing file.");
				return "";
			}
		}
	}
}
