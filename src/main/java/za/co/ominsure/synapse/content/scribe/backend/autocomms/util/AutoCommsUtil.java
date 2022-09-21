package za.co.ominsure.synapse.content.scribe.backend.autocomms.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AutoCommsUtil {

    public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String EMPTY_PAYLOAD = "Payload Cannot be Empty or Null ";
    public static final String ID_ERROR = "TemplateID can not be empty or null ";
    public static final String DB_ERROR = "Failed to perform DB operation with unknown error ";
    public static final String DB_AUDIT_ERROR = "Failed to audit changes ";
    public static final String DB_REVERT_FAILED = "Failed to clean up Audit Trail after DB Failure, Please Contact Synapse Admin for further help ";
    public static final String IDS_MAP = "IDs";
    public static final String ELIGABLE_VALUES_MAP = "Values on which CRUD operation can be performed without resulting in an error";
    public static final String ADD_VALUES_MAP = "Non-Duplicate values That can be added to the database";
    public static final String UPDATE_VALUES_MAP = "Values that are in the Database and can thus be updated";
    public static final String OLD_VALUES_MAP = "Values containing pre-update/delete values";
    public static final String VALID = "valid user permissions";
    public static final String INVALID = "invalid user permissions";
    public static final String NO_REASON_IDS = "Update request with no reason given for the update";
    public static final String SEC_ID = "SECTION_ID";
	public static final String LANGUAGE = "LANGUAGE";
	public static final String BROKER = "SEND_TO_BROKER";
	public static final String CLIENT = "SEND_TO_CLIENT";
	public static final String PRIMARY_CONTACT = "SEND_TO_PRIMARY_CONTACT";
	public static final String ASSESSMENT_CONTACT = "SEND_TO_ASSESSMENT_CONTACT";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String TEXT = "TEXT";
	public static final String IS_ATT_PRESENT = "IS_ATT_PRESENT";
	public static final String GUID = "GUID";
	public static final String NAME = "NAME";
	public static final String ATT_NAME = "ATTACHMENT_NAME";
	public static final String SYN = "SYNAPSE";
	public static final String TIA = "TIA";
	
	public static final Gson gson = new GsonBuilder()
			.setDateFormat(ISO_8601_DATE_FORMAT)
			.setPrettyPrinting().create();

    public static String getLineOfQs(int num) {
        return Joiner.on(", ").join(Iterables.limit(Iterables.cycle("?"), num));
    }
}
