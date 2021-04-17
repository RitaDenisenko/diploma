package GitTools.GitRepoAnalysis;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.DepthWalk.Commit;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.NullOutputStream;

public class GitRepositoryAnalysisTools {

	static final long month = 2592000000L;
	static final int nearestLinesNumber = 10;
	static final int persentToKnow = 50;
	int monthNumber;
	
	public GitRepository gr = new GitRepository();	
	public List<String> fileNames = new ArrayList<String>();
	public List<String> peopleNames = new ArrayList<String>();
	
	public GitRepositoryAnalysisTools(int months)
	{
		monthNumber = months;
	}
	
	public void initListFileNames(final File folder) {	
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	        	initListFileNames(fileEntry);
	        } else {
	            fileNames.add(fileEntry.getAbsolutePath().replace("\\", "/"));
	        }
	    }
	}
	
	public static int countLinesNew(String filename) throws IOException {
        try (Stream<String> fileStream = Files.lines(Paths.get(filename))) {
        	return (int) fileStream.count();
        }
        catch (Exception e)
        {
        	//System.out.println("countLinesNew exception");
        	return 0;
        }
	}
	
	public void initListPeopleNames() throws RevisionSyntaxException, NoHeadException, MissingObjectException, IncorrectObjectTypeException, AmbiguousObjectException, GitAPIException, IOException
	{
		Git git = new Git(gr.jgitRepository);
		Repository repo = gr.jgitRepository;
		String treeName = "refs/heads/master";
		String person;
		for (RevCommit commit : git.log().add(repo.resolve(treeName)).call()) {
		    person = commit.getCommitterIdent().getName();
		    if (!peopleNames.contains(person) & !person.equals("GitHub"))
		    {
		    	peopleNames.add(person);
		    }
		}
	}
	
	//main
	public void calculatePeopleKnowingForAllLevels(File repo) throws Exception
	{
		//System.out.println("calculatePeopleKnowingForAllLevels");
		gr.init(repo.getAbsolutePath());
    	initListPeopleNames();
    	System.out.println("People knowing repository: " + calculatePeopleKnowingRepository(repo));
   
    	calculatePeopleKnowingForFoldersAndFiles(repo);
	}

	public void calculatePeopleKnowingForFoldersAndFiles(File repo) throws Exception
	{
		//System.out.println("calculatePeopleKnowingForFoldersAndFiles");
		for (final File fileEntry : repo.listFiles()) {
	        if (fileEntry.isDirectory()) {
	        	System.out.println("People knowing folder: " + fileEntry.getAbsolutePath() + ": "+ calculatePeopleKnowingFolder(fileEntry.getAbsolutePath().replace("\\", "/"), repo));
	        	calculatePeopleKnowingForFoldersAndFiles(fileEntry);
	        } else {
	        	System.out.println("People knowing file: " + fileEntry.getAbsolutePath() + ": "+ calculatePeopleKnowingFile(fileEntry.getAbsolutePath().replace("\\", "/"), repo));
	        }
	    }
	}
	
	public int calculatePeopleKnowingRepository(File repo) throws Exception
	{
		//System.out.println("calculatePeopleKnowingRepository");
		int count = 0;
    	//gr.init(repo.getAbsolutePath());
    	//initListPeopleNames();
    	
    	for(String person : peopleNames) {
    		if (calculateLinesPercentInFolderKnownBy(repo.getAbsolutePath().replace("\\", "/"), repo, person) >= persentToKnow)
    		{
    			count++;
    		}
    	}
    	
    	
    	return count;
	}
	
	public int calculatePeopleKnowingFolder(String folderName, File repo) throws Exception
	{
		//System.out.println("calculatePeopleKnowingFolder");
		int count = 0;
    	//gr.init(repo.getAbsolutePath());
    	//initListPeopleNames();
    	
    	for(String person : peopleNames) {
    		if (calculateLinesPercentInFolderKnownBy(folderName, repo, person) >= persentToKnow)
    		{
    			count++;
    		}
    	}
    	
    	
    	return count;
	}

	public int calculatePeopleKnowingFile(String fileName, File repo) throws Exception
	{
		//System.out.println("calculatePeopleKnowingFile");
		int count = 0;
    	//gr.init(repo.getAbsolutePath());
    	//initListPeopleNames();
		File file = new File(fileName);
    	
    	for(String person : peopleNames) {
    		int length = countLinesNew(file.getAbsolutePath().replace("\\", "/"));
    		System.out.println("length = " + length);
    		int calc = calculateLinesPercentInFileKnownBy(fileName, repo, length, person);
    		System.out.println("fileName = " +  fileName + " person = "+ person + " calc = " + calc);
    		if (calculateLinesPercentInFileKnownBy(fileName, repo, length, person) >= persentToKnow)
    		{
    			count++;
    		}
    	}
    	
    	
    	return count;
	}
	
	public int calculateLinesPercentInFolderKnownBy(String folderName, File repo, String person) throws Exception
	{
		//System.out.println("calculateLinesPercentInFolderKnownBy");
		File folder = new File(folderName);
		int count = 0, sum = 0, calc;
		
		for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	        	sum = sum + calculateLinesPercentInFolderKnownBy(fileEntry.getAbsolutePath().replace("\\", "/"), repo, person);
	        	count++;
	        } 
	        else {
	        	int length = countLinesNew(fileEntry.getAbsolutePath().replace("\\", "/"));
	        	calc = calculateLinesPercentInFileKnownBy(fileEntry.getAbsolutePath().replace("\\", "/"), repo, length, person);
	        	if (calc != -1)
	        	{
	        		sum = sum + calc;
	        		count++;
	        	}
	        	
	        }
	    }
		
		//System.out.println("In " +folderName + " " + person + " knows " + ((count==0) ? 0 : sum / count) + "%");
		return (count==0) ? 0 : sum / count;
	}
	
	public int calculateLinesPercentInFileKnownBy(String fileName, File repo, int length, String person) throws Exception
	{
		//System.out.println("calculateLinesPercentInFileKnownBy file: " + fileName + ", person: " + person);
    	BlameResult br = gr.getBlameResultForFile(fileName.replace(repo.getAbsolutePath().replace("\\", "/") + "/", ""));
    	int countKnownLines = 0;
     	int i = 0;
     	Date d = new Date(System.currentTimeMillis() - month * monthNumber);
     	RevCommit commit, parentCommit;
     	boolean stop;
     	
     	try {
     		while (length > i)
        	{
        		//System.out.println("while, i =" + i + ", length=" + length );
     			commit = br.getSourceCommit(i);
     			int lineNum = i;
     			stop = false;
     			//System.out.println("before while");
             	while (!stop)
             	{
             		if (commit.getParentCount() > 0)
             		{
             			//System.out.println("commit.getParentCount() > 0");
             			parentCommit = commit.getParent(0);
             			if ( person.equals(commit.getCommitterIdent().getName()) & diffBetweenCommitsContainsThisLineOrNearestLines(commit, parentCommit,repo, fileName, lineNum))
             			{
             				//System.out.println("if1");
             				if (d.before(commit.getCommitterIdent().getWhen()))
             				{
             					countKnownLines++;
             					//System.out.println("1countKnownLines = "+countKnownLines);
             				}
             				stop = true;
             			}
             			
             			commit = parentCommit;
             			lineNum = lineNumberInPreviousVersion(commit, parentCommit, repo, fileName, lineNum);
             			//System.out.println("lineNum = "+lineNum);
             			if (lineNum == -1)//строка только появилась в этом коммите
             			{
             				//System.out.println("lineNum == -1");
             				stop = true;	
             			}
             						
             		}
             		else
             		{
             			//System.out.println("no parent");
             			if (person.equals(commit.getCommitterIdent().getName()) & diffForInitCommitContainsThisLineOrNearestLines(commit, repo, fileName, lineNum))
             			{
             				System.out.println("if2");
             				if (d.before(commit.getCommitterIdent().getWhen()))
             				{
             					countKnownLines++;
             					//System.out.println("2countKnownLines = "+countKnownLines);
             				}
             			}
             			stop = true; // выходим, для этой строки дальше не смотрим
             		}
             	}
				//System.out.println("after while");
		
        		i++;
        	}
     	}catch(Exception e)
     	{
     		//System.out.println("exception");
     		if (i == 0)
     			length = 0;
     	}
    	
     	System.out.println("In " + fileName + " " + person + " knows " + ((length == 0) ? -1 : countKnownLines*100 / length) + "%");
    	return (length == 0) ? -1 : countKnownLines*100 / length; // пустые и автосгенерированные файлы при подсчёте не учитываем
	}
	
	
	public int calculateKnownLinesPercentInFile(String fileName, File repo, int length, int peopleNumber) throws Exception
	{
    	BlameResult br = gr.getBlameResultForFile(fileName.replace(repo.getAbsolutePath().replace("\\", "/") + "/", ""));
    	int countKnownLines = 0;
     	int i = 0, j = 1;
     	Date d = new Date(System.currentTimeMillis() - month * monthNumber);
     	RevCommit commit, parentCommit;
     	
     	try {
     		while (length > i)
        	{
     			List<PersonIdent> people = new LinkedList<PersonIdent>();
     			commit = br.getSourceCommit(i);
     			int lineNum = i;
     				if (peopleNumber != 1)
     				{
             			while (peopleNumber > j)
             			{
             				if (commit.getParentCount() > 0)
             				{
             					parentCommit = commit.getParent(0);
             						if ( !people.contains(commit.getAuthorIdent()) & diffBetweenCommitsContainsThisLineOrNearestLines(commit, parentCommit,repo, fileName, lineNum) & d.before(commit.getAuthorIdent().getWhen()))
             						{
             							people.add(commit.getAuthorIdent());
             							if (peopleNumber == j + 1) // мы на последней итерации
             							{
             								countKnownLines++;
             							}
             							
             							j++;
             						}
             						commit = parentCommit;
             						lineNum = lineNumberInPreviousVersion(commit, parentCommit, repo, fileName, lineNum);
             						if (lineNum == -1)//строка только появилась в этом коммите
             						{
             							j = peopleNumber;	
             						}
             						
             				}
             				else
             				{
             					if (j == peopleNumber - 1)
             					{
             						if (!people.contains(commit.getAuthorIdent()) & diffForInitCommitContainsThisLineOrNearestLines(commit, repo, fileName, lineNum) & d.before(commit.getAuthorIdent().getWhen()))
             						{
             							countKnownLines++;
             						}
             					}
             					j = peopleNumber; // выходим, для этой строки дальше не смотрим
             				}
             			//}
     				}

     			}else
     			{
         			int k = -nearestLinesNumber;
         			while(k <= lineNum + nearestLinesNumber)
         			{
         				if(k > 0 & k <= length)
         				{
         					if (d.before(br.getSourceCommitter(k).getWhen()))
             				{
             					countKnownLines++;
             					k = lineNum + nearestLinesNumber + 1;
             				}   
         				}
         				k++;  					
         			}
     			}
		
        		i++;
        	}
     	}catch(Exception e)
     	{
     		System.out.println("exception");
     		if (i == 0)
     			length = 0;
     	}
    	
    	return (length == 0) ? -1 : countKnownLines*100 / length; // пустые и автосгенерированные файлы при подсчёте не учитываем
	}
	
	public int calculateTotalKnownLinesPercent(File repo, int peopleNumber) throws Exception
	{
		int calc;
		int calcTotal = 0;
		int linesTotal = 0;
		int linesNumber = 0;
    	gr.init(repo.getAbsolutePath());
    	initListFileNames(repo);
    	for(String fileName : fileNames)
    	{
    			linesNumber = countLinesNew(fileName);
    			calc = calculateKnownLinesPercentInFile(fileName, repo, linesNumber, peopleNumber);
    			if (calc != -1)
    			{
        			System.out.println("Percent of known lines in file " + fileName + ": " + calc);
        			
        			calcTotal = calcTotal + calc * linesNumber / 100;
        			linesTotal = linesTotal + linesNumber;
        			//System.out.println("Known: "+calcTotal);
        			//System.out.println("Total: "+linesTotal);
    			}
    	}
    	
    	return (linesTotal == 0)? 100 :calcTotal * 100 / linesTotal;
	}
	
	private Boolean diffForInitCommitContainsThisLineOrNearestLines(RevCommit commit, File repo, String filePath, int lineNumber) throws IncorrectObjectTypeException, IOException
	{
		Git git = new Git(gr.jgitRepository);
		
		File tempFile = new File("temp.txt");
		tempFile.createNewFile(); // if file already exists will do nothing 
		FileOutputStream oFile = new FileOutputStream(tempFile, true); 
	    DiffFormatter df = new DiffFormatter(oFile);
		df.setRepository( git.getRepository() );
		AbstractTreeIterator oldTreeIter = new EmptyTreeIterator();
		ObjectReader reader = gr.jgitRepository.newObjectReader();
		AbstractTreeIterator newTreeIter = new CanonicalTreeParser(null, reader, commit.getTree());
		for (DiffEntry entry : df.scan(oldTreeIter, newTreeIter)) {
        	df.format(df.toFileHeader(entry));
        }
        
        String content = readAllBytesJava7(tempFile.getAbsolutePath());
        
        tempFile.delete();
        
        int fileNamePosition = content.indexOf(filePath.replace(repo.getAbsolutePath().replace("\\", "/") + "/", ""));
        int atSymbolsPosition1, atSymbolsPosition2, nextFileDiffPosition;
        String substringWithLineNumbers;
        
        //System.out.println(filePath.replace(repo.getAbsolutePath().replace("\\", "/") + "/", ""));
        //System.out.println(fileNamePosition);
        //System.out.println(content);
        if (fileNamePosition!=-1)
        {
        	//System.out.println("(fileNamePosition!=-1)");
        	nextFileDiffPosition = content.indexOf("diff --git");
        	atSymbolsPosition1 = content.indexOf("@@", fileNamePosition);
        	atSymbolsPosition2 = content.indexOf("@@", atSymbolsPosition1+2);
        	substringWithLineNumbers = content.substring(atSymbolsPosition1+2, atSymbolsPosition2-1);
        	
        	if(containsThisOrNearestLine(substringWithLineNumbers, lineNumber))
        		return true;
        	
        	Boolean ok = true;
        	while(ok)
        	{
        		atSymbolsPosition1 = content.indexOf("@@", atSymbolsPosition2+2);
        		if (atSymbolsPosition1 < nextFileDiffPosition & atSymbolsPosition1 != -1)
        		{
        			atSymbolsPosition2 = content.indexOf("@@", atSymbolsPosition1+2);
                	substringWithLineNumbers = content.substring(atSymbolsPosition1+2, atSymbolsPosition2-1);
                	if(containsThisOrNearestLine(substringWithLineNumbers, lineNumber))
                		return true;
                	
        		}else
        			return false;            	
        	}
        	
        	
        }
        
		return false;
	}
	
	//ok
	private Boolean diffBetweenCommitsContainsThisLineOrNearestLines(RevCommit commit, RevCommit parentCommit, File repo, String filePath, int lineNumber) throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException
	{
		Git git = new Git(gr.jgitRepository);
		
		File tempFile = new File("temp.txt");
		tempFile.createNewFile(); // if file already exists will do nothing 
		FileOutputStream oFile = new FileOutputStream(tempFile, true); 
	    DiffFormatter df = new DiffFormatter(oFile);
		df.setRepository( git.getRepository() );
        for (DiffEntry entry : df.scan(parentCommit, commit)) {
        	df.format(df.toFileHeader(entry));
        }
        
        String content = readAllBytesJava7(tempFile.getAbsolutePath());
        
        tempFile.delete();
        
        int fileNamePosition = content.indexOf(filePath.replace(repo.getAbsolutePath().replace("\\", "/") + "/", ""));
        int atSymbolsPosition1, atSymbolsPosition2, nextFileDiffPosition;
        String substringWithLineNumbers;
        
        //System.out.println(filePath.replace(repo.getAbsolutePath().replace("\\", "/") + "/", ""));
        //System.out.println(fileNamePosition);
        //System.out.println(content);
        if (fileNamePosition!=-1)
        {
        	//System.out.println("(fileNamePosition!=-1)");
        	nextFileDiffPosition = content.indexOf("diff --git");
        	atSymbolsPosition1 = content.indexOf("@@", fileNamePosition);
        	atSymbolsPosition2 = content.indexOf("@@", atSymbolsPosition1+2);
        	substringWithLineNumbers = content.substring(atSymbolsPosition1+2, atSymbolsPosition2-1);
        	
        	if(containsThisOrNearestLine(substringWithLineNumbers, lineNumber))
        		return true;
        	
        	Boolean ok = true;
        	while(ok)
        	{
        		atSymbolsPosition1 = content.indexOf("@@", atSymbolsPosition2+2);
        		if (atSymbolsPosition1 < nextFileDiffPosition & atSymbolsPosition1 != -1)
        		{
        			atSymbolsPosition2 = content.indexOf("@@", atSymbolsPosition1+2);
                	substringWithLineNumbers = content.substring(atSymbolsPosition1+2, atSymbolsPosition2-1);
                	if(containsThisOrNearestLine(substringWithLineNumbers, lineNumber))
                		return true;
                	
        		}else
        			return false;            	
        	}
        	
        	
        }
        
		return false;
	}
	
	private static Boolean containsThisOrNearestLine(String substringToParse, int lineNumber)
	{
		int plusPosition = substringToParse.indexOf("+");
		int minusPosition = substringToParse.indexOf("-");
		int commaPosition1 = substringToParse.indexOf(",");
		int commaPosition2 = substringToParse.lastIndexOf(",");
		//int blankPosition = substringToParse.indexOf(" ", commaPosition1);
		
		int firstSubstractedLine = Integer.parseInt(substringToParse.substring(minusPosition+1, commaPosition1));  
		//int lastSubstractedLine = firstSubstractedLine - 1 + Integer.parseInt(substringToParse.substring(commaPosition1+1, blankPosition));  
		
		int firstAddedLine = Integer.parseInt(substringToParse.substring(plusPosition+1, commaPosition2));  
		int lastAddedLine;
		if (firstAddedLine != 0)
			lastAddedLine = firstAddedLine - 1 + Integer.parseInt(substringToParse.substring(commaPosition2+1, substringToParse.length()).trim());
		else
			lastAddedLine = 0;
		//if(!(lineNumber >= firstAddedLine & lineNumber <= lastAddedLine))
		
		return ( ((lineNumber >= firstAddedLine - nearestLinesNumber | firstAddedLine == 0) 
				 & lineNumber <= lastAddedLine + nearestLinesNumber) 
				|((lineNumber >= firstSubstractedLine - nearestLinesNumber | firstSubstractedLine == 0) 
				 & lineNumber <= firstSubstractedLine + nearestLinesNumber));
		
	}
	
	private int lineNumberInPreviousVersion(RevCommit commit, RevCommit parentCommit, File repo, String filePath, int lineNumber) throws IOException
	{
		Git git = new Git(gr.jgitRepository);
		
		File tempFile = new File("temp.txt");
		tempFile.createNewFile(); // if file already exists will do nothing 
		FileOutputStream oFile = new FileOutputStream(tempFile, true); 
	    DiffFormatter df = new DiffFormatter(oFile);
		df.setRepository( git.getRepository() );
        for (DiffEntry entry : df.scan(parentCommit, commit)) {
        	df.format(df.toFileHeader(entry));
        }
        
        String content = readAllBytesJava7(tempFile.getAbsolutePath());
        
        tempFile.delete();
        
        int fileNamePosition = content.indexOf(filePath.replace(repo.getAbsolutePath().replace("\\", "/") + "/", ""));
        int atSymbolsPosition1, atSymbolsPosition2, nextFileDiffPosition;
        String substringWithLineNumbers;
        
        //System.out.println(filePath.replace(repo.getAbsolutePath().replace("\\", "/") + "/", ""));
        //System.out.println(fileNamePosition);
        //System.out.println(content);
        if (fileNamePosition!=-1)
        {
        	List<String> stringsForParse = new LinkedList<String>();
        	nextFileDiffPosition = content.indexOf("diff --git");
        	atSymbolsPosition1 = content.indexOf("@@", fileNamePosition);
        	atSymbolsPosition2 = content.indexOf("@@", atSymbolsPosition1+2);
        	//substringWithLineNumbers = content.substring(atSymbolsPosition1+2, atSymbolsPosition2-1);
        	stringsForParse.add(content.substring(atSymbolsPosition1+2, atSymbolsPosition2-1));
        	//if(containsThisOrNearestLine(substringWithLineNumbers, lineNumber))
        	//	return true;
        	
        	Boolean ok = true;
        	while(ok)
        	{
        		atSymbolsPosition1 = content.indexOf("@@", atSymbolsPosition2+2);
        		if (atSymbolsPosition1 < nextFileDiffPosition & atSymbolsPosition1 != -1)
        		{
        			atSymbolsPosition2 = content.indexOf("@@", atSymbolsPosition1+2);
                	substringWithLineNumbers = content.substring(atSymbolsPosition1+2, atSymbolsPosition2-1);
                	//if(containsThisOrNearestLine(substringWithLineNumbers, lineNumber))
                	//	return true;
                	stringsForParse.add(content.substring(atSymbolsPosition1+2, atSymbolsPosition2-1));
        		}else
        			ok = false;            	
        	}
        	
        	return generateLineNum(stringsForParse, lineNumber);
        	
        }
        
		return lineNumber; //в коммите нет этого файла, значит в нём строка не двигалась
	}
	
	private static int generateLineNum(List<String> substringsToParse, int lineNumber)
	{
		int addToLineNumber = 0;
		for(String substringToParse : substringsToParse)
		{
			int plusPosition = substringToParse.indexOf("+");
			int minusPosition = substringToParse.indexOf("-");
			int commaPosition1 = substringToParse.indexOf(",");
			int commaPosition2 = substringToParse.lastIndexOf(",");
			int blankPosition = substringToParse.indexOf(" ", commaPosition1);
			
			int firstSubstractedLine = Integer.parseInt(substringToParse.substring(minusPosition+1, commaPosition1));  
			//int lastSubstractedLine = firstSubstractedLine - 1 + Integer.parseInt(substringToParse.substring(commaPosition1+1, blankPosition));  
			
			int firstAddedLine = Integer.parseInt(substringToParse.substring(plusPosition+1, commaPosition2));  
			int lastAddedLine;
			if (firstAddedLine != 0)
				lastAddedLine = firstAddedLine - 1 + Integer.parseInt(substringToParse.substring(commaPosition2+1, substringToParse.length()).trim());
			else
				lastAddedLine = 0;
			
			if(!(lineNumber >= firstAddedLine & lineNumber <= lastAddedLine)) //строку добавили не в текущем коммите
			{
				if (lineNumber < firstAddedLine & lineNumber < firstSubstractedLine)//если строка лежит до изменений, её номер сохраняется
				{
					//addToLineNumber = addToLineNumber;
				}
				if(lineNumber > firstAddedLine & lineNumber > lastAddedLine)//если строка лежит после изменений, её номер сдвигается на добавленное количество строк
				{
					addToLineNumber = addToLineNumber + Integer.parseInt(substringToParse.substring(commaPosition2+1, substringToParse.length()).trim()) - Integer.parseInt(substringToParse.substring(commaPosition1+1, blankPosition));
				}
			}
			
			
			return -1;
		}
		
		return lineNumber + addToLineNumber;
		
	}
	
    private static String readAllBytesJava7(String filePath) 
    {
        String content = "";
 
        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
 
        return content;
    }
	
	public void DifferenceBetweenThisCommitAndTheNext(RevCommit commit) throws Exception
	{
	    RevCommit diffWith = commit.getParent(0);
	    FileOutputStream stdout = new FileOutputStream(FileDescriptor.out);
	    DiffFormatter diffFormatter = new DiffFormatter(stdout);
	    try {
	        diffFormatter.setRepository(gr.jgitRepository);
	        for (DiffEntry entry : diffFormatter.scan(diffWith, commit)) {
	        	//diffFormatter.format(diffFormatter.setDiffComparator(null);
	        	diffFormatter.format(diffFormatter.toFileHeader(entry));
	        }
	    }
	    finally
	    {
	    	
	    }
	}

	public void PrintDifferenceBetweenTwoCommits(RevCommit newCommit, RevCommit oldCommit, Git git) throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException
	{
		ObjectReader reader = git.getRepository().newObjectReader();
		CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
		ObjectId oldTree = git.getRepository().resolve( "HEAD^{tree}" ); // equals newCommit.getTree()
		oldTreeIter.reset( reader, oldTree );
		CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
		ObjectId newTree = git.getRepository().resolve( "HEAD~1^{tree}" ); // equals oldCommit.getTree()
		newTreeIter.reset( reader, newTree );

		DiffFormatter df = new DiffFormatter( new ByteArrayOutputStream() ); // use NullOutputStream.INSTANCE if you don't need the diff output
		df.setRepository( git.getRepository() );
		List<DiffEntry> entries = df.scan( oldTreeIter, newTreeIter );

		for( DiffEntry entry : entries ) {
		  System.out.println( entry );
		}

	}
	

	
}
