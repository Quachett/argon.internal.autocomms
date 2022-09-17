package za.co.ominsure.synapse.content.scribe.backend;

import javax.enterprise.context.ApplicationScoped;

import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsAudits;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsResult;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookup;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookupTIA;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.SearchTempateIDs;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UserPermissionsResults;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UsersPermissions;
import uk.co.inc.argon.commons.exceptions.HttpException;

@ApplicationScoped
public interface AutoCommsDashboardFacade {
	public RecipientsLookup getTemplateRecipientLookup(SearchTempateIDs templateIDs) throws HttpException;

    public AutoCommsResult addTemplateRecipientLookup(RecipientsLookup recipients, String user) throws HttpException;

    public AutoCommsResult updateTemplateRecipientLookup(RecipientsLookup recipients, String user) throws HttpException;
    
    public AutoCommsResult deleteTemplateRecipientLookup(String templateID, String user) throws HttpException;

    public RecipientsLookupTIA getTemplateRecipientLookupTIA() throws HttpException;

	public AutoCommsResult updateTIARecipientLookup(RecipientsLookupTIA recipients, String user) throws HttpException;

	public AutoCommsAudits getAutoCommsAuditTrail(String searchID, Integer pagesize, Integer offset) throws HttpException;

	public UserPermissionsResults deleteUserPermissionsInfo(String userID) throws HttpException;

	public UserPermissionsResults updateUserPermissionsInfo(UsersPermissions usp) throws HttpException;

	public UserPermissionsResults addUserPermissionsInfo(UsersPermissions usp) throws HttpException;

	public UsersPermissions getUserPermissionsInfo(String user) throws HttpException;
	
	public boolean checkPermissions(String UserID, String Permission) throws HttpException;
	
	public String getUserFromToken(String token);
}
