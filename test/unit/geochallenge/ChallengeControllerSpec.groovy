package geochallenge

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ChallengeController)
@Mock([User, Challenge, ChallengeService, AuthService])
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
			def challenge = new Challenge(info)
			challenge.save()
			
			return challenge
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
			params.user = 1
			params.token = TOKEN
			params.challenge = '{title: "title", expires: 123456789, points: [{longitude: 3.0, latitude: 2.5, type: "text", data: "Testing"}]}'
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
			params.user = 1
			params.token = "other"
			params.challenge = '{title: "title", points: [{longitude: 3.0, latitude: 2.5, type: "text", data: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == AuthService.ERROR_AUTH_FAILURE
	}
	
	void "test create missing title"() {
		when:
			params.user = 1
			params.token = TOKEN
			params.challenge = '{title: "", points: [{longitude: 3.0, latitude: 2.5, type: "text", data: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_MISSING_TITLE
	}
	
	void "test create invalid expires"() {
		when:
			params.user = 1
			params.token = TOKEN
			params.challenge = '{title: "title", points: [{longitude: 3.0, latitude: 2.5, type: "text", data: "Testing"}], expires: "hi"}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_EXPIRES_INVALID
	}
	
	void "test create missing points"() {
		when:
			params.user = 1
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
			params.user = 1
			params.token = TOKEN
			params.challenge = '{title: "title", expires: 123456789, points: [{longitude: "not", latitude: 2.5, type: "text", data: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_POINT_GPS_INVALID
	}
	
	void "test create invalid type"() {
		when:
			params.user = 1
			params.token = TOKEN
			params.challenge = '{title: "title", expires: 123456789, points: [{longitude: 3.0, latitude: 2.5, type: "other", data: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_INVALID_POINT_CONTENT_TYPE
	}
	
	void "test create point missing text"() {
		when:
			params.user = 1
			params.token = TOKEN
			params.challenge = '{title: "title", expires: 123456789, points: [{longitude: 3.0, latitude: 2.5, type: "text", data: ""}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_MISSING_POINT_TEXT
	}
	
	void "test create unexpected point data"() {
		when:
			params.user = 1
			params.token = TOKEN
			params.challenge = '{title: "title", expires: 123456789, points: [{longitude: 3.0, latitude: 2.5, type: "video", data: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_POINT_DATA_UNEXPECTED
	}
	
	void "test create invalid json"() {
		when:
			params.user = 1
			params.token = TOKEN
			params.challenge = '{title: "title",invalidEntry: ,expires: 123456789, points: [{longitude: 3.0, latitude: 2.5, type: "text", data: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_INVALID_JSON
	}
	
	void "test create user doesnt exist"() {
		when:
			params.user = 2
			params.token = TOKEN
			params.challenge = '{title: "title", expires: 123456789, points: [{longitude: 3.0, latitude: 2.5, type: "text", data: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_USER_DOESNT_EXIST
	}
	
	void "test create missing user"() {
		when:
			params.token = TOKEN
			params.challenge = '{title: "title", expires: 123456789, points: [{longitude: 3.0, latitude: 2.5, type: "text", data: "Testing"}]}'
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_MISSING_USER
	}
	
	void "test create missing challenge"() {
		when:
			params.user = 1
			params.token = TOKEN
			controller.create()
		then:
			Challenge.count() == 0
			response.json.success == false
			response.json.error == controller.ERROR_MISSING_CHALLENGE
	}
}
