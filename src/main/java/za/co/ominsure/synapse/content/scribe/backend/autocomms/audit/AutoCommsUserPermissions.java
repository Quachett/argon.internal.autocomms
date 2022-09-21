package za.co.ominsure.synapse.content.scribe.backend.autocomms.audit;

import uk.co.inc.argon.commons.exceptions.HttpException;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UserPermissionsResults;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UsersPermissions;

public interface AutoCommsUserPermissions {
	public String getUserFromToken(String token);
	
	public UsersPermissions getUserPermissionsInfo(String UserID);
	
	public boolean checkPermissions(String UserID, String Permission) throws HttpException;

	public UserPermissionsResults deleteUserPermissionsInfo(String userID) throws HttpException;

	public UserPermissionsResults updateUserPermissionsInfo(UsersPermissions usp) throws HttpException;

	public UserPermissionsResults addUserPermissionsInfo(UsersPermissions usp) throws HttpException;
}
