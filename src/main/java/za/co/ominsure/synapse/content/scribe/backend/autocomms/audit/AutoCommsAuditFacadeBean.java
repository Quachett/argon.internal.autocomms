package za.co.ominsure.synapse.content.scribe.backend.autocomms.audit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import uk.co.inc.argon.commons.exceptions.HttpException;
import uk.co.inc.argon.commons.util.DateUtil;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.dao.AutoCommsDashboardDao;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.util.AutoCommsUtil;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsAudit;
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

@ApplicationScoped
public class AutoCommsAuditFacadeBean implements AutoCommsAuditFacade {
	
	@Inject
	private AutoCommsDashboardDao dao;
	
	private DateUtil du = new DateUtil();

	@Override
	public AutoCommsAudits getAutoCommsAuditTrail(String source, String templateId, Integer pagesize, Integer offset) throws HttpException {
		StringBuilder sb = new StringBuilder();

	    sb.append("%\"templateId\"");
	    sb.append(":\"");
	    sb.append(templateId);
	    sb.append("\"%");
	    System.out.println(sb.toString());
		return dao.getAuditTrailInfo(source, sb.toString(), pagesize, offset);
	}
	
	@Override
	public AutoCommsAudits getTIAAutoCommsAuditTrail(String source, int sectionId, String lang, Integer pagesize, Integer offset) throws HttpException {
		StringBuilder sb = new StringBuilder();

        sb.append("%\"sectionId\"");
        sb.append(":");
        sb.append(sectionId);
        sb.append("%");
        sb.append("|");
        sb.append("%\"language\"");
        sb.append(":\"");
        sb.append(lang);
        sb.append("\"%");
	    System.out.println(sb.toString());
        return dao.getAuditTrailInfo(source, sb.toString(), pagesize, offset);
	}

	@Override
	public AutoCommsAudits autoCommsAudit(Map<String, RecipientLookup> newValuesMap,
			Map<String, RecipientLookup> originalValuesMap, Map<String, String> ids, String user) throws HttpException {
        AutoCommsAudit autoCommsAudit = null;
        AutoCommsAudits aca = new AutoCommsAudits();
        SearchTempateIDs templateIDs = new SearchTempateIDs();
        
        templateIDs.getTemplateIds().addAll(ids.keySet());
        
        for (String templateID : templateIDs.getTemplateIds()) {
            autoCommsAudit = new AutoCommsAudit();
            autoCommsAudit.setActionPerformed(Operation.UPDATE);
            autoCommsAudit.setAuditTime(du.formatDateTime(new Date()));
            autoCommsAudit.setAutoCommsId(ids.get(templateID));
            autoCommsAudit.setUserId(user);
            

            autoCommsAudit.setReason(newValuesMap.get(templateID).getReason());
            autoCommsAudit.setNewValue(AutoCommsUtil.gson.toJson(newValuesMap.get(templateID)));
            autoCommsAudit.setOriginalValue(AutoCommsUtil.gson.toJson(originalValuesMap.get(templateID)));
            aca.getAutoCommsAudit().add(autoCommsAudit);
        }
        
        return aca;
	}

	@Override
	public AutoCommsAudits autoCommsAuditTIA(Map<TIASearchIDs, RecipientLookupTIA> newValuesMap,
			Map<TIASearchIDs, RecipientLookupTIA> originalValuesMap, Map<TIASearchIDs, String> ids) throws HttpException {
		AutoCommsAudit autoCommsAudit = null;
        AutoCommsAudits aca = new AutoCommsAudits();
        List<TIASearchIDs> tiaSearchIDs = new ArrayList<>();
        
        tiaSearchIDs.addAll(ids.keySet());
        
        for (TIASearchIDs tiaSearchID : tiaSearchIDs) {
            autoCommsAudit = new AutoCommsAudit();
            autoCommsAudit.setActionPerformed(Operation.UPDATE);
            autoCommsAudit.setAuditTime(du.formatDateTime(new Date()));
            autoCommsAudit.setAutoCommsId(ids.get(tiaSearchID));
            autoCommsAudit.setUserId(newValuesMap.get(tiaSearchID).getUser());

            autoCommsAudit.setReason(newValuesMap.get(tiaSearchID).getReason());
            autoCommsAudit.setNewValue(AutoCommsUtil.gson.toJson(newValuesMap.get(tiaSearchID)));
            autoCommsAudit.setOriginalValue(AutoCommsUtil.gson.toJson(originalValuesMap.get(tiaSearchID)));
            aca.getAutoCommsAudit().add(autoCommsAudit);
        }
        
        return aca;
	}
    
