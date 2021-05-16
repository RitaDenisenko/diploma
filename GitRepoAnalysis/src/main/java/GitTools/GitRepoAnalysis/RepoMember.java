package GitTools.GitRepoAnalysis;

public class RepoMember {

	public String path;
	public int peopleKnows;
	public boolean isFolder;
	
	public RepoMember(String newPath, int newPeopleNum, boolean newIsFolder)
	{
		path = newPath;
		peopleKnows = newPeopleNum;
		isFolder = newIsFolder;
	}
}
