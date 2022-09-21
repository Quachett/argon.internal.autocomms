package za.co.ominsure.synapse.content.scribe.backend.autocomms.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import io.quarkus.agroal.DataSource;
import oracle.jdbc.internal.OracleTypes;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.util.AutoCommsUtil;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Attachment;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Attachments;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsAudit;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsAudits;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Operation;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientLookup;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientLookupTIA;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookup;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookupTIA;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.SearchTempateIDs;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UsersPermissions;
import uk.co.inc.argon.commons.exceptions.HttpException;
import uk.co.inc.argon.commons.util.DateUtil;
import uk.co.inc.argon.commons.util.SynapseConstants;

@ApplicationScoped
public class AutoCommsDashboardDaoBean implements AutoCommsDashboardDao {
	@EJB
	//private Logger logger;
	
	@Resource(name = "jdbc/tiaDS", mappedName = "jdbc/tiaDS")
	private DataSource dataSource;
	@Inject
	@DataSource("oracleDs")
	javax.sql.DataSource synapseDatasource;
    
	DateUtil du = new DateUtil();

	@Override
	public RecipientsLookup getTemplateRecipientLookupInfo(SearchTempateIDs templateIDs) throws HttpException {
		int num = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT trl.template_id, trl.broker, trl.primary_contact, trl.client, trl.assessment_contact, trl.description, t.date_created, t.created_by");
		sb.append(StringUtils.SPACE).append("FROM scribe.template_recipient_lookup trl, SCRIBE.template t WHERE trl.template_id = t.name");
        
        if(templateIDs != null) {
            num = templateIDs.getTemplateIds().size();
            
            sb.append(StringUtils.SPACE).append("AND trl.template_id IN (" + AutoCommsUtil.getLineOfQs(num) + ")");
        }
        return getTemplateRecipientLookup(sb.toString(), num, templateIDs);
	}
	
	@Override
	public int[] updateTemplateRecipientLookupInfo(RecipientsLookup recipients) throws HttpException {
String query = "UPDATE scribe.template_recipient_lookup SET broker = ?, primary_contact = ?, client = ?, assessment_contact = ?, description = ? WHERE template_id = ?";
        
        return updateDataTemplateRecipientLookup(query, recipients);
	}

