package za.co.ominsure.synapse.content.scribe.backend.autocomms.util;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import uk.co.inc.argon.commons.exceptions.HttpException;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsResult;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Operation;

@ApplicationScoped
public interface AutoCommsHelperFacade {
	public Map<String, Map<Object,Object>> createHelperMaps(Object objs, Operation ops) throws HttpException;
	public Map<String, Object> checkIfRecipientTemplatesExistsSYN(Map<String, Object> newValuesMap) throws HttpException;
	public AutoCommsResult failedUpdateTemplateRecipientLookup(Map<String, String> ids, Map<String, String> noReasonIDs);
}