	@Override
    public AutoCommsResult auditAddandUpdateDBTables(AutoCommsAudits aca, Map<String, RecipientLookup> executableValuesMap, Map<String, String> executableIDsMap,
                    Map<String, String> nonExecutableIDsMap, Map<String, String> noReasonIDsMap) throws HttpException {
        AutoCommsResult acr = new AutoCommsResult();
        List<Object> nonExecIDs = null;
        List<Object> noReasonIDs = null;
        SearchTempateIDs autoCommsIDs = new SearchTempateIDs();
        SearchTempateIDs templateIDs = new SearchTempateIDs();
        templateIDs.getTemplateIds().addAll(executableValuesMap.keySet());
        autoCommsIDs.getTemplateIds().addAll(executableIDsMap.values());

        if (aca != null) {
            try {
                int[] insAudit = dao.templateRecipientLookupAuditTrail(aca);

                if (Arrays.stream(insAudit).allMatch(k -> k == 1)) {
                    try {
                        dao.updateTemplateRecipientLookupInfo(createNewRecipients(executableValuesMap));

                        if(nonExecutableIDsMap != null)
                        	nonExecIDs = nonExecutableIDsMap.keySet().stream().map(k -> (Object) k)
                            	.collect(Collectors.toList());
                        
                        if(noReasonIDsMap != null)
                        	noReasonIDs = noReasonIDsMap.keySet().stream().map(k -> (Object) k)
                            	.collect(Collectors.toList());

                        acr = setResult(nonExecIDs, Operation.UPDATE, noReasonIDs);
                    } catch (Exception e) {
                        throw new HttpException(AutoCommsUtil.DB_ERROR + e.getMessage(),
                                        Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    }
                }
                else
                	throw new HttpException(AutoCommsUtil.DB_AUDIT_ERROR, Status.INTERNAL_SERVER_ERROR.getStatusCode());
        
            } catch (HttpException e) {
                
                throw new HttpException(AutoCommsUtil.DB_AUDIT_ERROR + e.getMessage(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }
        } else {
            acr.setMessage(Result.FAILED);
            if(!nonExecutableIDsMap.isEmpty())
                acr.getTemplateIDsNotFound().addAll(nonExecutableIDsMap.keySet());
            if(!noReasonIDsMap.isEmpty())
                acr.getNoUpdateReasonGivenTemplateIDs().addAll(noReasonIDsMap.keySet());
        }

        return acr;
    }
    
	@Override
    public AutoCommsResult auditAndUpdateTIATables(AutoCommsAudits aca, Map<TIASearchIDs, RecipientLookupTIA> executableValuesMap, Map<TIASearchIDs, String> executableIDsMap, 
            List<TIASearchIDs> nonExecutableIDs, Map<TIASearchIDs, String> noReasonIDsMap) throws HttpException {
        AutoCommsResult acr = new AutoCommsResult();
        SearchTempateIDs autoCommsIDs = new SearchTempateIDs();
        
        autoCommsIDs.getTemplateIds().addAll(executableIDsMap.values());
        
        if(aca != null) {
            try {
                int[] insAudit = dao.templateRecipientLookupAuditTrail(aca);
                
                if(Arrays.stream(insAudit).allMatch(i -> i==1)) {
                    try {
                        dao.updateTemplateRecipientLookupTIAInfo(createNewRecipientsTIA(executableValuesMap));
    
                        List<Object> nonExecIDs = nonExecutableIDs.stream().map(k -> (Object)k).collect(Collectors.toList());
                        List<Object> noReasonIDs = noReasonIDsMap.keySet().stream().map(k -> (Object) k)
                            .collect(Collectors.toList());
                        
                        acr = setResult(nonExecIDs, Operation.UPDATE, noReasonIDs);
                    }catch (HttpException e) {
                        throw new HttpException(AutoCommsUtil.DB_ERROR + e.getMessage(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    }
                }
            }catch (HttpException e) {
                throw new HttpException(AutoCommsUtil.DB_AUDIT_ERROR + e.getMessage(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }
        }
        else{
            acr.setMessage(Result.FAILED);
            if(!nonExecutableIDs.isEmpty())
                acr.getTiaSectionIDandLanguageNotFound().addAll(nonExecutableIDs);
            if(!noReasonIDsMap.isEmpty())
                acr.getNoUpdateReasonGivenTIASectionIDandLanguage().addAll(noReasonIDsMap.keySet());
        }        
        return acr;
    }
    
    private RecipientsLookup createNewRecipients(Map<String, RecipientLookup> newValuesMap) {
        RecipientsLookup newRecipients = new RecipientsLookup();
        
        newRecipients.getRecipientTemplates().addAll(newValuesMap.values());
        
        return newRecipients;
    }
    
    private RecipientsLookupTIA createNewRecipientsTIA(Map<TIASearchIDs, RecipientLookupTIA> newValuesMap) {
        RecipientsLookupTIA newRecipients = new RecipientsLookupTIA();
        
        newRecipients.getRecipientLookupTIAs().addAll(newValuesMap.values());
        
        return newRecipients;
    }
    
    private AutoCommsResult setResult(List<Object> newIDs, Operation actionPerformed, List<Object> noReasonIDs) {
        AutoCommsResult acr = new AutoCommsResult();
        List<String> synapseIDs = null;
        List<TIASearchIDs> tiaIDs = null;
        List<String> noReasonSynIDs = null;
        List<TIASearchIDs> noReasonTiaIDs = null;
            
        acr.setMessage(Result.SUCCESS);
        
        if(!newIDs.isEmpty()) {
            if(((ArrayList<?>)newIDs).get(0) instanceof String)
                synapseIDs = newIDs.stream().map(k -> Objects.toString(k, null)).collect(Collectors.toList());
            else
                tiaIDs = newIDs.stream().map(k -> (TIASearchIDs)k).collect(Collectors.toList());
            
            if(acr.getMessage().equals(Result.SUCCESS)) 
                acr.setMessage(Result.PARTIAL_SUCCESS);
            if (actionPerformed.equals(Operation.CREATE)) {
                if(synapseIDs != null)
                    acr.getDuplicateTemplateIDs().addAll(synapseIDs);
            }
            else {
                if(synapseIDs != null)
                    acr.getTemplateIDsNotFound().addAll(synapseIDs);
                else
                    acr.getTiaSectionIDandLanguageNotFound().addAll(tiaIDs);
            }
        }
        
        if(!noReasonIDs.isEmpty()) {
            if(((ArrayList<?>)noReasonIDs).get(0) instanceof String)
                noReasonSynIDs = noReasonIDs.stream().map(k -> Objects.toString(k, null)).collect(Collectors.toList());
            else
                noReasonTiaIDs = noReasonIDs.stream().map(k -> (TIASearchIDs)k).collect(Collectors.toList());
            
            if(acr.getMessage().equals(Result.SUCCESS)) 
                acr.setMessage(Result.PARTIAL_SUCCESS);
            if(noReasonSynIDs != null)
                acr.getNoUpdateReasonGivenTemplateIDs().addAll(noReasonSynIDs);
            else
                acr.getNoUpdateReasonGivenTIASectionIDandLanguage().addAll(noReasonTiaIDs);
        }
            
        return acr;
    }
}
