package geochallenge

import grails.converters.JSON

class AchievementController {

	public static final String ERROR_DUPLICTE_ACHIEVEMENT = "duplicate_achievement"
	
	def achievementService
	def authService
	
	/**
	 * Create achievement - i.e. add completed challenge info to a specified user
	 * @param token Application authentication token
	 * @param user User Id
	 * @param challenge Challenge Id
	 * @param content String representing challenge completed comment from User (optional)
	 * @return JSON response with success = true or false.  if true then id = <new achievement id> else error field will contain error string.
	 * 			Error codes: auth_failure, missing_user, missing_challenge, user_doesnt_exist, user_inactive, challenge_doesnt_exist, duplicate_achievement
	 */
	def create() {
		def results
		
		if(!authService.isAuthorized(params.token)) {
			results = [success: false, error: AuthService.ERROR_AUTH_FAILURE]
		}
		else if(params.user && params.challenge) {
			def user 		= User.get(params.user)
			def challenge 	= Challenge.get(params.challenge)
					
			if(user && user.isActive() && challenge) {
				if(Achievement.findByUserAndChallenge(user, challenge) == null) {
					try {
						def achievement = achievementService.create(user: user, challenge: challenge, content: params.content)
								
								results = [success: true, id: achievement.id]
					}
					catch (RuntimeException e) {
						results = [success: false, error: e.getMessage()]
					}
				}
				else
					results = [success: false, error: ERROR_DUPLICTE_ACHIEVEMENT]
			}
			else if(user && challenge)
				results = [success: false, error: UserController.ERROR_USER_INACTIVE]
			else if(challenge)
				results = [success: false, error: UserController.ERROR_USER_DOESNT_EXIST]
			else
				results = [success: false, error: ChallengeController.ERROR_CHALLENGE_DOESNT_EXIST]
			
		}
		else if(params.user == null)
			results = [success: false, error: UserController.ERROR_MISSING_USER]
		else
			results = [success: false, error: ChallengeController.ERROR_MISSING_CHALLENGE]
		
		render results as JSON
	}
	
	/**
	 * Get all achievements - i.e. completed challenges of a specified user
	 * @param token Application authentication token
	 * @param user User Id
	 * @return JSON response with success = true or false.  if true then achievements = list of Achievements else error field will contain error string.
	 * 			Error codes: auth_failure, missing_user, user_doesnt_exist, user_inactive
	 */
	def getAllByUser() {
		def results
		
		if(!authService.isAuthorized(params.token)) {
			results = [success: false, error: AuthService.ERROR_AUTH_FAILURE]
		}
		else if(params.user) {
			def user 		= User.get(params.user)
					
			if(user && user.isActive()) {
				try {
					def achievements = achievementService.getAllByUser(user)
					
					results = [success: true, achievements: achievementService.toJSON(achievements)]
				}
				catch (RuntimeException e) {
					results = [success: false, error: e.getMessage()]
				}
			}
			else if(user)
				results = [success: false, error: UserController.ERROR_USER_INACTIVE]
			else
				results = [success: false, error: UserController.ERROR_USER_DOESNT_EXIST]
			
		}
		else
			results = [success: false, error: UserController.ERROR_MISSING_USER]
		
		render results as JSON
	}
	
	/**
	 * Get all achievements - i.e. completed challenges of a specified challenge
	 * @param token Application authentication token
	 * @param challenge Challenge Id
	 * @return JSON response with success = true or false.  if true then achievements = list of Achievements else error field will contain error string.
	 * 			Error codes: auth_failure, missing_challenge, challenge_doesnt_exist
	 */
	def getAllByChallenge() {
		def results
		
		if(!authService.isAuthorized(params.token)) {
			results = [success: false, error: AuthService.ERROR_AUTH_FAILURE]
		}
		else if(params.challenge) {
			def challenge 		= Challenge.get(params.challenge)
					
			if(challenge) {
				try {
					def achievements = achievementService.getAllByChallenge(challenge)
					
					results = [success: true, achievements: achievementService.toJSON(achievements)]
				}
				catch (RuntimeException e) {
					results = [success: false, error: e.getMessage()]
				}
			}
			else
				results = [success: false, error: ChallengeController.ERROR_CHALLENGE_DOESNT_EXIST]
			
		}
		else
			results = [success: false, error: ChallengeController.ERROR_MISSING_CHALLENGE]
		
		render results as JSON
	}
}
