package geochallenge

import grails.converters.JSON

class UserController {
	public static final String ERROR_DUPLICATE_SURROGATE_ID 		= "duplicate_surrogateId"
	public static final String ERROR_MISSING_NAME 					= "missing_name"
	public static final String ERROR_MISSING_SURROGATE_ID			= "missing_surrogateId"
	public static final String ERROR_MISSING_USER					= "missing_user"
	public static final String ERROR_SURROGATE_ID_DOES_NOT_EXIST 	= "surrogateId_doesnt_exist"
	public static final String ERROR_USER_DOESNT_EXIST				= "user_doesnt_exist"
	public static final String ERROR_USER_INACTIVE					= "user_inactive"
	
	def userService
	def authService
	
	/**
	 * Create user account
	 * @param token Application authentication token
	 * @param name Display Name for User
	 * @param surrogateId	Unique Social Id for this user i.e. facebook or google id
	 * @return JSON response with success = true or false.  if true then id = <new user id> else error field will contain error string.  Error codes: auth_failure, duplicate_surrogateId, missing_name & missing_surrogateId
	 */
	def create() {
		def user
		def results
		
		if(!authService.isAuthorized(params.token)) {
			results = [success: false, error: AuthService.ERROR_AUTH_FAILURE]
		}
		else if(params.name && !params.name.trim().isEmpty() && params.surrogateId) {
			user = userService.create(name: params.name, surrogateId: params.surrogateId)
					
			if(user)
				results = [success: true, id: user.id]
			else
				results = [success: false, error: ERROR_DUPLICATE_SURROGATE_ID]
			
		}
		else if(params.surrogateId == null || params.surrogateId.isEmpty()) {
			results = [success: false, error: ERROR_MISSING_SURROGATE_ID]
		}
		else {
			results = [success: false, error: ERROR_MISSING_NAME]
		}
		
		render results as JSON
	}
	
	/**
	 * Get the user Id given a surrogateId
	 * @param token Application authentication token
	 * @param surrogateId	Unique Social Id for this user i.e. facebook or google id
	 * @return JSON response with success = true or false.  if true then id = <new user id> else error field will contain error string. Error codes: auth_failure & surrogateId_doesnt_exist
	 */
	def getId() {
		def id
		def results
		
		if(!authService.isAuthorized(params.token))
			results = [success: false, error: AuthService.ERROR_AUTH_FAILURE]
		else if(params.surrogateId) {
			id = userService.getIdBySurrogateId(params.surrogateId)
					
			if(id)
				results = [success: true, id: id]
			else
				results = [success: false, error: ERROR_SURROGATE_ID_DOES_NOT_EXIST]
			
		}
		else
			results = [success: false, error: ERROR_MISSING_SURROGATE_ID]
		
		render results as JSON
	}
	
	/**
	 * Get the user info given id
	 * @param token Application authentication token
	 * @param user	User Id
	 * @return JSON response with success = true or false.  if true then user = user info else error field will contain error string. Error codes: auth_failure, missing_user, user_doesnt_exist
	 */
	def get() {
		def user
		def results
		
		if(!authService.isAuthorized(params.token))
			results = [success: false, error: AuthService.ERROR_AUTH_FAILURE]
		else if(params.user) {
			user = User.get(params.user)
					
			if(user)
				results = [success: true, user: userService.toJSON(user)]
			else
				results = [success: false, error: ERROR_USER_DOESNT_EXIST]
			
		}
		else
			results = [success: false, error: ERROR_MISSING_USER]
		
		render results as JSON
	}
	
	/**
	 * Deactivate or Activate User given user Id
	 * @param token Application authentication token
	 * @param user	User Id
	 * @return JSON response with success = true or false.  if true then active = <new state (true or false)> info else error field will contain error string. Error codes: auth_failure, missing_user, user_doesnt_exist
	 */
	def toggleActive() {
		def user
		def results
		
		if(!authService.isAuthorized(params.token))
			results = [success: false, error: AuthService.ERROR_AUTH_FAILURE]
		else if(params.user) {
			user = User.get(params.user)
					
			if(user) {
				userService.toggleActive(user)
				
				results = [success: true, active: User.get(params.user).isActive()]
			}
			else
				results = [success: false, error: ERROR_USER_DOESNT_EXIST]
			
		}
		else
			results = [success: false, error: ERROR_MISSING_USER]
		
		render results as JSON
	}
}
