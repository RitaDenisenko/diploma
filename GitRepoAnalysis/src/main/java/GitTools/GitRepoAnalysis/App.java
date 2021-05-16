package GitTools.GitRepoAnalysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.eclipse.jgit.api.Git;

public class App 
{		
	//public static final String FOLDER_FOR_SAVING = "C:/Users/MiPro";
	
	public static void clearJSFile() throws IOException {
        FileWriter fwOb = new FileWriter("C:\\Users\\MiPro\\eclipse-workspace_git\\GitRepoAnalysis\\WebContent\\resources\\js\\test.js", false); 
        PrintWriter pwOb = new PrintWriter(fwOb, false);
        pwOb.flush();
        pwOb.close();
        fwOb.close();
    }
	
    public static void main( String[] args ) throws Exception
    {	
        /*Scanner in = new Scanner(System.in);
        System.out.print("Введите ссылку на гит-репозиторий: ");
        String repoLink = in.next();
        System.out.print("Введите число месяцев для подсчёта давности: ");
        int monthNumber = in.nextInt();
        //System.out.print("Введите число людей для проверки: ");
        //int peopleNumber = in.nextInt();
        
        in.close();
    	File directory = new File(FOLDER_FOR_SAVING+repoLink.substring(repoLink.lastIndexOf("https://github.com")+18).replace(".git", ""));
    	String path;
    	
    	if (!directory.exists())
    	{
        	Git git = Git.cloneRepository()
      			  .setURI( repoLink ) //https://github.com/sidgrouse/LetsDoStuff.git   https://github.com/catchorg/Catch2.git
      			  .setDirectory(directory)
      			  .call();
      	
        	System.out.println(git.getRepository().getDirectory().getAbsolutePath().replace("\\", "/").replace("/.git", ""));
        	path = git.getRepository().getDirectory().getAbsolutePath().replace("\\", "/").replace("/.git", "");    		
    	}
    	else
    	{
    		path = directory.getAbsolutePath();
    	}
    	
    	Repo g = new Repo();
    	File repository = new File(path);
    	
    	g.calculateAll(repository, repoLink);
    	
    	*/
    	
    	//Repo g = new Repo();
    	//g.calculateAll();
    	
    	//clearJSFile();
    	
    	/*Repo g = new Repo();
    	g.link = "https://github.com/sidgrouse/LetsDoStuff.git";
    	g.calculateAll();
    	System.out.println(g.getDataForTreemap());
    	*/
    }
}
