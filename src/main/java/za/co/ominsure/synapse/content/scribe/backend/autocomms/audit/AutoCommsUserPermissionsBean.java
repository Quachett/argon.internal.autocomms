package za.co.ominsure.synapse.content.scribe.backend.autocomms.audit;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import uk.co.inc.argon.commons.exceptions.HttpException;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.dao.AutoCommsDashboardDao;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.util.AutoCommsUtil;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Error;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.ErrorMsg;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Result;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UserPermissions;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UserPermissionsResults;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UsersPermissions;

@ApplicationScoped
public class AutoCommsUserPermissionsBean implements AutoCommsUserPermissions {
	@Inject
	private AutoCommsDashboardDao dao;

	@Override
	public UserPermissionsResults deleteUserPermissionsInfo(String userId) throws HttpException {
		UserPermissionsResults upr = new UserPermissionsResults();
        UsersPermissions usp = getUserPermissionsInfo(userId);
        
        try {
            if(usp != null) {
                boolean delete = dao.deleteUserPermissionsInfo(userId);
                if(delete)
                    upr.setMessage(Result.SUCCESS);
                else
                    throw new HttpException("Failed to delete User from DB. Please Contact Synapse team", Status.EXPECTATION_FAILED.getStatusCode());
            }
            else {
                upr.setMessage(Result.FAILED);
                Error err = new Error();
                usp = new UsersPermissions();
                UserPermissions up = new UserPermissions();
                up.setUserId(userId);
                up.setPermissions(null);
                usp.getUserPermissions().add(up);
                err.setMessage(ErrorMsg.THE_FOLLOWING_USERS_COULD_NOT_BE_FOUND_IN_THE_DB.value());
                err.setUsers(usp);
                upr.getUsersNotFound().add(err);
            }
        }catch (Exception e) {
        	throw new HttpException(e.toString(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        
        return upr;
	}

	@Override
	public UserPermissionsResults updateUserPermissionsInfo(UsersPermissions usp) throws HttpException {
		UserPermissionsResults uspResults = new UserPermissionsResults();
        if(usp != null && !usp.getUserPermissions().isEmpty()) {
            Map<String, Map<String, UserPermissions>> uspMaps = userPermissionsMap(usp);
            Map<String, UserPermissions> validUspMap = uspMaps.get(AutoCommsUtil.VALID);
            Map<String, UserPermissions> invalidUspMap = uspMaps.get(AutoCommsUtil.INVALID);
            
            try {
                if(validUspMap.isEmpty()) {
                    uspResults.setMessage(Result.FAILED);
                    uspResults.getPermissionsError().add(wrongPermissions(invalidUspMap));
                }
                else {
                    uspMaps = checkIfUsersExist(validUspMap);
                    
                    Map<String, UserPermissions> updateableUspMap;
                    updateableUspMap = uspMaps.get(AutoCommsUtil.UPDATE_VALUES_MAP);
                    Map<String, UserPermissions> notFoundUspMap = uspMaps.get(AutoCommsUtil.ADD_VALUES_MAP);
                    
                    if(!updateableUspMap.isEmpty()) {
                        usp = new UsersPermissions();
                        usp.getUserPermissions().addAll(updateableUspMap.values());
                        
                        boolean update = dao.updateUserPermissionsInfo(usp);
                    	uspResults = getResults(invalidUspMap, notFoundUspMap, update);
                    }
                    else {
                        uspResults.setMessage(Result.FAILED);
                        uspResults.getUsersNotFound().add(notFoundPermissions(notFoundUspMap));
                        if(!invalidUspMap.isEmpty())
                            uspResults.getPermissionsError().add(wrongPermissions(invalidUspMap));
                    }
                }
                
            } catch (Exception e) {
            	throw new HttpException(e.toString(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }
        }
        return uspResults;
	}

	@Override
	public UserPermissionsResults addUserPermissionsInfo(UsersPermissions usp) throws HttpException {
		UserPermissionsResults uspResults = new UserPermissionsResults();
        if(usp != null && !usp.getUserPermissions().isEmpty()) {
            Map<String, Map<String, UserPermissions>> uspMaps = userPermissionsMap(usp);
            Map<String, UserPermissions> validUspMap = uspMaps.get(AutoCommsUtil.VALID);
            Map<String, UserPermissions> invalidUspMap = uspMaps.get(AutoCommsUtil.INVALID);
            
            try {
                if(validUspMap.isEmpty()) {
                    uspResults.setMessage(Result.FAILED);
                    uspResults.getPermissionsError().add(wrongPermissions(invalidUspMap));
                }
                else {
                    uspMaps = checkIfUsersExist(validUspMap);
                    
                    Map<String, UserPermissions> addableUspMap;
                    addableUspMap = uspMaps.get(AutoCommsUtil.ADD_VALUES_MAP);
                    Map<String, UserPermissions> duplicateUspMap = uspMaps.get(AutoCommsUtil.UPDATE_VALUES_MAP);
                    
                    if(!addableUspMap.isEmpty()) {
                    	usp = new UsersPermissions();
                        usp.getUserPermissions().addAll(addableUspMap.values());
                        
                        boolean added = dao.addUserPermissionsInfo(usp);
                    	uspResults = getResults(invalidUspMap, duplicateUspMap, added);
                    }
                    else {
                        uspResults.setMessage(Result.FAILED);
                        uspResults.getDuplicateUsers().add(duplicatePermissions(duplicateUspMap));
                        if(!invalidUspMap.isEmpty())
                            uspResults.getPermissionsError().add(wrongPermissions(invalidUspMap));
                    }
                }
            } catch (Exception e) {
            	throw new HttpException(e.toString(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }
        }
        else
            throw new HttpException(AutoCommsUtil.EMPTY_PAYLOAD, Status.BAD_REQUEST.getStatusCode());
        return uspResults;
	}
	
	@Override
	public UsersPermissions getUserPermissionsInfo(String userId) throws HttpException {
		return dao.getUserPermissionsInfo(userId);
	}

	@Override
	public boolean checkPermissions(String userId, String permission) throws HttpException {
		boolean allowed = false;
        String regex = "^(?!.*?(.).*?\\1)[RAWD]*$";
        
        if(Pattern.matches(regex, permission)) {
            UsersPermissions usp = getUserPermissionsInfo(userId);
            if(usp != null) {
                UserPermissions up = usp.getUserPermissions().get(0);
                
                if(StringUtils.containsIgnoreCase(up.getPermissions(), permission))
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
    
    private UserPermissionsResults getResults(Map<String, UserPermissions> invalidUspMap,
    		Map<String, UserPermissions> duplicateUspMap, boolean bool) throws HttpException {
    	UserPermissionsResults uspResults = new UserPermissionsResults();
        
        if(bool) {
            if(invalidUspMap.isEmpty() && duplicateUspMap.isEmpty())
                uspResults.setMessage(Result.SUCCESS);
            else {
                uspResults.setMessage(Result.PARTIAL_SUCCESS);
                if(!invalidUspMap.isEmpty())
                    uspResults.getPermissionsError().add(wrongPermissions(invalidUspMap));
                
                if(!duplicateUspMap.isEmpty())
                    uspResults.getDuplicateUsers().add(duplicatePermissions(duplicateUspMap));
            }
        }
        else
            throw new HttpException("Failed to add User(s) to DB. Please Contact Synapse team", Status.EXPECTATION_FAILED.getStatusCode());
    	
    	return uspResults;
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
    
    private Error wrongPermissions(Map<String, UserPermissions> invalidUspMap) {
        Error err = new Error();
        UsersPermissions usp = new UsersPermissions();
        
        usp.getUserPermissions().addAll(invalidUspMap.values());
        err.setMessage(ErrorMsg
           .ERROR_ONLY_ONE_OR_A_COMBINATION_FROM_THESE_LETTERS_IS_ALLOWED_RAWD_MAKE_SURE_EACH_LETTER_IS_ENTERED_ONLY_ONCE
           .value());
        err.setUsers(usp);
        
        return err;
    }
    
    private Error duplicatePermissions(Map<String, UserPermissions> duplicateUspMap) {
        Error err = new Error();
        UsersPermissions usp = new UsersPermissions();

        usp.getUserPermissions().addAll(duplicateUspMap.values());
        err.setMessage(ErrorMsg.THE_FOLLOWING_USERS_ARE_ALREADY_IN_THE_DB.value());
        err.setUsers(usp);
        
        return err;
    }

    private Error notFoundPermissions(Map<String, UserPermissions> notFoundUspMap) {
        Error err = new Error();
        UsersPermissions usp = new UsersPermissions();

        usp.getUserPermissions().addAll(notFoundUspMap.values());
        err.setMessage(ErrorMsg.THE_FOLLOWING_USERS_COULD_NOT_BE_FOUND_IN_THE_DB.value());
        err.setUsers(usp);
        
        return err;
    }
}
