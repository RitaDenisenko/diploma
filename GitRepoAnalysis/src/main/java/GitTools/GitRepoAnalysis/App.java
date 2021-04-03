package GitTools.GitRepoAnalysis;

import java.io.File;
import java.util.Scanner;

import org.eclipse.jgit.api.Git;

public class App 
{		
	public static final String FOLDER_FOR_SAVING = "C:/Users/MiPro";
    public static void main( String[] args ) throws Exception
    {	
        Scanner in = new Scanner(System.in);
        System.out.print("Введите ссылку на гит-репозиторий: ");
        String repoLink = in.next();
        System.out.print("Введите число месяцев для подсчёта давности: ");
        int monthNumber = in.nextInt();
        System.out.print("Введите число людей для проверки: ");
        int peopleNumber = in.nextInt();
        
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
    	
    	GitRepositoryAnalysisTools g = new GitRepositoryAnalysisTools(monthNumber);
    	File repository = new File(path);
    	System.out.println("Total percent of known lines: " + g.calculateTotalUnknownLinesPercent(repository, peopleNumber));
    	
    	//g.calculateLinesInFileKnownByMoreThen("C:/Users/MiPro/LetsDoStuff/LetsDoStuff.Domain/LdsContext.cs",20);
    	
    }
}
