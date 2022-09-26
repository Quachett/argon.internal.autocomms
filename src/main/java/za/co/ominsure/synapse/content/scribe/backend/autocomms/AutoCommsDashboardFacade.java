package za.co.ominsure.synapse.content.scribe.backend.autocomms;

import javax.enterprise.context.ApplicationScoped;

import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Attachments;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsResult;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookup;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookupTIA;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.SearchTempateIDs;
import uk.co.inc.argon.commons.exceptions.HttpException;

@ApplicationScoped
public interface AutoCommsDashboardFacade {
	public RecipientsLookup getTemplateRecipientLookup(SearchTempateIDs templateIDs) throws HttpException;

    public AutoCommsResult updateTemplateRecipientLookup(RecipientsLookup recipients, String user) throws HttpException;

    public RecipientsLookupTIA getTemplateRecipientLookupTIA() throws HttpException;

	public AutoCommsResult updateTIARecipientLookup(RecipientsLookupTIA recipients, String user) throws HttpException;
	
	public Attachments getAttachments(String name) throws HttpException;
}
