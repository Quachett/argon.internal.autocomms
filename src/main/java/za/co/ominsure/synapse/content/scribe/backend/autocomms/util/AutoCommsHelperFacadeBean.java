package za.co.ominsure.synapse.content.scribe.backend.autocomms.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import uk.co.inc.argon.commons.exceptions.HttpException;
import uk.co.inc.argon.commons.util.GuidUtil;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.dao.AutoCommsDashboardDao;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsResult;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Operation;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientLookup;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientLookupTIA;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookup;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookupTIA;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Result;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.SearchTempateIDs;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.TIASearchIDs;

@ApplicationScoped
public class AutoCommsHelperFacadeBean implements AutoCommsHelperFacade {

	@Inject
	AutoCommsDashboardDao dao;
	
	@Override
	public Map<String, Map<Object, Object>> createHelperMaps(Object objs, Operation ops) throws HttpException {
		Map<String, Map<Object,Object>> helperMaps = new HashMap<>();
        Map<Object, Object> ids = new HashMap<>();
        Map<Object, Object> noReasonIDs = new HashMap<>();
        Map<Object, Object> newValuesMap = new HashMap<>();
        
        try {
            if(objs instanceof RecipientsLookup) {
                
                RecipientsLookup rsl = (RecipientsLookup) objs;
                
                newValuesMap = rsl.getRecipientTemplates().stream()
                    .collect(Collectors.toMap(RecipientLookup::getTemplateId, k -> k));
                if(ops != null)
                    noReasonIDs = newValuesMap.entrySet().stream().filter(x -> StringUtils.isBlank
                            (((RecipientLookup)x.getValue()).getReason())).collect(Collectors.toMap(x -> x.getKey(), x -> ""));
            }
            else {
                RecipientsLookupTIA rslrTIA = (RecipientsLookupTIA) objs;

                newValuesMap = rslrTIA.getRecipientLookupTIAs().stream()
                    .collect(Collectors.toMap(k -> {
                        TIASearchIDs tiaPK = new TIASearchIDs();
                        tiaPK.setSectionId(k.getSectionId());
                        tiaPK.setLanguage(k.getLanguage());
                        return tiaPK;}, x-> x));
                if(ops != null)
                    noReasonIDs = newValuesMap.entrySet().stream().filter(x -> StringUtils.isBlank
                              (((RecipientLookupTIA)x.getValue()).getReason())).collect(Collectors.toMap(x -> x.getKey(), x -> ""));
            }
            
            //Synlog.info(AutoCommsUtil.NO_REASON_IDS, noReasonIDs.keySet().toString());
            
            newValuesMap.keySet().removeAll(noReasonIDs.keySet());
            
            ids = newValuesMap.keySet().stream()
                .collect(Collectors.toMap(key -> key, key -> {
                    return getguid();
                }));

            helperMaps.put(AutoCommsUtil.IDS_MAP, ids);
            helperMaps.put(AutoCommsUtil.NO_REASON_IDS, noReasonIDs);
            helperMaps.put(AutoCommsUtil.ELIGABLE_VALUES_MAP, newValuesMap);
            
            return helperMaps;
        }
        catch (Exception e) {
            throw new HttpException(e.toString(), Status.EXPECTATION_FAILED.getStatusCode());
        }
	}
    
	@Override
    public Map<String, Object> checkIfRecipientTemplatesExistsSYN(Map<String, Object> newValuesMap) throws HttpException {
        RecipientsLookup recipientsLookup = null;
        SearchTempateIDs templateIDs = new SearchTempateIDs();
        Map<String, Object> oldValuesMap;
        Map<String, Object> dbCheckMap = new HashMap<>();
        
        templateIDs.getTemplateIds().addAll(newValuesMap.keySet());
        recipientsLookup = dao.getTemplateRecipientLookupInfo(templateIDs);
        
        if(recipientsLookup != null) {
            templateIDs = new SearchTempateIDs();
            
            templateIDs.getTemplateIds().addAll(recipientsLookup.getRecipientTemplates().stream()
                                                .map(k -> k.getTemplateId()).collect(Collectors.toList()));
            
            oldValuesMap = recipientsLookup.getRecipientTemplates().stream().collect(Collectors.toMap(RecipientLookup::getTemplateId, rl -> rl));
            newValuesMap.keySet().retainAll(templateIDs.getTemplateIds());
            dbCheckMap.put(AutoCommsUtil.ELIGABLE_VALUES_MAP, newValuesMap);
            dbCheckMap.put(AutoCommsUtil.OLD_VALUES_MAP, oldValuesMap);
        }
        
        return dbCheckMap;
    }
    
	@Override
    public AutoCommsResult failedUpdateTemplateRecipientLookup(Map<String, String> ids, Map<String, String> noReasonIDs) {
        AutoCommsResult acr = new AutoCommsResult();
    	acr.setMessage(Result.FAILED);
        if(!ids.isEmpty())
            acr.getTemplateIDsNotFound().addAll(ids.keySet());
        if(!noReasonIDs.isEmpty())
            acr.getNoUpdateReasonGivenTemplateIDs().addAll(noReasonIDs.keySet());
        
        return acr;
    }
	
	private Map<String, Object> filterMap(Map<String, Object> map, SearchTempateIDs templateIds) {
		map.keySet().retainAll(templateIds.getTemplateIds());
		return map;
	}
    
    private String getguid() {
        return RegExUtils.removeAll(GuidUtil.generateGUID(), Pattern.compile("[{}-]"));
    }

}
