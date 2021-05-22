package GitTools.GitRepoAnalysis;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

@FacesValidator("repoValidator")
public class RepoValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        
    	String repo = (String)value;
    	Boolean valueIsInvalid = (repo).equals("") || !(repo.matches("https://github.com/(.*)/(.*).git"));

    	final LsRemoteCommand lsCmd = new LsRemoteCommand(null);
    	String callResult = "";
    	lsCmd.setRemote(repo);
        try {
        	callResult = lsCmd.call().toString();
		} catch (InvalidRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        if (valueIsInvalid || callResult.isEmpty()) {
            throw new ValidatorException(new FacesMessage("Введено пустое или неверное значение. Ссылка должна указывать на существующий репозиторий и иметь формат https://github.com/<имя владельца>/<название репозитория>.git "));
        }
    }

}