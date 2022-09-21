package za.co.ominsure.synapse.content.scribe.backend.autocomms.audit;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import uk.co.inc.argon.commons.exceptions.HttpException;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.dao.AutoCommsDashboardDao;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.util.AutoCommsUtil;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UserPermissions;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UserPermissionsResults;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UsersPermissions;

public class AutoCommsUserPermissionsBean implements AutoCommsUserPermissions {
	@Inject
	private AutoCommsDashboardDao dao;

	@Override
	public UserPermissionsResults deleteUserPermissionsInfo(String userID) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserPermissionsResults updateUserPermissionsInfo(UsersPermissions usp) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserPermissionsResults addUserPermissionsInfo(UsersPermissions usp) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public UsersPermissions getUserPermissionsInfo(String UserID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkPermissions(String UserID, String Permission) throws HttpException {
		boolean allowed = false;
        String regex = "^(?!.*?(.).*?\\1)[RAWD]*$";
        
        if(Pattern.matches(regex, Permission)) {
            System.out.println("Regex Matched");;
            UsersPermissions usp = getUserPermissionsInfo(UserID);
            if(usp != null) {
                UserPermissions up = usp.getUserPermissions().get(0);
                
                System.out.println("up.getPermissions(): " + up.getPermissions());
                if(StringUtils.containsIgnoreCase(up.getPermissions(), Permission))
                    allowed = true;
            }
        }
        
        return allowed;
	}

	@Override
	public String getUserFromToken(String token) {
		String encoddedString = StringUtils.substringAfter(token, StringUtils.SPACE);
        
        byte[] decodedBytes = Base64.getDecoder().decode(encoddedString.split("\\.")[1]);
        String decodedString = new String(decodedBytes);
        
        String user = StringUtils.substringBefore(StringUtils.substringAfter(StringUtils.strip(decodedString,"{\"}"), ":\""),"\"");
        
        return user;
	}
    
    private Map<String, Map<String, UserPermissions>> userPermissionsMap(UsersPermissions usp){
        String regex = "^(?!.*?(.).*?\\1)[RAWD]*$";
        Map<String, Map<String, UserPermissions>> uspMaps = new HashMap<>();
        Map<String, String> uspMap;
        Map<String, UserPermissions> userMap;
        Map<String, UserPermissions> addUspMap = new HashMap<>();
        Map<String, UserPermissions> errorUspMap = new HashMap<>();
        
        uspMap = usp.getUserPermissions().stream().collect(Collectors.toMap(UserPermissions::getUserId, k -> k.getPermissions()));
        uspMap.values().retainAll(uspMap.values().stream().filter(s -> s.matches(regex)).collect(Collectors.toSet()));
        
        userMap = usp.getUserPermissions().stream().collect(Collectors.toMap(UserPermissions::getUserId, k -> k));
        addUspMap.putAll(userMap);
        errorUspMap.putAll(userMap);
        addUspMap.keySet().retainAll(uspMap.keySet());
        errorUspMap.keySet().removeAll(addUspMap.keySet());
        
        uspMaps.put(AutoCommsUtil.VALID, addUspMap);
        uspMaps.put(AutoCommsUtil.INVALID, errorUspMap);
        
        return uspMaps;
    }
    
    private Map<String, Map<String, UserPermissions>> checkIfUsersExist(Map<String, UserPermissions> validUspMap) throws HttpException {
        Map<String, Map<String, UserPermissions>> uspMaps = new HashMap<>();
        Map<String, UserPermissions> absentUspMap = new HashMap<>();
        Map<String, UserPermissions> presentUspMap = new HashMap<>();
        
        absentUspMap.putAll(validUspMap);
        presentUspMap.putAll(validUspMap);
        
        UsersPermissions usp = getUserPermissionsInfo(null);
        
        List<String> userIDs = usp.getUserPermissions().stream().map(k -> k.getUserId()).collect(Collectors.toList());
        
        absentUspMap.keySet().removeAll(userIDs);
        presentUspMap.keySet().retainAll(userIDs);

        uspMaps.put(AutoCommsUtil.ADD_VALUES_MAP, absentUspMap);
        uspMaps.put(AutoCommsUtil.UPDATE_VALUES_MAP, presentUspMap);

        return uspMaps;
    }

}