	@Override
	public int[] templateRecipientLookupAuditTrail(AutoCommsAudits autoCommsAudits) throws HttpException {
		String query = "INSERT INTO scribe.comms_dashboard_audit_trail(auto_comm_id, action_performed, original_value, new_value, user_id, reason, audit_time) VALUES (?,?,?,?,?,?,?)";
        //logger.info("Auto Comms Auditing started");
        
        try(Connection conn = synapseDatasource.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            for(AutoCommsAudit aca : autoCommsAudits.getAutoCommsAudit()) {
                ps.setString(1, aca.getAutoCommsId());
                ps.setString(2, aca.getActionPerformed().value());
                ps.setString(3, (aca.getOriginalValue() != null)?aca.getOriginalValue():"");
                ps.setString(4, (aca.getNewValue() != null)?aca.getNewValue():"");
                ps.setString(5, aca.getUserId());
                ps.setString(6, aca.getReason());
                ps.setTimestamp(7, du.getTimestamp(aca.getAuditTime()));
                ps.addBatch();
            }

            //logger.info("Auto Comms Auditing finished");
            return ps.executeBatch();
        }
        catch (SQLException | ParseException e) {
        	String error = "Failed to Audit changed objects: " + e.getMessage();
			//logger.error(error);
            throw new HttpException(error,Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
	}

	@Override
	public AutoCommsAudits getValuesBeforeUpdate(SearchTempateIDs autoCommsIDs) throws HttpException {
		AutoCommsAudits acas = new AutoCommsAudits();
        AutoCommsAudit aca = null;
        StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT ORIGINAL_VALUE FROM COMMS_DASHBOARD_AUDIT_TRAIL WHERE AUTO_COMM_ID IN ");
        sb.append("(");
        sb.append( AutoCommsUtil.getLineOfQs(autoCommsIDs.getTemplateIds().size()));
        sb.append(")");
        String query = sb.toString();
        
        int i=1;
        
        try(Connection conn = synapseDatasource.getConnection(); PreparedStatement ps = conn.prepareStatement(query)){
            if(!autoCommsIDs.getTemplateIds().isEmpty()) {
                for(String autoCommsID : autoCommsIDs.getTemplateIds())
                    ps.setString(i++, autoCommsID);
                
                try(ResultSet rs = ps.executeQuery()) {
	                if(rs.next() != false) {
	                    do{
	                        aca = new AutoCommsAudit();
	                        
	                        if(StringUtils.isNoneEmpty(rs.getString("ORIGINAL_VALUE"))) {
	                            aca.setOriginalValue(rs.getString("ORIGINAL_VALUE"));
	                            acas.getAutoCommsAudit().add(aca);
	                        }
	                    }while(rs.next());
	                }
                }
            }
            return acas;
        }
        catch (SQLException e) {
        	String error = "Failed to get revert falues to revert changes made to the Database: " + e.getMessage();
			//logger.error(error);
            throw new HttpException(error,Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
	}

	@Override
	public RecipientsLookupTIA getTemplateRecipientLookupTIAInfo() throws HttpException {
		String query = "{? = call TIA.IBBPM_CLAIM.get_comms_dashboard()}";

		return getTemplateRecipientLookupTIA(query);
	}

	@Override
	public boolean updateTemplateRecipientLookupTIAInfo(RecipientsLookupTIA recipients) throws HttpException {
		String query = "{call TIA.IBBPM_UPDATE.update_comms_dashboard(?,?,?,?,?,?,?,?)}";
        
        return updateTemplateRecipientLookupTIA(query, recipients);
	}

	@Override
	public AutoCommsAudits getAuditTrailInfo(String source, String term, Integer pagesize, Integer offset) throws HttpException {
		
		String query = getQuery(source);

        return getAuditTrail(query, term, pagesize, offset);
	}

	@Override
	public UsersPermissions getUserPermissionsInfo(String userID) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addUserPermissionsInfo(UsersPermissions usp) throws HttpException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateUserPermissionsInfo(UsersPermissions usp) throws HttpException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteUserPermissionsInfo(String userID) throws HttpException {
		// TODO Auto-generated method stub
		return false;
	}
    
    private int[] updateDataTemplateRecipientLookup(String query, RecipientsLookup recipients) throws HttpException {        
        try(Connection conn = synapseDatasource.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
                for(RecipientLookup recipient: recipients.getRecipientTemplates()) {
                ps.setString(1, (recipient.isBroker())?SynapseConstants.TIA_Y:"");
                ps.setString(2, (recipient.isPrimaryContact())?SynapseConstants.TIA_Y:"");
                ps.setString(3, (recipient.isClient())?SynapseConstants.TIA_Y:"");
                ps.setString(4, (recipient.isAssessmentContact())?SynapseConstants.TIA_Y:"");
                ps.setString(5, recipient.getDescription());
                ps.setString(6, recipient.getTemplateId());
                ps.addBatch();
            }
            return ps.executeBatch();
        }
        catch (SQLException e) {
        	String error = "Failed to update Objects in Database: " + e.getMessage();
			//logger.error(error);
            throw new HttpException(error,Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }
    
    private boolean updateTemplateRecipientLookupTIA(String query, RecipientsLookupTIA recipients) throws HttpException {
    	boolean success = false;
        try(Connection conn = ((Statement) dataSource).getConnection();//remove cast
        		CallableStatement cs = conn.prepareCall(query)){
        	//logger.info("Started Get Comms Dashboard for TIA");
        	
        	for(RecipientLookupTIA rl : recipients.getRecipientLookupTIAs()) {
        		cs.setInt(1, rl.getSectionId());
        		cs.setString(2, rl.getLanguage());
        		cs.setString(3, (rl.isBroker())?SynapseConstants.TIA_Y:null);
        		cs.setString(4, (rl.isClient())?SynapseConstants.TIA_Y:null);
        		cs.setString(5, (rl.isPrimaryContact())?SynapseConstants.TIA_Y:null);
        		cs.setString(6, (rl.isAssessmentContact())?SynapseConstants.TIA_Y:null);
        		cs.setString(7, rl.getUser());
        		cs.registerOutParameter(8, java.sql.Types.VARCHAR);
        		cs.execute();
            	
            	String errMsg = cs.getString(8);
            	if(!StringUtils.isBlank(errMsg)) {
            		throw new HttpException(errMsg, 400);
            	}
        	}
        	success = true;
        } catch (SQLException e) {
			String error = "Failed to update recipients on TIA. Error: " + e.getMessage();
			//logger.error(error);
			throw new HttpException(error, Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
        return success;
    }
    
    private AutoCommsAudits getAuditTrail(String query, String searchID, Integer pagesize, Integer offset) throws HttpException {
        AutoCommsAudit aca = null;
        AutoCommsAudits acas = null;
        
        try (Connection conn = synapseDatasource.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)){
                //logger.info("Started Getting Auto Comms Trail");
            
            if(StringUtils.contains(searchID, "templateId")) {
                ps.setString(1, searchID);
                ps.setString(2, searchID);
            }
            else {
                ps.setString(1, StringUtils.substringBefore(searchID, "|"));
                ps.setString(2, StringUtils.substringAfter(searchID, "|"));
                ps.setString(3, StringUtils.substringBefore(searchID, "|"));
                ps.setString(4, StringUtils.substringAfter(searchID, "|"));
            }
            
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()) {
                    acas = new AutoCommsAudits();
                    int count = 0;
                    int addedCount = 0;
                    
                    do {
                        if(count >= offset) {
                            if(addedCount>=pagesize)
                                break;
                            aca = new AutoCommsAudit();

                            aca.setAutoCommsId(rs.getString("AUTO_COMM_ID"));
                            aca.setActionPerformed(Operation.fromValue(rs.getString("ACTION_PERFORMED"))); 
                            aca.setOriginalValue(rs.getString("ORIGINAL_VALUE"));
                            aca.setNewValue(rs.getString("NEW_VALUE"));
                            aca.setUserId(rs.getString("USER_ID"));
                            aca.setReason(rs.getString("REASON"));
                            aca.setAuditTime(du.formatDateTimeMilli(rs.getTimestamp("AUDIT_TIME")));
                            
                            acas.getAutoCommsAudit().add(aca);
                            addedCount++;
                        }
                        count++;
                    }while(rs.next());
                }
            }
            
        } catch (SQLException e) {
            String error = "Failed to retrieve the recipient list. Error: " + e.getMessage();
            //logger.error(error);
            throw new HttpException(error, Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        
        return acas;
    }

	private RecipientsLookup getTemplateRecipientLookup(String query, int num, SearchTempateIDs templateIDs) throws HttpException {
		RecipientLookup recipientLookup = null;
        RecipientsLookup recipients = null;
        
        try (Connection conn = synapseDatasource.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)){
            
            for(int i=0;i<num;i++)
                ps.setString(i+1, templateIDs.getTemplateIds().get(i));
            
            try(ResultSet rs = ps.executeQuery()) {
                recipients = new RecipientsLookup();
                while(rs.next()) {
                    recipientLookup = new RecipientLookup();
                    
                    recipientLookup.setTemplateId(rs.getString("TEMPLATE_ID"));
                    recipientLookup.setBroker(StringUtils.equalsIgnoreCase(rs.getString("BROKER"), SynapseConstants.TIA_Y));
                    recipientLookup.setPrimaryContact(StringUtils.equalsIgnoreCase(rs.getString("PRIMARY_CONTACT"),SynapseConstants.TIA_Y));
                    recipientLookup.setClient(StringUtils.equalsIgnoreCase(rs.getString("CLIENT"),SynapseConstants.TIA_Y));
                    recipientLookup.setAssessmentContact(StringUtils.equalsIgnoreCase(rs.getString("ASSESSMENT_CONTACT"),SynapseConstants.TIA_Y));
                    recipientLookup.setDescription(rs.getString("DESCRIPTION"));
                    recipientLookup.setUpdateDate(rs.getString("DATE_CREATED"));
                    recipientLookup.setUpdatedBy(rs.getString("CREATED_BY"));
                    
                    Map<String, String> result = checkAttachmentPresent(rs.getString("TEMPLATE_ID"), conn);
                    recipientLookup.setAttachmentPresent(Boolean.parseBoolean(result.get(AutoCommsUtil.IS_ATT_PRESENT)));
                    recipientLookup.setAttachmentName(result.get(AutoCommsUtil.ATT_NAME));
                    
                    //System.out.println(checkAttachmentPresent(rs.getString("TEMPLATE_ID"), conn));
                    
                    recipients.getRecipientTemplates().add(recipientLookup);
                }
            }
        }
        catch (SQLException e) {
        	String syn_err = "Failed to retrieve the recipient list. Error: " + e.getMessage();
			System.out.println(syn_err);
            throw new HttpException(syn_err,Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return recipients;
	}
    
    public RecipientsLookupTIA getTemplateRecipientLookupTIA(String query) throws HttpException{
		RecipientsLookupTIA recipients = null;
		RecipientLookupTIA recipientLookup = null;

		try (Connection conn = ((Statement) dataSource).getConnection(); //remove cast
				CallableStatement cs = conn.prepareCall(query)) {
			//logger.info("Started Updating Comms Dashboard for TIA");

			cs.registerOutParameter(1, OracleTypes.CURSOR);
			cs.execute();

			try (ResultSet rs = (ResultSet) cs.getObject(1)) {

				if(rs.next()) {
					recipients = new RecipientsLookupTIA();
					
					do {
						recipientLookup = new RecipientLookupTIA();

						recipientLookup.setSectionId(rs.getInt(AutoCommsUtil.SEC_ID));
						recipientLookup.setLanguage(rs.getString(AutoCommsUtil.LANGUAGE));
						recipientLookup.setBroker(StringUtils.equalsIgnoreCase(rs.getString(AutoCommsUtil.BROKER), SynapseConstants.TIA_Y));
						recipientLookup.setClient(StringUtils.equalsIgnoreCase(rs.getString(AutoCommsUtil.CLIENT), SynapseConstants.TIA_Y));
						recipientLookup.setPrimaryContact(StringUtils.equalsIgnoreCase(rs.getString(AutoCommsUtil.PRIMARY_CONTACT), SynapseConstants.TIA_Y));
						recipientLookup.setAssessmentContact(StringUtils.equalsIgnoreCase(rs.getString(AutoCommsUtil.ASSESSMENT_CONTACT), SynapseConstants.TIA_Y));
						recipientLookup.setDescription(rs.getString(AutoCommsUtil.DESCRIPTION));
						recipientLookup.setText(rs.getString(AutoCommsUtil.TEXT));

						recipients.getRecipientLookupTIAs().add(recipientLookup);
					}while (rs.next());
				}
			}
		} catch (SQLException e) {
			String error = "Failed to retrieve the recipient list. Error: " + e.getMessage();
			//logger.error(error);
			throw new HttpException(error, Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return recipients;    	
    }

	private Map<String, String> checkAttachmentPresent(String templateId, Connection conn) throws HttpException {
		Map<String, String> result = new HashMap<>();
		String query = "SELECT * FROM scribe.attachment_template WHERE NAME = ?";
		
		try(PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, templateId);
			try(ResultSet rs = ps.executeQuery()) {
				boolean bool = rs.next();
				result.put(AutoCommsUtil.IS_ATT_PRESENT, String.valueOf(bool));
				if(bool) {
					System.out.println("if false then no: " + bool);
					result.put(AutoCommsUtil.ATT_NAME, rs.getString(AutoCommsUtil.ATT_NAME));
				}
			}
		} 
		catch (SQLException e) {
			String syn_err = "Failed to lookup Scribe Attachments. Error: " + e.getMessage();
			System.out.println(syn_err);
			e.printStackTrace();
			throw new HttpException(syn_err,Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return result;
	}
	
	private Attachments getAttachement(String templateId, Connection conn) throws HttpException {
		Attachment att = new Attachment();
		Attachments atts = new Attachments();
		String query = "SELECT name, guid FROM scribe.attachment WHERE NAME = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, templateId);
			try(ResultSet rs = ps.executeQuery()) {
				while(rs.next()!=false) {
					 att.setAttachmentGuid(rs.getString(AutoCommsUtil.GUID));
					 att.setAttachmentName(rs.getString(AutoCommsUtil.NAME));
					 atts.getAttachment().add(att);
				}
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
			String syn_err = "Failed to retrieve templateless Hoard Attachments. Error: " + e.getMessage();
			System.out.println(syn_err);
			throw new HttpException(syn_err,Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return atts;
	}
    
    private String getQuery(String source) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT * FROM scribe.comms_dashboard_audit_trail");
        sb.append(" WHERE ");
        
        switch (source) {
			case AutoCommsUtil.SYN:
				sb.append("UPPER(original_value)");
	            sb.append(" LIKE UPPER(?)");
	            sb.append(" OR ");
	            sb.append("UPPER(new_value)");
	            sb.append(" LIKE UPPER(?)");
				break;

			default:
				sb.append("(UPPER(original_value)");
	            sb.append(" LIKE UPPER(?)");
	            sb.append(" AND ");
	            sb.append("UPPER(original_value)");
	            sb.append(" LIKE UPPER(?))");
	            sb.append(" OR ");
	            sb.append("(UPPER(new_value)");
	            sb.append(" LIKE UPPER(?)");
	            sb.append(" AND ");
	            sb.append("UPPER(new_value)");
	            sb.append(" LIKE UPPER(?))");
				break;
		}
        
        sb.append(" ORDER BY AUDIT_TIME DESC");
        
        return sb.toString();
    }

}
