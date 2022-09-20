package za.co.ominsure.synapse.content.scribe.backend.autocomms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import io.quarkus.agroal.DataSource;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.util.AutoCommsUtil;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Attachment;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Attachments;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsAudits;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientLookup;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookup;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookupTIA;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.SearchTempateIDs;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UsersPermissions;
import uk.co.inc.argon.commons.exceptions.HttpException;
import uk.co.inc.argon.commons.util.SynapseConstants;

@ApplicationScoped
public class AutoCommsDashboardDaoBean implements AutoCommsDashboardDao {
	@Inject
	@DataSource("oracleDs")
	javax.sql.DataSource synapseDatasource;

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
	public SearchTempateIDs getTemplateIDs(SearchTempateIDs templateIDs) throws HttpException {
		String query = "SELECT template_id FROM scribe.template_recipient_lookup WHERE template_id IN (" + AutoCommsUtil.getLineOfQs(templateIDs.getTemplateIds().size()) + ")";
		return getTemplateIDs(query, templateIDs);
	}

	@Override
	public int[] addTemplateRecipientLookupInfo(RecipientsLookup recipients) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] updateTemplateRecipientLookupInfo(RecipientsLookup recipients) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] deleteTemplateRecipientLookupInfo(SearchTempateIDs templateIDs) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] templateRecipientLookupAuditTrail(AutoCommsAudits autoCommsAudits) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] reverseAuditTrail(SearchTempateIDs autoCommsIDs) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutoCommsAudits getValuesBeforeUpdate(SearchTempateIDs autoCommsIDs) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecipientsLookupTIA getTemplateRecipientLookupTIAInfo() throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateTemplateRecipientLookupTIAInfo(RecipientsLookupTIA recipients) throws HttpException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AutoCommsAudits getAuditTrailInfo(String searchID, Integer pagesize, Integer offset) throws HttpException {
		// TODO Auto-generated method stub
		return null;
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

	private RecipientsLookup getTemplateRecipientLookup(String query, int num, SearchTempateIDs templateIDs) {
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
            //throw new HttpException(syn_err,Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return recipients;
	}

	private SearchTempateIDs getTemplateIDs(String query, int num, SearchTempateIDs templateIDs) {
		try(Connection conn = synapseDatasource.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)){
            
            for(int i=0;i<num;i++)
                ps.setString(i+1, templateIDs.getTemplateIds().get(i));
            
            try(ResultSet rs = ps.executeQuery()) {
            	templateIDs = new SearchTempateIDs();
                while(rs.next())
                	templateIDs.getTemplateIds().add(rs.getString("TEMPLATE_ID"));
            }
			
		} catch(SQLException e) {
			String syn_err = "Failed to retrieve template Ids: " + e.getMessage();
			System.out.println(syn_err);
            //throw new HttpException(syn_err,Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		
		return templateIDs;
	}
	
	private Map<String, String> checkAttachmentPresent(String templateId, Connection conn) {
		Map<String, String> result = new HashMap<>();
		String query = "SELECT * FROM SCRIBE.ATTACHMENT_TEMPLATE WHERE NAME = ?";
		
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
			//throw new HttpException(syn_err,Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return result;
	}
	
	private Attachments getAttachement(String templateId, Connection conn) {
		Attachment att = new Attachment();
		Attachments atts = new Attachments();
		String query = "SELECT name, guid FROM SCRIBE.ATTACHMENT WHERE NAME = ?";
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
			//throw new HttpException(syn_err,Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return atts;
	}

}
