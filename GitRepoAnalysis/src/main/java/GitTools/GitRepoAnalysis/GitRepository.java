package GitTools.GitRepoAnalysis;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

public class GitRepository {

    Repository jgitRepository;

    public void init(String gitDir) throws Exception {

        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        try {
            jgitRepository = builder
                    .findGitDir(new File(gitDir))
                    .readEnvironment()
                    .build();
        } catch (IOException ioe) {
            throw new Exception("Can't build the git repository");
        }
    }


    public BlameResult getBlameResultForFile(String filePath) throws Exception {

        try {
            BlameCommand blamer = new BlameCommand(jgitRepository);
            ObjectId commitID = jgitRepository.resolve("HEAD");
            blamer.setStartCommit(commitID);
            blamer.setFilePath(filePath);
            BlameResult blame = blamer.call();

            return blame;
        } catch (GitAPIException | IOException e) {
            String msg = MessageFormat.format("Can't get blame result for file: {0}", filePath);
            throw new Exception(msg, e);
        }
    }

}
