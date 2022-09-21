package za.co.ominsure.synapse.content.scribe.backend.autocomms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import za.co.ominsure.synapse.content.scribe.backend.autocomms.audit.AutoCommsAuditFacade;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.dao.AutoCommsDashboardDao;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.util.AutoCommsHelperFacade;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.util.AutoCommsUtil;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.util.TIAKeyDeserializer;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsAudits;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsResult;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Operation;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientLookup;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientLookupTIA;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookup;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookupTIA;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Result;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.SearchTempateIDs;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.TIASearchIDs;
import uk.co.inc.argon.commons.exceptions.HttpException;

@ApplicationScoped
public class AutoCommsDashboardFacadeBean implements AutoCommsDashboardFacade {
	
	@Inject
	private AutoCommsDashboardDao dao;
	
	@Inject
	private AutoCommsAuditFacade audit;
	
	@Inject
	private AutoCommsHelperFacade helper;
	
    private static final ObjectMapper oMapper = new ObjectMapper();

	@Override
	public RecipientsLookup getTemplateRecipientLookup(SearchTempateIDs templateIDs) throws HttpException {
		return dao.getTemplateRecipientLookupInfo(templateIDs);
	}

	@Override
	public AutoCommsResult updateTemplateRecipientLookup(RecipientsLookup recipients, String user)
			throws HttpException {
		if (recipients != null && !recipients.getRecipientTemplates().isEmpty()) {
            AutoCommsAudits aca = null;
            AutoCommsResult acr = new AutoCommsResult();
            Map<String, String> updateableIDs = new HashMap<>();
            
            try {
                Map<String, Map<Object,Object>> helperMaps = helper.createHelperMaps(recipients, Operation.UPDATE);
                Map<String, String> ids = oMapper.convertValue(helperMaps.get(AutoCommsUtil.IDS_MAP), new TypeReference<Map<String, String>>() {});
                Map<String, String> noReasonIDs = oMapper.convertValue(helperMaps.get(AutoCommsUtil.NO_REASON_IDS), new TypeReference<Map<String, String>>() {});
                Map<String, Object> updateableValuesMap = oMapper.convertValue(helperMaps.get(AutoCommsUtil.ELIGABLE_VALUES_MAP), new TypeReference<Map<String, Object>>() {});
                
                if(!updateableValuesMap.isEmpty()) {
                    Map<String, Object> dbCheckMap = helper.checkIfRecipientTemplatesExistsSYN(updateableValuesMap);
                    if(dbCheckMap.isEmpty())
                        return failedUpdateTemplateRecipientLookup(ids, noReasonIDs);
                    else {

                        Map<String, RecipientLookup> updateableRecipientsMap = oMapper.convertValue(dbCheckMap.get(AutoCommsUtil.ELIGABLE_VALUES_MAP),
                                    new TypeReference<Map<String, RecipientLookup>>() {});
                        
                        updateableIDs.putAll(ids);
                        updateableIDs.keySet().retainAll(updateableRecipientsMap.keySet());
                        ids.keySet().removeAll(updateableRecipientsMap.keySet());
                        
                        if(updateableIDs.isEmpty()) {
                            acr.setMessage(Result.FAILED);
                            acr.getTemplateIDsNotFound().addAll(ids.keySet());
                            
                            return acr;
                        }
                        else {                    
                            Map<String, RecipientLookup> oldValuesMap = oMapper.convertValue(dbCheckMap.get(AutoCommsUtil.OLD_VALUES_MAP),
                                            new TypeReference<Map<String, RecipientLookup>>() {});
                            
                            aca = audit.autoCommsAudit(updateableRecipientsMap, oldValuesMap, updateableIDs, user);
                            
                            return audit.auditAddandUpdateDBTables(aca, updateableRecipientsMap, updateableIDs, ids, noReasonIDs);
                        }
                    }
                }
                else {
                    acr.setMessage(Result.FAILED);
                    acr.getNoUpdateReasonGivenTemplateIDs().addAll(noReasonIDs.keySet());
                    
                    return acr;
                }
            }
            catch(HttpException ex) {
                throw new HttpException(ex.toString(), ex.getStatus());
            }
            catch (Exception e) {
                throw new HttpException(e.toString(), Status.EXPECTATION_FAILED.getStatusCode());
            }
        }
        else
            throw new HttpException(AutoCommsUtil.EMPTY_PAYLOAD, Status.BAD_REQUEST.getStatusCode());
	}

	@Override
	public RecipientsLookupTIA getTemplateRecipientLookupTIA() throws HttpException {
		return dao.getTemplateRecipientLookupTIAInfo();
	}

