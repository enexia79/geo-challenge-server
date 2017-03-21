package geochallenge

import javax.validation.constraints.Max

import grails.converters.JSON

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONException
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.web.multipart.MultipartHttpServletRequest

class ChallengeController {
	public static final String ERROR_CHALLENGE_DOESNT_EXIST			= "challenge_doesnt_exist"
	public static final String ERROR_CHALLENGE_HAS_ACHIEVEMENT		= "challenge_has_achievement"
	public static final String ERROR_EXPIRES_INVALID				= "expires_not_long_number"
	public static final String ERROR_INVALID_JSON					= "invalid_json"
	public static final String ERROR_MAX_NOT_INTEGER				= "max_not_integer"
	public static final String ERROR_MAX_LIMIT_EXCEEDED				= "max_limit_exceeded"
	public static final String ERROR_MISSING_CHALLENGE				= "missing_challenge"
	public static final String ERROR_MISSING_LOCATION_PARAMETER		= "missing_location_parameter"
	public static final String ERROR_MISSING_POINT					= "missing_point"
	public static final String ERROR_MISSING_POINTS					= "missing_points"
	public static final String ERROR_MISSING_TITLE					= "missing_title"
	public static final String ERROR_MISSING_USER					= "missing_user"
	public static final String ERROR_POINT_DOESNT_EXIST				= "point_doesnt_exist"
	public static final String ERROR_POINT_GPS_INVALID				= "point_gps_invalid"
	public static final String ERROR_UNKNOWN_SORT_TYPE				= "unknown_sort_type"
	public static final String ERROR_USER_DOESNT_EXIST				= "user_doesnt_exist"
	public static final String ERROR_USER_INACTIVE					= "user_inactive"
	
	public static final Integer	DEFAULT_MAX							= 100;
	public static final Integer MAX_RESULTS							= 1000;
	
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
	 * 			Error codes: auth_failure, missing_user, missing_challenge, user_doesnt_exist, invalid_json, missing_title, expires_not_long_number, missing_points, point_gps_invalid, user_inactive
	 */
    def create() {
		def challenge
		def results
		
		if(!authService.isAuthorized(params.token)) {
			results = [success: false, error: AuthService.ERROR_AUTH_FAILURE]
		}
		else if(params.user && params.challenge) {
			def user = User.get(params.user)
					
			if(user && user.isActive()) {
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
			else if(user)
				results = [success: false, error: ERROR_USER_INACTIVE]
			else
				results = [success: false, error: ERROR_USER_DOESNT_EXIST]
			
		}
		else if(params.user == null)
			results = [success: false, error: ERROR_MISSING_USER]
		else
			results = [success: false, error: ERROR_MISSING_CHALLENGE]
		
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
	 * 			Error codes: auth_failure, missing_challenge, challenge_doesnt_exist, challenge_has_achievement, user_inactive
	 */
	def delete() {
		def results
		
		if(!authService.isAuthorized(params.token)) {
			results = [success: false, error: AuthService.ERROR_AUTH_FAILURE]
		}
		else if(params.challenge) {
			def challenge = Challenge.get(params.challenge)
			if(challenge && challenge.user.isActive()) {
				if(Achievement.findByChallenge(challenge))
					results = [success: false, error: ERROR_CHALLENGE_HAS_ACHIEVEMENT]
				else {
					challenge.delete()
					results = [success: true]
				}
			}
			else if(challenge)
				results = [success: false, error: ERROR_USER_INACTIVE]
			else
				results = [success: false, error: ERROR_CHALLENGE_DOESNT_EXIST]
		}
		else
			results = [success: false, error: ERROR_MISSING_CHALLENGE]
		
		render results as JSON
	}	
	
	/**
	 * search challenges
	 * @param token Application authentication token
	 * @param user User search i.e. Challenges owned by this user (optional)
	 * @param latitude (gps coord of current location of requester) (optional, required if specifying gps location)
	 * @param longitude (gps coord of current location of requester) (optional, required if specifying gps location)
	 * @param radius (max distance from requester in meters) (optional, required if specifying gps location)
	 * @param sort ("popular" or "nearby") (optional - defaults to popular, if "nearby" is selected than gps location parameters are required)
	 * @param max (max number of results to be returned) (<= 1000 - optional, defaults to 100)
	 * @return JSON response with success = true or false.  if true, challenges = challenges JSON info.  if false, error field will contain error string.
	 * 			Error codes: auth_failure, max_limit_exceeded, missing_location_parameter, unknown_sort_type, user_doesnt_exist, max_not_integer, point_gps_invalid
	 */
	def search() {
		def challenges
		def results
		
		if(!authService.isAuthorized(params.token)) {
			results = [success: false, error: AuthService.ERROR_AUTH_FAILURE]
		}
		// check if missing any one of the location params when at least one is specified
		else if((params.latitude || params.longitude || params.radius) && !(params.latitude && params.longitude && params.radius)) {
			results = [success: false, error: ERROR_MISSING_LOCATION_PARAMETER]
		}
		else if(params.sort && params.sort == ChallengeService.SORT_NEARBY && !(params.latitude && params.longitude && params.radius)) {
			results = [success: false, error: ERROR_MISSING_LOCATION_PARAMETER]
		}
		else if(params.sort && params.sort != ChallengeService.SORT_NEARBY && params.sort != ChallengeService.SORT_POPULAR) {
			results = [success: false, error: ERROR_UNKNOWN_SORT_TYPE]
		}
		else if(params.user && User.get(params.user) == null) {
			results = [success: false, error: ERROR_USER_DOESNT_EXIST]
		}
		else if(params.max && params.int('max') == null) {
			results = [success: false, error: ERROR_MAX_NOT_INTEGER]
		}
		else if(params.max && params.int('max') > MAX_RESULTS) {
			results = [success: false, error: ERROR_MAX_LIMIT_EXCEEDED]
		}
		else if(params.radius && (!params.latitude.isBigDecimal() || !params.longitude.isBigDecimal() || !params.radius.isBigDecimal())) {
			results = [success: false, error: ERROR_POINT_GPS_INVALID]
		}
		else {
			def user		= params.user ? User.get(params.user) : null
			def latitude	= params.latitude?.toBigDecimal()
			def longitude	= params.longitude?.toBigDecimal()
			def radius		= params.radius?.toBigDecimal()
			def max			= params.max ? params.int('max') : DEFAULT_MAX
			
			challenges = challengeService.search(user: user, latitude: latitude, longitude: longitude, radius: radius, sort: params.sort, max: max)
			
			results = [success: true, challenges: challengeService.toJSON(challenges)]
		}
		
		render results as JSON
	}
}
