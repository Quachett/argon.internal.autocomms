package za.co.ominsure.synapse.content.scribe.frontend;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import za.co.ominsure.synapse.content.scribe.backend.autocomms.AutoCommsDashboardFacade;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.audit.AutoCommsAuditFacade;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.audit.AutoCommsUserPermissions;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.util.AutoCommsUtil;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsAudits;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.AutoCommsResult;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookup;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.RecipientsLookupTIA;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.Result;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.SearchTempateIDs;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UserPermissionsResults;
import za.co.ominsure.synapse.content.scribe.backend.autocomms.vo.UsersPermissions;
import uk.co.inc.argon.commons.exceptions.ErrorResponse;
import uk.co.inc.argon.commons.exceptions.HttpException;
import uk.co.inc.argon.commons.util.SynapseConstants;

@Path("/synapse/content/scribe/rest")
@Transactional
public class ScribeRest {

    @Inject
    private AutoCommsDashboardFacade facade;
    
    @Inject
    private AutoCommsUserPermissions up;
	
	@Inject
	private AutoCommsAuditFacade audit;

    private static final String READ = "R";
    private static final String ADD = "A";
    private static final String WRITE = "W";
    private static final String DELETE = "D";
    
    /**
     * [getTemplateRecipientLookup]
     * 
     * Returns an array of records from Template Recipient Lookup Table
     * 
     * @return RecipientLookup
     */
    
    @GET
    @Path("/template/recipients")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getTemplateRecipientLookup() {
        try {
            RecipientsLookup recipientsLookup = facade.getTemplateRecipientLookup(null);

            if (recipientsLookup != null)
                return Response.status(Status.OK).entity(recipientsLookup).build();
            else
                return Response.status(Status.NOT_FOUND).entity(new RecipientsLookup()).build();

        } catch (HttpException e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(e.getStatus()).entity(new ErrorResponse(e.getStatus(), e.toString())).build();
        } catch (Exception e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(500).entity(new ErrorResponse(500, e.toString())).build();
        }
    }
    
    /**
     * [getTemplateRecipientLookupByTemplateID]
     * 
     * Returns a single record from Template Recipient Lookup Table
     * 
     * @param templateId - Path Param with the templateID being searched
     * 
     * @return RecipientLookup - the Template Recipient Lookup object associated with the received templateID
     */
    
    @GET
    @Path("/template/recipients/{templateId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getTemplateRecipientLookupByTemplateID(@PathParam("templateId") String templateId) {
        

        SearchTempateIDs templateIds = new SearchTempateIDs();
        templateIds.getTemplateIds().add(templateId);
        try {
            RecipientsLookup recipientsLookup = facade.getTemplateRecipientLookup(templateIds);

            if (recipientsLookup != null)
                return Response.status(Status.OK).entity(recipientsLookup).build();
            else
                return Response.status(Status.NOT_FOUND).entity(new RecipientsLookup()).build();

        } catch (HttpException e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(e.getStatus()).entity(new ErrorResponse(e.getStatus(), e.toString())).build();
        } catch (Exception e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(500).entity(new ErrorResponse(500, e.toString())).build();
        }
    }
    
    /**
     * [updateTemplateRecipientLookup] Updates Template Lookup Entries in the Template Recipient Lookup Table
     * 
     * @param token - Authorisation Header Parameter from which the user is extracted
     * @param recipients - payload containing an array of Template Recipient Lookup Objects
     * 
     * @return AutoCommsResult - Result Object indicating degree of success and information about any failed
     *         processes
     */