	@Override
	public AutoCommsResult updateTIARecipientLookup(RecipientsLookupTIA recipients, String user) throws HttpException {
		if (recipients != null && !recipients.getRecipientLookupTIAs().isEmpty()) {
            AutoCommsAudits aca = null;
            AutoCommsResult acr = new AutoCommsResult();
            Map<TIASearchIDs, String> updateableIDs = new HashMap<>();
            
            try {
                
                SimpleModule simpleModule = new SimpleModule();
                simpleModule.addKeyDeserializer(TIASearchIDs.class, new TIAKeyDeserializer());
                oMapper.registerModule(simpleModule);
                
                Map<String, Map<Object,Object>> helperMaps = helper.createHelperMaps(recipients, Operation.UPDATE);
                
                Map<TIASearchIDs, String> ids = oMapper.convertValue(helperMaps.get(AutoCommsUtil.IDS_MAP), new TypeReference<Map<TIASearchIDs, String>>() {});
                Map<TIASearchIDs, String> noReasonIDs = oMapper.convertValue(helperMaps.get(AutoCommsUtil.NO_REASON_IDS), new TypeReference<Map<TIASearchIDs, String>>() {});
                Map<TIASearchIDs, Object> updateableValuesMap = oMapper.convertValue(helperMaps.get(AutoCommsUtil.ELIGABLE_VALUES_MAP),
                                                                                     new TypeReference<Map<TIASearchIDs, Object>>() {});
                
                if(!updateableValuesMap.isEmpty()) {
                    Map<String, Object> dbCheckMap = helper.checkIfRecipientTemplatesExistsTIA(updateableValuesMap);
                    
                    if(dbCheckMap.isEmpty()) {
                        acr.setMessage(Result.FAILED);
                        acr.getTiaSectionIDandLanguageNotFound().addAll(ids.keySet());
                        
                        return acr;
                    }
                    else {
                        
                        Map<TIASearchIDs, RecipientLookupTIA> updateableRecipientsMap = oMapper.convertValue(dbCheckMap.get(AutoCommsUtil.ELIGABLE_VALUES_MAP),
                                        new TypeReference<Map<TIASearchIDs, RecipientLookupTIA>>() {});
                        
                        updateableRecipientsMap.replaceAll((k, v) -> {v.setUser(user); return v;});
                        
                        updateableIDs.putAll(ids);
                        updateableIDs.keySet().retainAll(updateableRecipientsMap.keySet());
                        ids.keySet().removeAll(updateableRecipientsMap.keySet());
                        
                        if(updateableIDs.isEmpty()) {
                            acr.setMessage(Result.FAILED);
                            acr.getTiaSectionIDandLanguageNotFound().addAll(ids.keySet());
                            
                            return acr;
                        }
                        else {                    
                            Map<TIASearchIDs, RecipientLookupTIA> oldValuesMap = oMapper.convertValue(dbCheckMap.get(AutoCommsUtil.OLD_VALUES_MAP),
                                        new TypeReference<Map<TIASearchIDs, RecipientLookupTIA>>() {});
                            
                            aca = audit.autoCommsAuditTIA(updateableRecipientsMap, oldValuesMap, updateableIDs);
                            
                            List<TIASearchIDs> tiaPKs = new ArrayList<>();
                            tiaPKs.addAll(ids.keySet());
                            
                            return audit.auditAndUpdateTIATables(aca, updateableRecipientsMap, updateableIDs, tiaPKs, noReasonIDs);
                        }
                    }
                }
                else {
                    acr.setMessage(Result.FAILED);
                    acr.getNoUpdateReasonGivenTIASectionIDandLanguage().addAll(noReasonIDs.keySet());
                    
                    return acr;
                }
            }
            catch(HttpException ex) {
                throw new HttpException(ex.toString(), ex.getStatus());
            }
            catch (Exception e) {
                throw new HttpException(e.toString(), Status.EXPECTATION_FAILED.getStatusCode());
            }
        }
        else
            throw new HttpException(AutoCommsUtil.EMPTY_PAYLOAD, Status.BAD_REQUEST.getStatusCode());
	}
    
    private AutoCommsResult failedUpdateTemplateRecipientLookup(Map<String, String> ids, Map<String, String> noReasonIDs) {
        AutoCommsResult acr = new AutoCommsResult();
    	acr.setMessage(Result.FAILED);
        if(!ids.isEmpty())
            acr.getTemplateIDsNotFound().addAll(ids.keySet());
        if(!noReasonIDs.isEmpty())
            acr.getNoUpdateReasonGivenTemplateIDs().addAll(noReasonIDs.keySet());
        
        return acr;
    }

}
