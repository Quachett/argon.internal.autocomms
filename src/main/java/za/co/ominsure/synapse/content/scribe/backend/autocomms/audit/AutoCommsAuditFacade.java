package za.co.ominsure.synapse.content.scribe.backend.autocomms.audit;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import uk.co.inc.argon.commons.exceptions.HttpException;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsAudits;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsResult;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientLookup;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientLookupTIA;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.TIASearchIDs;

@ApplicationScoped
public interface AutoCommsAuditFacade {
	public AutoCommsAudits getAutoCommsAuditTrail(String source, String templaateId, Integer pagesize, Integer offset) throws HttpException;
	
	public AutoCommsAudits getTIAAutoCommsAuditTrail(String souce, int sectionId, String lang, Integer pagesize, Integer offset) throws HttpException;
	
	public AutoCommsAudits autoCommsAudit(Map<String, RecipientLookup> newValuesMap, Map<String, RecipientLookup> originalValuesMap,
            Map<String, String> ids, String user) throws HttpException;
	
	public AutoCommsAudits autoCommsAuditTIA(Map<TIASearchIDs, RecipientLookupTIA> newValuesMap, Map<TIASearchIDs, RecipientLookupTIA> originalValuesMap,
            Map<TIASearchIDs, String> ids) throws HttpException;
	
	public AutoCommsResult auditAddandUpdateDBTables(AutoCommsAudits aca, Map<String, RecipientLookup> executableValuesMap, Map<String, String> executableIDsMap,
            Map<String, String> nonExecutableIDsMap, Map<String, String> noReasonIDsMap) throws HttpException;
	
	AutoCommsResult auditAndUpdateTIATables(AutoCommsAudits aca,  Map<TIASearchIDs, RecipientLookupTIA> executableValuesMap, Map<TIASearchIDs, String> executableIDsMap, 
            List<TIASearchIDs> nonExecutableIDs, Map<TIASearchIDs, String> noReasonIDsMap) throws HttpException;
}
