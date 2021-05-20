package GitTools.GitRepoAnalysis;

public class PersonKnowsIn {

	private String person;
	private String path;
	private int percent;
	
	public String getPerson() {
        return person;
    }

	public String getPath() {
        return path;
    }

    public int getPercent(){
        return percent;
    }

    public void setPerson(String newPerson) {
        person = newPerson;
    }

    public void setPath(String newPath) {
        path = newPath;
    }
	
	public PersonKnowsIn(String newPerson, String newPath, int newPercent)
	{
		person = newPerson;
		path = newPath;
		percent = newPercent;
	}
}
