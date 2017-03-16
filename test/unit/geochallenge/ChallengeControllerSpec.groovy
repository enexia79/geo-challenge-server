package geochallenge

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ChallengeController)
@Mock([User, Challenge, ChallengeService, AuthService, Achievement])
class ChallengeControllerSpec extends Specification {

	private final String TOKEN = "geo-ninjas"
	
    def setup() {
		def user = new User(name: 'david', surrogateId: 'enexia@gmail.com')
		
		user.save()
		
		def service = mockFor(AuthService)
		service.demand.isAuthorized {
			String token ->
			return token == TOKEN
		}
		controller.authService = service.createMock()
		service = mockFor(ChallengeService)
		service.demand.create {
			def info, pointsInfo ->
			info.latitude = pointsInfo[0].latitude
			info.longitude = pointsInfo[0].longitude
			def challenge = new Challenge(info)
			challenge.save()
			
			return challenge
		}
		service.demand.search(0..1) {
			def searchCriteria ->
			return []
		}
		service.demand.toJSON {
			def challenge ->
			return [title: "mocked"]
		}
		controller.challengeService = service.createMock()
    }

    def cleanup() {
    }

    void "test create"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			params.user = "1"
			params.token = TOKEN
			params.challenge = '{title: "title", expires: 1488907050430, points: [{longitude: 3.0, latitude: 2.5, content: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 1
			response.json.success == true
			response.json.id != null
    }
	
	void "test create invalid token"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			params.user = "1"
			params.token = "other"
			params.challenge = '{title: "title", points: [{longitude: 3.0, latitude: 2.5, content: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == AuthService.ERROR_AUTH_FAILURE
	}
	
	void "test create missing title"() {
		when:
			params.user = "1"
			params.token = TOKEN
			params.challenge = '{title: "", points: [{longitude: 3.0, latitude: 2.5, content: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_MISSING_TITLE
	}
	
	void "test create invalid expires"() {
		when:
			params.user = "1"
			params.token = TOKEN
			params.challenge = '{title: "title", points: [{longitude: 3, latitude: 2.5, content: "Testing"}], expires: "hi"}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_EXPIRES_INVALID
	}
	
	void "test create missing points"() {
		when:
			params.user = "1"
			params.token = TOKEN
			params.challenge = '{title: "title", points: []}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_MISSING_POINTS
	}
	
	void "test create invalid gps"() {
		when:
			params.user = "1"
			params.token = TOKEN
			params.challenge = '{title: "title", expires: 1488907050430, points: [{longitude: "not", latitude: 2.5, content: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_POINT_GPS_INVALID
	}
	
	void "test create invalid json"() {
		when:
			params.user = "1"
			params.token = TOKEN
			params.challenge = '{title: "title",invalidEntry: ,expires: 1488907050430, points: [{longitude: 3.0, latitude: 2.5, type: "text", data: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_INVALID_JSON
	}
	
	void "test create user doesnt exist"() {
		when:
			params.user = "2"
			params.token = TOKEN
			params.challenge = '{title: "title", expires: 1488907050430, points: [{longitude: 3.0, latitude: 2.5, content: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_USER_DOESNT_EXIST
	}
	
	void "test create missing user"() {
		when:
			params.token = TOKEN
			params.challenge = '{title: "title", expires: 1488907050430, points: [{longitude: 3.0, latitude: 2.5, content: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_MISSING_USER
	}
	
	void "test create missing challenge"() {
		when:
			params.user = "1"
			params.token = TOKEN
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_MISSING_CHALLENGE
	}
	
	void "test get"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.challenge = Challenge.findByTitle("test").id.toString()
			params.token = TOKEN
			controller.get()
		then:
			response.json.success == true
			response.json.challenge != null
	}
	
	void "test get unauthorized"() {
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.challenge = Challenge.findByTitle("test").id.toString()
			params.token = "other"
			controller.get()
		then:
			response.json.success == false
			response.json.challenge == null
			response.json.error == AuthService.ERROR_AUTH_FAILURE
	}
	
	void "test get challenge doesnt exist"() {
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.challenge = "1000"
			params.token = TOKEN
			controller.get()
		then:
			response.json.success == false
			response.json.challenge == null
			response.json.error == controller.ERROR_CHALLENGE_DOESNT_EXIST
	}
	
	void "test get missing challenge"() {
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.token = TOKEN
			controller.get()
		then:
			response.json.success == false
			response.json.challenge == null
			response.json.error == controller.ERROR_MISSING_CHALLENGE
	}
	
	void "test delete"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "deleteMe", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.challenge = Challenge.findByTitle("deleteMe").id.toString()
			params.token = TOKEN
			controller.delete()
		then:
			Challenge.count() == 0
			response.json.success == true
	}
	
	void "test delete unauthorized"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "deleteMe", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.challenge = Challenge.findByTitle("deleteMe").id.toString()
			params.token = "other"
			controller.delete()
		then:
			Challenge.count() == 1
			response.json.success == false
			response.json.error == AuthService.ERROR_AUTH_FAILURE
	}
	
	void "test delete has achievement"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			Achievement.count() == 0
			
		when:
			controller.challengeService.create([title: "deleteMe", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
			def achievement = new Achievement(user: User.get(1), challenge: Challenge.get(1))
			achievement.save()
		then:
			Challenge.count() == 1
			Achievement.count() == 1
			
		when:
			params.challenge = Challenge.findByTitle("deleteMe").id.toString()
			params.token = TOKEN
			controller.delete()
		then:
			Challenge.count() == 1
			response.json.success == false
			response.json.error == controller.ERROR_CHALLENGE_HAS_ACHIEVEMENT
	}
	
	void "test delete challenge doesnt exist"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			Achievement.count() == 0
			
		when:
			controller.challengeService.create([title: "deleteMe", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.challenge = "1000"
			params.token = TOKEN
			controller.delete()
		then:
			Challenge.count() == 1
			response.json.success == false
			response.json.error == controller.ERROR_CHALLENGE_DOESNT_EXIST
	}
	
	void "test delete challenge not integer"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			Achievement.count() == 0
			
		when:
			controller.challengeService.create([title: "deleteMe", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.challenge = "x"
			params.token = TOKEN
			controller.delete()
		then:
			Challenge.count() == 1
			response.json.success == false
			response.json.error == controller.ERROR_CHALLENGE_DOESNT_EXIST
	}
	
	void "test delete missing challenge"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			Achievement.count() == 0
			
		when:
			controller.challengeService.create([title: "deleteMe", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.token = TOKEN
			controller.delete()
		then:
			Challenge.count() == 1
			response.json.success == false
			response.json.error == controller.ERROR_MISSING_CHALLENGE
	}
	
	void "test search"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.token = TOKEN
			controller.search()
		then:
			response.json.success == true
			response.json.challenges != null
	}
	
	void "test search by user"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.token = TOKEN
			params.user = "1"
			controller.search()
		then:
			response.json.success == true
			response.json.challenges != null
	}
	
	void "test search by gps location"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.token = TOKEN
			params.latitude = "3.2"
			params.longitude = "4.65"
			params.radius = "1000000.5"
			controller.search()
		then:
			response.json.success == true
			response.json.challenges != null
	}
	
	void "test search by gps location sort by nearby with max 1"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.token = TOKEN
			params.user = "1"
			params.latitude = "3.2"
			params.longitude = "4.65"
			params.radius = "1000000"
			params.sort = ChallengeService.SORT_NEARBY
			params.max = "1"
			controller.search()
		then:
			response.json.success == true
			response.json.challenges != null
	}
	
	void "test search unauthorized"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.token = "other"
			controller.search()
		then:
			response.json.success == false
			response.json.error == AuthService.ERROR_AUTH_FAILURE
	}
	
	void "test search user doesnt exist"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.token = TOKEN
			params.user = "100"
			controller.search()
		then:
			response.json.success == false
			response.json.error == ChallengeController.ERROR_USER_DOESNT_EXIST
	}
	
	void "test search missing latitude"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.token = TOKEN
			params.longitude = "3.5"
			params.radius = "100"
			controller.search()
		then:
			response.json.success == false
			response.json.error == ChallengeController.ERROR_MISSING_LOCATION_PARAMETER
	}
	
	void "test search missing longitude"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.token = TOKEN
			params.latitude = "3.5"
			params.radius = "100"
			controller.search()
		then:
			response.json.success == false
			response.json.error == ChallengeController.ERROR_MISSING_LOCATION_PARAMETER
	}
	
	void "test search missing radius"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.token = TOKEN
			params.latitude = "3.5"
			params.longitude = "3.5"
			controller.search()
		then:
			response.json.success == false
			response.json.error == ChallengeController.ERROR_MISSING_LOCATION_PARAMETER
	}
	
	void "test search sort nearby but missing latitude param"() {
		expect:
		User.count() == 1
		Challenge.count() == 0
		
	when:
		controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
	then:
		Challenge.count() == 1
		
	when:
		params.token = TOKEN
		params.sort = ChallengeService.SORT_NEARBY
		params.longitude = "3.5"
		params.radius = "10"
		controller.search()
	then:
		response.json.success == false
		response.json.error == ChallengeController.ERROR_MISSING_LOCATION_PARAMETER
	}
	
	void "test search sort nearby but missing longitude param"() {
		expect:
		User.count() == 1
		Challenge.count() == 0
		
	when:
		controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
	then:
		Challenge.count() == 1
		
	when:
		params.token = TOKEN
		params.sort = ChallengeService.SORT_NEARBY
		params.latitude = "3.5"
		params.radius = "10"
		controller.search()
	then:
		response.json.success == false
		response.json.error == ChallengeController.ERROR_MISSING_LOCATION_PARAMETER
	}
	
	void "test search sort nearby but missing radius param"() {
		expect:
		User.count() == 1
		Challenge.count() == 0
		
	when:
		controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
	then:
		Challenge.count() == 1
		
	when:
		params.token = TOKEN
		params.sort = ChallengeService.SORT_NEARBY
		params.latitude = "3.5"
		params.longitude = "3.5"
		controller.search()
	then:
		response.json.success == false
		response.json.error == ChallengeController.ERROR_MISSING_LOCATION_PARAMETER
	}
	
	void "test search unknown sort"() {
		expect:
		User.count() == 1
		Challenge.count() == 0
		
	when:
		controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
	then:
		Challenge.count() == 1
		
	when:
		params.token = TOKEN
		params.sort = "other"
		controller.search()
	then:
		response.json.success == false
		response.json.error == ChallengeController.ERROR_UNKNOWN_SORT_TYPE
	}
	
	void "test search max not integer"() {
		expect:
		User.count() == 1
		Challenge.count() == 0
		
	when:
		controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
	then:
		Challenge.count() == 1
		
	when:
		params.token = TOKEN
		params.max = "x"
		controller.search()
	then:
		response.json.success == false
		response.json.error == ChallengeController.ERROR_MAX_NOT_INTEGER
	}
	
	void "test search max exceeds limit"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
				
		when:
			params.token = TOKEN
			params.max = ChallengeController.MAX_RESULTS + 1
			controller.search()
		then:
			response.json.success == false
			response.json.error == ChallengeController.ERROR_MAX_LIMIT_EXCEEDED
	}
	
	void "test search by gps location invalid latitude"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.token = TOKEN
			params.latitude = "3x"
			params.longitude = "4.65"
			params.radius = "1000000"
			controller.search()
		then:
			response.json.success == false
			response.json.error == ChallengeController.ERROR_POINT_GPS_INVALID
	}
	
	void "test search by gps location invalid longitude"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.token = TOKEN
			params.latitude = "3.3"
			params.longitude = "v"
			params.radius = "1000000"
			controller.search()
		then:
			response.json.success == false
			response.json.error == ChallengeController.ERROR_POINT_GPS_INVALID
	}
	
	void "test search by gps location invalid radius"() {
		expect:
			User.count() == 1
			Challenge.count() == 0
			
		when:
			controller.challengeService.create([title: "test", user: User.get(1)], [[longitude: 4.0, latitude: 3.0]])
		then:
			Challenge.count() == 1
			
		when:
			params.token = TOKEN
			params.latitude = "3.1"
			params.longitude = "4.65"
			params.radius = "v3"
			controller.search()
		then:
			response.json.success == false
			response.json.error == ChallengeController.ERROR_POINT_GPS_INVALID
	}
}
