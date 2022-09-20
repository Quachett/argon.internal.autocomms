package za.co.ominsure.synapse.content.scribe.backend.autocomms;

import java.util.Base64;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;

import za.co.ominsure.synapse.content.scribe.backend.autocomms.dao.AutoCommsDashboardDao;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsAudits;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsResult;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookup;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookupTIA;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.SearchTempateIDs;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UserPermissions;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UserPermissionsResults;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UsersPermissions;
import uk.co.inc.argon.commons.exceptions.HttpException;

@ApplicationScoped
public class AutoCommsDashboardFacadeBean implements AutoCommsDashboardFacade {
	@Inject
	private AutoCommsDashboardDao dao;
    private static final ObjectMapper oMapper = new ObjectMapper();

	@Override
	public RecipientsLookup getTemplateRecipientLookup(SearchTempateIDs templateIDs) throws HttpException {
		return dao.getTemplateRecipientLookupInfo(templateIDs);
	}

	@Override
	public AutoCommsResult addTemplateRecipientLookup(RecipientsLookup recipients, String user) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutoCommsResult updateTemplateRecipientLookup(RecipientsLookup recipients, String user)
			throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutoCommsResult deleteTemplateRecipientLookup(String templateID, String user) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecipientsLookupTIA getTemplateRecipientLookupTIA() throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutoCommsResult updateTIARecipientLookup(RecipientsLookupTIA recipients, String user) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutoCommsAudits getAutoCommsAuditTrail(String searchID, Integer pagesize, Integer offset)
			throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserPermissionsResults deleteUserPermissionsInfo(String userID) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserPermissionsResults updateUserPermissionsInfo(UsersPermissions usp) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserPermissionsResults addUserPermissionsInfo(UsersPermissions usp) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UsersPermissions getUserPermissionsInfo(String user) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkPermissions(String UserID, String Permission) throws HttpException {
		boolean allowed = false;
        String regex = "^(?!.*?(.).*?\\1)[RAWD]*$";
        
        if(Pattern.matches(regex, Permission)) {
            System.out.println("Regex Matched");;
            UsersPermissions usp = getUserPermissionsInfo(UserID);
            if(usp != null) {
                UserPermissions up = usp.getUserPermissions().get(0);
                
                System.out.println("up.getPermissions(): " + up.getPermissions());
                if(StringUtils.containsIgnoreCase(up.getPermissions(), Permission))
                    allowed = true;
            }
        }
        
        return allowed;
	}

	@Override
	public String getUserFromToken(String token) {
		String encoddedString = StringUtils.substringAfter(token, StringUtils.SPACE);
        
        byte[] decodedBytes = Base64.getDecoder().decode(encoddedString.split("\\.")[1]);
        String decodedString = new String(decodedBytes);
        
        String user = StringUtils.substringBefore(StringUtils.substringAfter(StringUtils.strip(decodedString,"{\"}"), ":\""),"\"");
        
        return user;
	}

}
