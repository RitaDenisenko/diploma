package GitTools.GitRepoAnalysis;

import java.io.File;
import java.util.Scanner;

import org.eclipse.jgit.api.Git;

public class App 
{		
    public static void main( String[] args ) throws Exception
    {	
        Scanner in = new Scanner(System.in);
        System.out.print("Введите ссылку на гит-репозиторий: ");
        String repoLink = in.next();
        System.out.print("Введите число месяцев для подсчёта давности: ");
        int monthNumber = in.nextInt();
        
        in.close();
    	File directory = new File("C:/Users/MiPro"+repoLink.substring(repoLink.lastIndexOf("https://github.com")+18).replace(".git", ""));
    	String path;
    	
    	if (!directory.exists())
    	{
        	Git git = Git.cloneRepository()
      			  .setURI( repoLink ) //https://github.com/sidgrouse/LetsDoStuff.git
      			  .setDirectory(directory)
      			  .call();
      	
        	System.out.println(git.getRepository().getDirectory().getAbsolutePath().replace("\\", "/").replace("/.git", ""));
        	path = git.getRepository().getDirectory().getAbsolutePath().replace("\\", "/").replace("/.git", "");    		
    	}
    	else
    	{
    		path = directory.getAbsolutePath();
    	}
    	
    	GitRepositoryAnalysisTools g = new GitRepositoryAnalysisTools(monthNumber);
    	File repository = new File(path);
    	System.out.println("Total percent of known lines: " + g.calculateTotalUnknownLinesPercent(repository));
    	
    }
}
