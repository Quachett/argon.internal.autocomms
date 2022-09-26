package za.co.ominsure.synapse.content.scribe.backend.autocomms.dao;

import javax.enterprise.context.ApplicationScoped;

import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Attachments;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsAudits;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookup;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookupTIA;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.SearchTempateIDs;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UsersPermissions;
import uk.co.inc.argon.commons.exceptions.HttpException;

@ApplicationScoped
public interface AutoCommsDashboardDao {
	public RecipientsLookup getTemplateRecipientLookupInfo(SearchTempateIDs templateIDs) throws HttpException;

    public int[] updateTemplateRecipientLookupInfo(RecipientsLookup recipients) throws HttpException;
    
    public int[] templateRecipientLookupAuditTrail(AutoCommsAudits autoCommsAudits) throws HttpException;

    public AutoCommsAudits getValuesBeforeUpdate(SearchTempateIDs autoCommsIDs) throws HttpException;

    public RecipientsLookupTIA getTemplateRecipientLookupTIAInfo() throws HttpException;

    public boolean updateTemplateRecipientLookupTIAInfo(RecipientsLookupTIA recipients) throws HttpException;

	public AutoCommsAudits getAuditTrailInfo(String source, String term, Integer pagesize, Integer offset) throws HttpException;

	public UsersPermissions getUserPermissionsInfo(String userID) throws HttpException;

	public boolean addUserPermissionsInfo(UsersPermissions usp) throws HttpException;

	public boolean updateUserPermissionsInfo(UsersPermissions usp) throws HttpException;

	public boolean deleteUserPermissionsInfo(String userID) throws HttpException;
	
	public Attachments getAttachments(String name) throws HttpException;
}
