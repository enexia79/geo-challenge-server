package geochallenge

import grails.transaction.Transactional

@Transactional
class AuthService {
	def grailsApplication
	
	public static final String ERROR_AUTH_FAILURE = "auth_failure"
	
	/**
	 * Check if token is authorized for API use
	 * @param token authentication token
	 * @return boolean true or false (success/failure)
	 */
    def isAuthorized(token) {
		return token == grailsApplication.config.app.token
    }
}
