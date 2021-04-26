package GitTools.GitRepoAnalysis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBUtils {
	
	public static final String URL = "jdbc:sqlite:C:/Users/MiPro/GitDB.db";
	
    public static Connection connect() throws SQLException {
        Connection conn = null;
        //try {
            
            conn = DriverManager.getConnection(URL);
            
            //System.out.println("Connection to SQLite has been established.");
            
        //} catch (SQLException e) {
          //  System.out.println(e.getMessage());
        //}
        
        return conn;
    }
    
    //надо чтобы в списке папки лежали от корневой к более низким уровням
    public static void updateInfoAboutRepositoryWithoutCountingStats(String repo, List<String> people, List<String> folders, List<String> files) throws IOException
    {
    	if(repositoryExists(repo))
    	{
    		List<String> currentPeople = selectAllPeopleConnectedToRepo(repo);
    		
    		for(String file : selectAllFilesConnectedToRepo(repo))
    		{
    			if(!files.contains(file))
    			{
    				deleteFile(file, repo);
    				for (String person : currentPeople)
    				{
    					deletePersonKnowsInFile(person, file, repo);
    				}
    			}
    		}
    		
    		for(String folder : selectAllFoldersConnectedToRepo(repo))
    		{
    			if(!folders.contains(folder))
    			{
    				deleteFolder(folder, repo);
    				for (String person : currentPeople)
    				{
    					deletePersonKnowsInFolder(person, folder, repo);
    				}
    			}
    		}
    		
    		for(String person : currentPeople)
    		{
    			if(!people.contains(person))
    			{
    				deleteParticipation(person, repo);
    			}
    		}
    	}
    	else
    	{
    		insertRepository(repo);
    	}
    	
    	for (String person : people)
    	{
    		if (!personExists(person))
    		{
    			insertPerson(person);
    		}
    		if (!participationExists(person, repo))
    		{
    			insertParticipation(person, repo);
    		}
    	}
    	
    	for (String folder : folders)
    	{
    		if (!folderExists(folder, repo))
    		{
    			insertFolder(folder, folder.substring(0,folder.lastIndexOf("/")), repo);
    		}
    	}
    	
    	int length;
    	
    	for (String file : files)
    	{
    		length = Repo.countLinesNew(file);
    		if (!fileExists(file, repo))
    		{
    			insertFile(file, file.substring(0,file.lastIndexOf("/")),repo, length);
    		}
    		else
    		{
    			updateFileLength(file, repo, length);
    		}
    	}
    }
    
    public static List<String> selectAllPeopleConnectedToRepo(String repository)
    {
        String sql = "SELECT name FROM people WHERE EXISTS (SELECT * FROM participation where id_repository in (select id from repositories where link = ?))";
        List<String> people = new ArrayList<String>();
        
        try (Connection conn = connect();
    			PreparedStatement pstmt  = conn.prepareStatement(sql)){
           pstmt.setString(1,repository);
           ResultSet rs  = pstmt.executeQuery();
            
            while (rs.next()) {
            	people.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return people;
    }
    
    public static List<String> selectAllFilesConnectedToRepo(String repository)
    {
        String sql = "SELECT path FROM files WHERE  id_repository in (select id from repositories where link = ?)";
        List<String> files = new ArrayList<String>();
        
        try (Connection conn = connect();
    			PreparedStatement pstmt  = conn.prepareStatement(sql)){
           pstmt.setString(1,repository);
           ResultSet rs  = pstmt.executeQuery();
            
            while (rs.next()) {
            	files.add(rs.getString("path"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return files;
    }
    
    public static List<String> selectAllFoldersConnectedToRepo(String repository)
    {
        String sql = "SELECT path FROM folders WHERE  id_repository in (select id from repositories where link = ?)";
        List<String> folders = new ArrayList<String>();
        
        try (Connection conn = connect();
    			PreparedStatement pstmt  = conn.prepareStatement(sql)){
           pstmt.setString(1,repository);
           ResultSet rs  = pstmt.executeQuery();
            
            while (rs.next()) {
            	folders.add(rs.getString("path"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return folders;
    }
    public static void deletePerson(String name) {
    	
    	if (personExists(name))
    	{
    		String sql = "DELETE FROM people WHERE name = ?";

            try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
    	}
        
    }
    
    public static void deleteFolder(String path, String repository) {
    	
    	if (folderExists(path, repository))
    	{
    		String sql = "DELETE FROM folders f JOIN repositories r ON f.id_repository = r.id WHERE f.path = ? AND r.link = ?";

            try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, path);
                pstmt.setString(2, repository);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
    	}
        
    }
    
    public static void deleteFile(String path, String repository) {
    	
    	if (fileExists(path, repository))
    	{
    		String sql = "DELETE FROM files f JOIN repositories r ON f.id_repository = r.id WHERE f.path = ? AND r.link = ?";

            try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, path);
                pstmt.setString(2, repository);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
    	}
        
    }
    
    public static void deleteParticipation(String person, String repository) {
    	
    	if (participationExists(person, repository))
    	{
    		String sql = "DELETE FROM participation WHERE id_person in (select id from people where name = ?) and id_repository in (select id from repositories where link = ?)";

            try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, person);
                pstmt.setString(2, repository);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
    	}
        
    }

    public static void deletePersonKnowsInFolder(String person, String path, String repository) {
    	
    	if (participationExists(person, repository))
    	{
    		String sql = "DELETE FROM PersonKnowsInFolder WHERE id_person in (select id from people where name = ?) and id_folder in (select id from folders where path = ? and id_repository in (select id from repositories where link = ?))";

            try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, person);
                pstmt.setString(2, path);
                pstmt.setString(3, repository);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
    	}
        
    }
    
    public static void deletePersonKnowsInFile(String person, String path, String repository) {
    	
    	if (participationExists(person, repository))
    	{
    		String sql = "DELETE FROM PersonKnowsInFile WHERE id_person in (select id from people where name = ?) and id_file in (select id from files where path = ? and id_repository in (select id from repositories where link = ?))";

            try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, person);
                pstmt.setString(2, path);
                pstmt.setString(3, repository);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
    	}
        
    }
    
    public static void updateFolder(String path, String repo, int peopleKnows) {
    	
    	String sql = "UPDATE folders SET PEOPLE_KNOWING = ? WHERE path = ? and id_repository in (select id from repositories where link = ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, peopleKnows);
             pstmt.setString(2, path);
             pstmt.setString(3, repo);
             pstmt.executeUpdate();
        } catch (SQLException e) {
        	System.out.println(e.getMessage());
        }
    	     
    }
    
    public static void updateFile(String path, String repo, int peopleKnows) {
    	
    	String sql = "UPDATE files SET PEOPLE_KNOWING = ? WHERE path = ? and id_repository in (select id from repositories where link = ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, peopleKnows);
             pstmt.setString(2, path);
             pstmt.setString(3, repo);
             pstmt.executeUpdate();
        } catch (SQLException e) {
        	System.out.println(e.getMessage());
        }
    	     
    }
    
    public static void updatePercentForFolder(String path, String repo, String person, int percent) {
    	
    	String sql = "UPDATE PersonKnowsInFolder SET percent = ? WHERE id_person in (select id from people where name = ?) and id_folder in (select id from folders where path = ? and id_repository in (select id from repositories where link = ?))";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, percent);
             pstmt.setString(2, person);
             pstmt.setString(3, path);
             pstmt.setString(4, repo);
             pstmt.executeUpdate();
        } catch (SQLException e) {
        	System.out.println(e.getMessage());
        }
    	     
    }
    
    public static void updatePercentForFile(String path, String repo, String person, int percent) {
    	
    	String sql = "UPDATE PersonKnowsInFile SET percent = ? WHERE id_person in (select id from people where name = ?) and id_file in (select id from files where path = ? and id_repository in (select id from repositories where link = ?))";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, percent);
             pstmt.setString(2, person);
             pstmt.setString(3, path);
             pstmt.setString(4, repo);
             pstmt.executeUpdate();
        } catch (SQLException e) {
        	System.out.println(e.getMessage());
        }
    	     
    }
    
    public static void insertPerson(String name) {
    	
    	if (!personExists(name))
    	{
    		String sql = "INSERT INTO people(name) VALUES(?)";

            try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
    	}
        
    }
    
    
    public static void insertRepository(String link) {
    	
    	if (!repositoryExists(link))
    	{
    		String sql = "INSERT INTO repositories(link) VALUES(?)";

            try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, link);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
    	}
        
    }

    public static void insertParticipation(String name, String link) {
    	
    	String sql = "INSERT INTO participation(id_person, id_repository) VALUES(?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectPersonIdByName(name));
                pstmt.setInt(2, selectRepositoryIdByLink(link));
                pstmt.executeUpdate();
        } catch (SQLException e) {
             System.out.println(e.getMessage());
        }
    }

    public static void insertFolder(String path, String parent, String repo) {
    	
    	String sql = "INSERT INTO folders(path, id_repository, id_parent_folder) VALUES(?, ?, ?)";
    	
    	if (!folderExists(path, repo))
    	{
            try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
            		   pstmt.setString(1, path);
            		   pstmt.setInt(2, selectRepositoryIdByLink(repo));
                       pstmt.setInt(3, selectFolderIdByPathAndRepo(parent, repo));
                       pstmt.executeUpdate();
               } catch (SQLException e) {
                    System.out.println(e.getMessage());
               }
    	}
    	
    }
    
    public static void insertFile(String path, String folder, String repo, int length) {
    	
    	String sql = "INSERT INTO files(path, id_repository, id_folder, length) VALUES(?, ?, ?, ?)";
    	
    	if (!fileExists(path, repo))
    	{
            try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
            		   pstmt.setString(1, path);
            		   pstmt.setInt(2, selectRepositoryIdByLink(repo));
                       pstmt.setInt(3, selectFolderIdByPathAndRepo(folder, repo));
                       pstmt.setInt(4, length);
                       pstmt.executeUpdate();
               } catch (SQLException e) {
                    System.out.println(e.getMessage());
               }
    	}
    	
    }
    
    public static void updateFileLength(String path, String repo, int length) {
    	
    	String sql = "UPDATE files length = ? WHERE id_repository = ? and path = ?";
    	
    	if (!fileExists(path, repo))
    	{
            try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
            		   pstmt.setInt(1, length);
            		   pstmt.setInt(2, selectRepositoryIdByLink(repo));
                       pstmt.setString(3, path);
                       pstmt.executeUpdate();
               } catch (SQLException e) {
                    System.out.println(e.getMessage());
               }
    	}
    	
    }
    
    public static void insertPersonKnowsInFile(String person, String file, String repo, int percent)
    {
    	String sql = "INSERT INTO PersonKnowsInFile(id_person, id_file, percent) VALUES(?, ?, ?)";
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        		   pstmt.setInt(1, selectPersonIdByName(person));
        		   pstmt.setInt(2, selectFileIdByPathAndRepo(file, repo));
                   pstmt.setInt(3, percent);
                   pstmt.executeUpdate();
           } catch (SQLException e) {
                System.out.println(e.getMessage());
           }
    }
    
    public static int selectPersonKnowsInFile(String person, String file, String repo)
    {
    	int percent = -2;
    	String sql = "SELECT percent FROM PersonKnowsInFile WHERE id_person = ? and id_file = ?";
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        		   pstmt.setInt(1, selectPersonIdByName(person));
        		   pstmt.setInt(2, selectFileIdByPathAndRepo(file, repo));

                   ResultSet rs  = pstmt.executeQuery();
                    
                    while (rs.next()) {
                    	percent = rs.getInt("percent");
                    }
           } catch (SQLException e) {
                System.out.println(e.getMessage());
           }
        return percent;
    }
    
    public static void insertPersonKnowsInFolder(String person, String folder, String repo, int percent)
    {	
    	String sql = "INSERT INTO PersonKnowsInFolder(id_person, id_folder, percent) VALUES(?, ?, ?)";
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        		   pstmt.setInt(1, selectPersonIdByName(person));
        		   pstmt.setInt(2, selectFolderIdByPathAndRepo(folder, repo));
                   pstmt.setInt(3, percent);
                   pstmt.executeUpdate();
           } catch (SQLException e) {
                System.out.println(e.getMessage());
           }
    }

    public static boolean participationExists(String person, String repo)
    {
    	String sql = "SELECT count(*) cnt FROM people p join participation pp on p.id = pp.id_person join repositories r on r.id = pp.id_repository where p.name = ? and r.link = ?";
        int cnt = -1;
    	try (Connection conn = connect();
    			PreparedStatement pstmt  = conn.prepareStatement(sql)){
           pstmt.setString(1,person);
           pstmt.setString(2,repo);
           ResultSet rs  = pstmt.executeQuery();
            
            while (rs.next()) {
            	cnt = rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return cnt > 0;
    }
    
    public static boolean fileExists(String path, String repo)
    {
    	String sql = "SELECT count(*) cnt FROM files f join repositories r on r.id = f.id_repository where f.path = ? and r.link = ?";
        int cnt = -1;
    	try (Connection conn = connect();
    			PreparedStatement pstmt  = conn.prepareStatement(sql)){
           pstmt.setString(1,path);
           pstmt.setString(2,repo);
           ResultSet rs  = pstmt.executeQuery();
            
            while (rs.next()) {
            	cnt = rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return cnt > 0;
    }

    public static boolean folderExists(String path, String repo)
    {
    	String sql = "SELECT count(*) cnt FROM folders f join repositories r on r.id = f.id_repository where f.path = ? and r.link = ?";
        int cnt = -1;
        try (Connection conn = connect();
    			PreparedStatement pstmt  = conn.prepareStatement(sql)){
           pstmt.setString(1,path);
           pstmt.setString(2,repo);
           ResultSet rs  = pstmt.executeQuery();
    		
            
            while (rs.next()) {
            	cnt = rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return cnt > 0;
    }
    
    public static boolean repositoryExists(String repo)
    {
    	String sql = "SELECT count(*) cnt FROM repositories WHERE link = ?";
        int cnt = -1;
        try (Connection conn = connect();
    			PreparedStatement pstmt  = conn.prepareStatement(sql)){
           pstmt.setString(1,repo);
           ResultSet rs  = pstmt.executeQuery();
            
            while (rs.next()) {
            	cnt = rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return cnt > 0;
    }
    
    public static boolean personExists(String name)
    {
    	String sql = "SELECT count(*) cnt FROM people WHERE name = ?";
        int cnt = -1;
        try (Connection conn = connect();
    			PreparedStatement pstmt  = conn.prepareStatement(sql)){
           pstmt.setString(1,name);
           ResultSet rs  = pstmt.executeQuery();
            
            while (rs.next()) {
            	cnt = rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return cnt > 0;
    }
    
    /*public List<String> selectAllRepositories(){
        String sql = "SELECT link FROM repositories";
        List<String> repositories = new ArrayList<String>();
        
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            
            while (rs.next()) {
            	repositories.add(rs.getString("link"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return repositories;
    }
    
    public List<String> selectAllFolders(){
        String sql = "SELECT f.path ||', ' ||r.link as folder FROM folders f join repositories r on r.id = f.id_repository";
        List<String> folders = new ArrayList<String>();
        
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            
            while (rs.next()) {
            	folders.add(rs.getString("folder"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return folders;
    }

    public List<String> selectAllFiles(){
        String sql = "SELECT f.path ||', ' || fd.path ||', '||r.link as file FROM files f join folders fd on fd.id = f.id_folder join repositories r on r.id = f.id_repository";
        List<String> files = new ArrayList<String>();
        
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            
            while (rs.next()) {
            	files.add(rs.getString("file"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return files;
    }*/
    
    private static int selectFileIdByPathAndRepo(String path, String repo)
    {
        String sql = "SELECT f.id FROM files f join repositories r on f.id_repository = r.id WHERE r.link = ? and f.path = ?";
        int id = -1;
        try (Connection conn = connect();
                PreparedStatement pstmt  = conn.prepareStatement(sql)){
               
               // set the value
               pstmt.setString(1,repo);
               pstmt.setString(2,path);
               //
               ResultSet rs  = pstmt.executeQuery();
               
               // loop through the result set
               while (rs.next()) {
                   id = rs.getInt("id");
               }
           } catch (SQLException e) {
               System.out.println(e.getMessage());
           }
        
        //System.out.println("file_id = " + id);
        return id;
    }
    
    private static int selectFolderIdByPathAndRepo(String path, String repo)
    {
        String sql = "SELECT f.id FROM folders f join repositories r on f.id_repository = r.id WHERE r.link = ? and f.path = ?";
        int id = -1;
        try (Connection conn = connect();
                PreparedStatement pstmt  = conn.prepareStatement(sql)){
               
               // set the value
               pstmt.setString(1,repo);
               pstmt.setString(2,path);
               //
               ResultSet rs  = pstmt.executeQuery();
               
               // loop through the result set
               while (rs.next()) {
                   id = rs.getInt("id");
               }
           } catch (SQLException e) {
               System.out.println(e.getMessage());
           }
        
        return id;
    }
    
    private static int selectRepositoryIdByLink(String link){
        String sql = "SELECT id FROM repositories WHERE link = ?";
        int id = -1;
        try (Connection conn = connect();
                PreparedStatement pstmt  = conn.prepareStatement(sql)){
               
               // set the value
               pstmt.setString(1,link);
               //
               ResultSet rs  = pstmt.executeQuery();
               
               // loop through the result set
               while (rs.next()) {
                   id = rs.getInt("id");
               }
           } catch (SQLException e) {
               System.out.println(e.getMessage());
           }
        
        return id;
    }
    
    private static String selectRepositoryLinkById(int id){
        String sql = "SELECT link FROM repositories WHERE id = ?";
        String link = "";
        try (Connection conn = connect();
                PreparedStatement pstmt  = conn.prepareStatement(sql)){
               
               // set the value
               pstmt.setInt(1,id);
               //
               ResultSet rs  = pstmt.executeQuery();
               
               // loop through the result set
               while (rs.next()) {
                   link = rs.getString("id");
               }
           } catch (SQLException e) {
               System.out.println(e.getMessage());
           }
        
        return link;
    }
    
    private static int selectPersonIdByName(String name){
        String sql = "SELECT id FROM people WHERE name = ?";
        int id = -1;
        try (Connection conn = connect();
                PreparedStatement pstmt  = conn.prepareStatement(sql)){
               
               // set the value
               pstmt.setString(1,name);
               //
               ResultSet rs  = pstmt.executeQuery();
               
               // loop through the result set
               while (rs.next()) {
                   id = rs.getInt("id");
               }
           } catch (SQLException e) {
               System.out.println(e.getMessage());
           }
        
        //System.out.println("person_id = " + id);
        return id;
    }
    
}
