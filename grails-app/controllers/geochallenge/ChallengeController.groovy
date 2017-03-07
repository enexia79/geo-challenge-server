package geochallenge

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONException
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.web.multipart.MultipartHttpServletRequest

class ChallengeController {
	public static final String ERROR_MISSING_USER					= "missing_user"
	public static final String ERROR_MISSING_CHALLENGE				= "missing_challenge"
	public static final String ERROR_USER_DOESNT_EXIST				= "user_doesnt_exist"
	public static final String ERROR_INVALID_JSON					= "invalid_json"
	public static final String ERROR_MISSING_TITLE					= "missing_title"
	public static final String ERROR_EXPIRES_INVALID				= "expires_not_long_number"
	public static final String ERROR_MISSING_POINTS					= "missing_points"
	public static final String ERROR_MISSING_POINT					= "missing_point"
	public static final String ERROR_POINT_GPS_INVALID				= "point_gps_invalid"
	public static final String ERROR_CHALLENGE_DOESNT_EXIST			= "challenge_doesnt_exist"
	public static final String ERROR_CHALLENGE_HAS_ACHIEVEMENT		= "challenge_has_achievement"
	public static final String ERROR_POINT_DOESNT_EXIST				= "point_doesnt_exist"
	
	def challengeService
	def authService
	
	/**
	 * Create challenge
	 * @param token Application authentication token
	 * @param user User Id
	 * @param challenge JSON representing challenge info example : {title: "title", description: "description", expires: 1234567890, 
	 * 			points: [{title: "pointA Title", longitude: 25.2, latitude: 12.5, content: "Eat my Shorts"}, 
	 * 				{title: "PointB Title", longitude: 12.0, latitude: 11.0},
	 * 				{title: "PointC Title", longitude: 8.0, latitude: 11.65, content: "The end"}]}
	 * @return JSON response with success = true or false.  if true then id = <new challenge id> else error field will contain error string.  
	 * 			Error codes: auth_failure, missing_user, missing_challenge, user_doesnt_exist, invalid_json, missing_title, expires_not_long_number, missing_points, point_gps_invalid
	 */
    def create() {
		def challenge
		def results
		
		if(!authService.isAuthorized(params.token)) {
			results = [success: false, error: AuthService.ERROR_AUTH_FAILURE]
		}
		else if(params.user && params.challenge) {
			def user = User.get(params.user)
					
			if(user) {
				try {
					def challengeInfo = new JSONObject(params.challenge)
					if(challengeInfo.title == null || challengeInfo.title.trim().isEmpty())
						results = [success: false, error: ERROR_MISSING_TITLE]
					else {
						if(challengeInfo.expires && !(challengeInfo.expires instanceof Long)) {
							results = [success: false, error: ERROR_EXPIRES_INVALID]
						}
						else {
							if(challengeInfo.points == null || !(challengeInfo.points instanceof JSONArray) || challengeInfo.points.size() == 0)
								results = [success: false, error: ERROR_MISSING_POINTS]
							else {
								challengeInfo.points.each { point ->
									if(results == null) {
										def type = point.type ? ContentType.valueOfCode(point.type) : null
										if(point.longitude == null || point.latitude == null || !(point.longitude instanceof Double || point.longitude instanceof Integer) || 
												!(point.latitude instanceof Double || point.latitude instanceof Integer))
											results = [success: false, error: ERROR_POINT_GPS_INVALID]
									}
								}
								if(results == null) {
									def expires = challengeInfo.expires ? new Date(challengeInfo.expires) : null
									challenge = challengeService.create([title: challengeInfo.title, description: challengeInfo.description, expires: expires, user: user], challengeInfo.points)
									results = [success: true, id: challenge.id]
								}
							}
						}
					}
				}
				catch (JSONException e) {
					results = [success: false, error: ERROR_INVALID_JSON]
				}
				catch (RuntimeException e) {
					results = [success: false, error: e.getMessage()]
				}
			}
			else
				results = [success: false, error: ERROR_USER_DOESNT_EXIST]
			
		}
		else if(params.user == null) {
			results = [success: false, error: ERROR_MISSING_USER]
		}
		else {
			results = [success: false, error: ERROR_MISSING_CHALLENGE]
		}
		
		render results as JSON
	}
	
	/**
	 * get challenge info
	 * @param token Application authentication token
	 * @param challenge Challenge Id
	 * @return JSON response with success = true or false.  if true, challenge = challenge JSON info.  if false, error field will contain error string.
	 * 			Error codes: auth_failure, missing_challenge, challenge_doesnt_exist
	 */
	def get() {
		def challenge
		def results
		
		if(!authService.isAuthorized(params.token)) {
			results = [success: false, error: AuthService.ERROR_AUTH_FAILURE]
		}
		else if(params.challenge) {
			challenge = Challenge.get(params.challenge) 
			if(challenge)
				results = [success: true, challenge: challengeService.toJSON(challenge)]
			else
				results = [success: false, error: ERROR_CHALLENGE_DOESNT_EXIST]
		}
		else
			results = [success: false, error: ERROR_MISSING_CHALLENGE]
		
		render results as JSON
	}

	/**
	 * Delete challenge
	 * @param token Application authentication token
	 * @param challenge Challenge Id
	 * @return JSON response with success = true or false.  if false, error field will contain error string.
	 * 			Error codes: auth_failure, missing_challenge, challenge_doesnt_exist, challenge_has_achievement
	 */
	def delete() {
		def results
		
		if(!authService.isAuthorized(params.token)) {
			results = [success: false, error: AuthService.ERROR_AUTH_FAILURE]
		}
		else if(params.challenge){
			def challenge = Challenge.get(params.challenge)
			if(challenge) {
				if(Achievement.findByChallenge(challenge))
					results = [success: false, error: ERROR_CHALLENGE_HAS_ACHIEVEMENT]
				else {
					challenge.delete()
					results = [success: true]
				}
			}
			else
				results = [success: false, error: ERROR_CHALLENGE_DOESNT_EXIST]
		}
		else
			results = [success: false, error: ERROR_MISSING_CHALLENGE]
		
		render results as JSON
	}	
}