    @PUT
    @Path("/template/recipients")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateTemplateRecipientLookup(RecipientsLookup recipients, @HeaderParam("Authorization") String token) {
        try {
            AutoCommsResult acr = facade.updateTemplateRecipientLookup(recipients, up.getUserFromToken(token));

            if (acr.getMessage() != null) {
                if (acr.getMessage().value().equals(Result.SUCCESS.value()))
                    return Response.status(Status.OK).entity(acr).build();
                else if (acr.getMessage().value().equals(Result.PARTIAL_SUCCESS.value()))
                    return Response.status(207).entity(acr).build();
                else
                    return Response.status(Status.NOT_FOUND).entity(acr).build();
            } else
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (HttpException e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(e.getStatus()).entity(new ErrorResponse(e.getStatus(), e.toString())).build();
        }
        catch (Exception e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(500).entity(new ErrorResponse(500, e.toString())).build(); 
        }
    }
    
    /**
     * [getTemplateRecipientLookupTIA] Returns an array of records from TIA for the Auto Comms Dashboard
     * 
     * @return RecipientsLookupTIA
     */

    @GET
    @Path("/template/tia/recipients")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTemplateRecipientLookupTIA() {
        try {
        	//Synlog.setAuditSearchFilter("Content->Scribe->getTemplateRecipientLookupTIA");
            

            RecipientsLookupTIA recipientsLookup = facade.getTemplateRecipientLookupTIA();

            if (recipientsLookup != null)
                return Response.status(Status.OK).entity(recipientsLookup).build();
            else
                return Response.status(Status.NOT_FOUND).entity(new RecipientsLookup()).build();

        } catch (HttpException e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(e.getStatus()).entity(new ErrorResponse(e.getStatus(), e.toString())).build();
        } catch (Exception e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(500).entity(new ErrorResponse(500, e.toString())).build();
        }
    }

    /**
     * [updateTemplateRecipientLookupTIA] Updates Template Lookup Entries in the Template Recipient Lookup Table
     * 
     * @param recipients - payload containing an array of Template Recipient Lookup Objects to be updated
     * @return AutoCommsResult - Result Object indicating degree of success and information about any failed
     *         processes
     */

    @PUT
    @Path("/template/tia/recipients")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateTemplateRecipientLookupTIA(@HeaderParam("Authorization") String token, RecipientsLookupTIA recipients) {
        try {
            AutoCommsResult acr = facade.updateTIARecipientLookup(recipients, up.getUserFromToken(token));

            if (acr.getMessage() != null) {
                if (acr.getMessage().value().equals(Result.SUCCESS.value()))
                    return Response.status(Status.OK).entity(acr).build();
                else if (acr.getMessage().value().equals(Result.PARTIAL_SUCCESS.value()))
                    return Response.status(207).entity(acr).build();
                else
                    return Response.status(Status.NOT_FOUND).entity(acr).build();
            } else
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (HttpException e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            
            return Response.status(e.getStatus())
                .entity(new ErrorResponse(e.getStatus(), e.toString())).build();
        }
        catch (Exception e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(500).entity(new ErrorResponse(500, e.toString())).build(); 
        }
    }

    /**
     * [getAutoCommsAuditTrailSYN] the entire audit trail for a given Comms ID
     * 
     * @param templateId - Query Param with the templateID being searched
     * @param pagesize - number of transaction per offset
     * @param offset - page number
     * @return AutoCommsAudits - An Array of the Audit trail
     */

    @GET
    @Path("/template/recipients/audittrail")
    @Produces({
               MediaType.APPLICATION_JSON , MediaType.APPLICATION_XML
    })
    public Response getAutoCommsAuditTrailSYN(@DefaultValue("100") @QueryParam(SynapseConstants.PAGESIZE) Integer pagesize,
                                           @DefaultValue("0") @QueryParam(SynapseConstants.OFFSET) Integer offset,
                                           @QueryParam("templateId") String templateId) {
        try {
            AutoCommsAudits acas = audit.getAutoCommsAuditTrail(AutoCommsUtil.SYN,templateId,pagesize,offset);

            if (acas != null)
                return Response.status(Status.OK).entity(acas).build();
            else
                return Response.status(Status.NOT_FOUND).entity(new AutoCommsAudits()).build();

        } catch (HttpException e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(e.getStatus()).entity(new ErrorResponse(e.getStatus(), e.toString())).build();
        } catch (Exception e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(500).entity(new ErrorResponse(500, e.toString())).build();
        }
    }

    /**
     * [getAutoCommsAuditTrailTIA] the entire audit trail for a given Comms ID
     * 
     * @param sectionId - Query Param with the SectionID That forms part of the unique key for TIA AutoComms
     * @param language - Query Param with the language That forms part of the unique key for TIA AutoComms
     * @param pagesize - number of transaction per offset
     * @param offset - page number
     * @return AutoCommsAudits - An Array of the Audit trail
     */

    @GET
    @Path("/template/tia/recipients/audittrail")
    @Produces({
               MediaType.APPLICATION_JSON , MediaType.APPLICATION_XML
    })
    public Response getAutoCommsAuditTrailTIA(@DefaultValue("100") @QueryParam(SynapseConstants.PAGESIZE) Integer pagesize,
                                           @DefaultValue("0") @QueryParam(SynapseConstants.OFFSET) Integer offset,
                                           @QueryParam("sectionId") int sectionId, @QueryParam("language") String lang) {
    	
        try {
            AutoCommsAudits acas = audit.getTIAAutoCommsAuditTrail(AutoCommsUtil.TIA,sectionId,lang,pagesize,offset);

            if (acas != null)
                return Response.status(Status.OK).entity(acas).build();
            else
                return Response.status(Status.NOT_FOUND).entity(new AutoCommsAudits()).build();

        } catch (HttpException e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(e.getStatus()).entity(new ErrorResponse(e.getStatus(), e.toString())).build();
        } catch (Exception e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(500).entity(new ErrorResponse(500, e.toString())).build();
        }
    }
    
    /**
     * [getUserPermisions] Returns an array of records from TIA for the Auto Comms Dashboard
     * 
     * @param token - Authorisation Header Parameter from which permission to perform action will be validated
     * @return UsersPermissions - An array of User Permission records
     */

    @GET
    @Path("/template/recipients/users")
    @Produces({
               MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    public Response getUserPermisions(@HeaderParam("Authorization") String token) {
        try {
            if(up.checkPermissions(up.getUserFromToken(token), READ)) {
    
                UsersPermissions userPermissions = up.getUserPermissionsInfo(null);
    
                if (userPermissions != null)
                    return Response.status(Status.OK).entity(userPermissions).build();
                else
                    return Response.status(Status.NOT_FOUND).entity(new UsersPermissions()).build();
            }
            else
                return Response.status(Status.UNAUTHORIZED.getStatusCode())
                    .entity(new ErrorResponse(Status.UNAUTHORIZED.getStatusCode(),"User Restricted from viewing this content")).build();

        } catch (HttpException e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(e.getStatus()).entity(new ErrorResponse(e.getStatus(), e.toString())).build();
        } catch (Exception e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(500).entity(new ErrorResponse(500, e.toString())).build();
        }
    }
    
    /**
     * [getUserPermisions] Returns an array of records from TIA for the Auto Comms Dashboard
     * 
     * @param token - Authorisation Header Parameter from which permission to perform action will be validated
     * @param userId - String Path Param with the userID to be fetched
     * @return UsersPermissions - An array of User Permission records
     */

    @GET
    @Path("/template/recipients/users/{userId}")
    @Produces({
               MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    public Response getUserPermisionsById(@HeaderParam("Authorization") String token,
                                            @PathParam("userId") String userID) {
        try {
            if(up.checkPermissions(up.getUserFromToken(token), READ)) {
    
                UsersPermissions userPermissions = up.getUserPermissionsInfo(userID);
    
                if (userPermissions != null)
                    return Response.status(Status.OK).entity(userPermissions).build();
                else
                    return Response.status(Status.NOT_FOUND).entity(new UsersPermissions()).build();
            }
            else
                return Response.status(Status.UNAUTHORIZED.getStatusCode())
                    .entity(new ErrorResponse(Status.UNAUTHORIZED.getStatusCode(),"User Restricted from viewing this content")).build();

        } catch (HttpException e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(e.getStatus()).entity(new ErrorResponse(e.getStatus(), e.toString())).build();
        } catch (Exception e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(500).entity(new ErrorResponse(500, e.toString())).build();
        }
    }

    /**
     * [addUserPermissions] Create new User Permissions Entries in the Userpermissions Table
     * 
     * @param token - Authorisation Header Parameter from which permission to perform action will be validated
     * @param UsersPermissions - payload containing an array of Users and their permissions to be updated
     * @return UserPermissionsResult - Result Object indicating degree of success and information about any failed
     *         processes
     */

    @POST
    @Path("/template/recipients/users")
    @Produces({
               MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Consumes({
               MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    public Response addUserPermissions(@HeaderParam("Authorization") String token, UsersPermissions userPermissions) {
        try {
            if(up.checkPermissions(up.getUserFromToken(token), ADD)) {
    
                UserPermissionsResults upr = up.addUserPermissionsInfo(userPermissions);
    
                if (upr.getMessage() != null) {
                    if (upr.getMessage().value().equals(Result.SUCCESS.value()))
                        return Response.status(Status.CREATED).entity(upr).build();
                    else if (upr.getMessage().value().equals(Result.PARTIAL_SUCCESS.value()))
                        return Response.status(207).entity(upr).build();
                    else
                        return Response.status(Status.CONFLICT).entity(upr).build();
                } else
                    return Response.status(Status.SERVICE_UNAVAILABLE).entity(upr).build();
            }
            else
                return Response.status(Status.UNAUTHORIZED.getStatusCode())
                    .entity(new ErrorResponse(Status.UNAUTHORIZED.getStatusCode(),"User Restricted from ADDING new content")).build();
        } catch (HttpException e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(e.getStatus()).entity(new ErrorResponse(e.getStatus(), e.toString())).build();
        }
        catch (Exception e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(500).entity(new ErrorResponse(500, e.toString())).build(); 
        }
    }

    /**
     * [updateUserPermissions] Update records in the User Permissions table
     * 
     * @param token - Authorisation Header Parameter from which permission to perform action will be validated
     * @param UsersPermissions - payload containing an array of Users and their permissions to be updated
     * @return UserPermissionsResult - Result Object indicating degree of success and information about any failed
     *         processes
     */

    @PUT
    @Path("/template/recipients/users")
    @Produces({
               MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Consumes({
               MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    public Response updateUserPermissions(@HeaderParam("Authorization") String token, UsersPermissions userPermissions) {
        try {
            if(up.checkPermissions(up.getUserFromToken(token), WRITE)) {
    
                UserPermissionsResults upr = up.updateUserPermissionsInfo(userPermissions);
    
                if (upr.getMessage() != null) {
                    if (upr.getMessage().value().equals(Result.SUCCESS.value()))
                        return Response.status(Status.CREATED).entity(upr).build();
                    else if (upr.getMessage().value().equals(Result.PARTIAL_SUCCESS.value()))
                        return Response.status(207).entity(upr).build();
                    else
                        return Response.status(Status.CONFLICT).entity(upr).build();
                } else
                    return Response.status(Status.SERVICE_UNAVAILABLE).entity(upr).build();
            }
            else
                return Response.status(Status.UNAUTHORIZED.getStatusCode())
                    .entity(new ErrorResponse(Status.UNAUTHORIZED.getStatusCode(),"User Restricted from UPDATING this content")).build();
        } catch (HttpException e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(e.getStatus()).entity(new ErrorResponse(e.getStatus(), e.toString())).build();
        }
        catch (Exception e) {
            //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
            return Response.status(500).entity(new ErrorResponse(500, e.toString())).build(); 
        }
    }

    /**
     * [deleteUserPermissions] Remove a single User record from the Permissions table Table
     * 
     * @param token - Authorisation Header Parameter from which permission to perform action will be validated
     * @param userId - String Path Param with the userID to be deleted
     * @return UserPermissionsResult - Result Object indicating degree of success and information about any failed
     *         processes
     */

    @DELETE
    @Path("/template/recipients/users/{userId}")
    @Produces({
               MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    public Response deleteUserPermissions(@HeaderParam("Authorization") String token,
                                                  @PathParam("userId") String userID) {

            UserPermissionsResults upr;
            try {
                if(up.checkPermissions(up.getUserFromToken(token), DELETE)) {
                    upr = up.deleteUserPermissionsInfo(userID);
    
                    if (upr != null) {
                        if (upr.getMessage().value().equals(Result.SUCCESS.value()))
                            return Response.status(Status.OK).entity(upr).build();
                        else if (upr.getMessage().value().equals(Result.PARTIAL_SUCCESS.value()))
                            return Response.status(207).entity(upr).build();
                        else
                            return Response.status(Status.NOT_FOUND).entity(upr).build();
                    } else
                        return Response.status(Status.NOT_FOUND).build();
                }
                else
                    return Response.status(Status.UNAUTHORIZED.getStatusCode())
                        .entity(new ErrorResponse(Status.UNAUTHORIZED.getStatusCode(),"User Restricted from DELETING this content")).build();
            } catch (HttpException e) {
                //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
                return Response.status(e.getStatus()).entity(new ErrorResponse(e.getStatus(), e.toString())).build();
            }
            catch (Exception e) {
                //Synlog.error(e.toString(), ExceptionUtil.getStackTrace(e));
                return Response.status(500).entity(new ErrorResponse(500, e.toString())).build(); 
            }

    }
}