package geochallenge

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(AchievementController)
@Mock([User, Challenge, AuthService, Achievement, AchievementService])
class AchievementControllerSpec extends Specification {

	private final String TOKEN = "geo-ninjas"
	
    def setup() {
		def user = new User(name: 'david', surrogateId: 'nospam@nowhere.com')
		user.save(flush: true)
		user = new User(name: 'ale', surrogateId: 'unavailable@whatever.com')
		user.save(flush: true)
		def challenge = new Challenge(user: user.get(1), title: "alpha", latitude: 2, longitude: 1)
		challenge.save(flush: true)
		challenge = new Challenge(user: user, title: "beta", latitude: 2, longitude: 1)
		challenge.save(flush: true)
		challenge = new Challenge(user: User.get(1), title: "charlie", latitude: 2, longitude: 1)
		challenge.save(flush: true)
		def achievement = new Achievement(user: user, challenge: Challenge.findByTitle("alpha"), content: "alpha content")
		achievement.save(flush: true)
		achievement = new Achievement(user: user, challenge: Challenge.findByTitle("charlie"), content: "charlie content")
		achievement.save(flush: true)
		achievement = new Achievement(user: User.get(1), challenge: Challenge.findByTitle("beta"), content: "beta content")
		achievement.save(flush: true)
		
		def service = mockFor(AuthService)
		service.demand.isAuthorized {
			String token ->
			return token == TOKEN
		}
		controller.authService = service.createMock()
		service = mockFor(AchievementService)
		service.demand.create (0..1) {
			def info ->
			achievement = new Achievement(info)
			achievement.save()
			
			return achievement
		}
		service.demand.getAllByUser(0..1) {
			return []
		}
		service.demand.getAllByChallenge(0..1) {
			return []
		}
		service.demand.toJSON(0..1) {
			return []
		}
		controller.achievementService = service.createMock()
    }

    def cleanup() {
    }

    void "test create"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = TOKEN
			params.challenge = 3
			params.user = 1
			params.content = "4 content"
			controller.create()
		then:
			Achievement.count() == 4
			response.json.success == true
			response.json.id != null
			Achievement.get(4).content == "4 content"
    }
	
	void "test create failed auth"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = "other"
			params.challenge = 2
			params.user = 1
			controller.create()
		then:
			Achievement.count() == 3
			response.json.success == false
			response.json.error == AuthService.ERROR_AUTH_FAILURE
	}
	
	void "test create missing user"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = TOKEN
			params.challenge = 2
			controller.create()
		then:
			Achievement.count() == 3
			response.json.success == false
			response.json.error == UserController.ERROR_MISSING_USER
	}
	
	void "test create user doesnt exist"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = TOKEN
			params.challenge = 2
			params.user = 10
			controller.create()
		then:
			Achievement.count() == 3
			response.json.success == false
			response.json.error == UserController.ERROR_USER_DOESNT_EXIST
	}
	
	void "test create user inactive"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			def user = User.get(2)
			user.active = Boolean.FALSE
			user.save()
		then:
			User.get(2).isActive() == false
			
		when:
			params.token = TOKEN
			params.challenge = 2
			params.user = 2
			controller.create()
		then:
			Achievement.count() == 3
			response.json.success == false
			response.json.error == UserController.ERROR_USER_INACTIVE
	}
	
	void "test create missing challenge"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = TOKEN
			params.user = 1
			controller.create()
		then:
			Achievement.count() == 3
			response.json.success == false
			response.json.error == ChallengeController.ERROR_MISSING_CHALLENGE
	}
	
	void "test create challenge doesnt exist"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = TOKEN
			params.user = 1
			params.challenge = 10
			controller.create()
		then:
			Achievement.count() == 3
			response.json.success == false
			response.json.error == ChallengeController.ERROR_CHALLENGE_DOESNT_EXIST
	}
	
	void "test create duplicate achievement"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = TOKEN
			params.user = 1
			params.challenge = 2
			controller.create()
		then:
			Achievement.count() == 3
			response.json.success == false
			response.json.error == AchievementController.ERROR_DUPLICTE_ACHIEVEMENT
	}
	
	void "test getAllByUser"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = TOKEN
			params.user = 2
			controller.getAllByUser()
		then:
			response.json.success == true
			response.json.achievements != null
	}
	
	void "test getAllByUser invalid token"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = "other"
			params.user = 2
			controller.getAllByUser()
		then:
			response.json.success == false
			response.json.error == AuthService.ERROR_AUTH_FAILURE
	}
	
	void "test getAllByUser missing user"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = TOKEN
			controller.getAllByUser()
		then:
			response.json.success == false
			response.json.error == UserController.ERROR_MISSING_USER
	}
	
	void "test getAllByUser user doesnt exist"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = TOKEN
			params.user = 10
			controller.getAllByUser()
		then:
			response.json.success == false
			response.json.error == UserController.ERROR_USER_DOESNT_EXIST
	}
	
	void "test getAllByUser user inactive"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			def user = User.get(2)
			user.active = Boolean.FALSE
			user.save()
		then:
			User.get(2).isActive() == false
			
		when:
			params.token = TOKEN
			params.user = 2
			controller.getAllByUser()
		then:
			response.json.success == false
			response.json.error == UserController.ERROR_USER_INACTIVE
	}
	
	void "test getAllByChallenge"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = TOKEN
			params.challenge = 1
			controller.getAllByChallenge()
		then:
			response.json.success == true
			response.json.achievements != null
	}
	
	void "test getAllByChallenge invalid token"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = "other"
			params.challenge = 2
			controller.getAllByChallenge()
		then:
			response.json.success == false
			response.json.error == AuthService.ERROR_AUTH_FAILURE
	}
	
	void "test getAllByChallenge missing challenge"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = TOKEN
			controller.getAllByChallenge()
		then:
			response.json.success == false
			response.json.error == ChallengeController.ERROR_MISSING_CHALLENGE
	}
	
	void "test getAllByChallenge challenge doesnt exist"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			params.token = TOKEN
			params.challenge = 10
			controller.getAllByChallenge()
		then:
			response.json.success == false
			response.json.error == ChallengeController.ERROR_CHALLENGE_DOESNT_EXIST
	}
}
