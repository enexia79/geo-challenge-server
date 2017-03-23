package geochallenge

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(UserController)
@Mock([User, UserService, AuthService])
class UserControllerSpec extends Specification {

	private final String TOKEN = "geo-ninjas"
	
    def setup() {
		def user = new User(name: 'david', surrogateId: 'nospam@thank.you')
		
		user.save()
		
		def service = mockFor(AuthService)
		service.demand.isAuthorized {
			String token ->
			return token == TOKEN
		}
		controller.authService = service.createMock()
		service = mockFor(UserService)
		service.demand.create(0..1) {
			def info ->
			if(info.name && info.surrogateId) {
				user = new User(name: info.name, surrogateId: info.surrogateId)
				if(user.save())
					return user
			}
		}
		service.demand.getIdBySurrogateId(0..1) {
			def surrogateId ->
			return User.findBySurrogateId(surrogateId)?.id
		}
		service.demand.toJSON(0..1) {
			return [name: "mocked"]
		}
		service.demand.toggleActive(0..1) {
			return true
		}
		controller.userService = service.createMock()
    }

    def cleanup() {
    }

    void "test create"() {
		expect:
			User.count() == 1
			
		when:
			params.name = 'ale'
			params.surrogateId = 'testing'
			params.token = TOKEN
			controller.create()
		then:
			User.count() == 2
			response.json.success == true
			response.json.id != null
    }
	
    void "test create wrong token"() {
    	expect:
    		User.count() == 1
    		
		when:
			params.name = "ale"
			params.surrogateId = "other"
			params.token = "wrong token"
			controller.create()
		then:
			User.count() == 1
			response.json.success == false
			response.json.error == AuthService.ERROR_AUTH_FAILURE
    }
	
	void "test create duplicate surrogate id"() {
		expect:
			User.count() == 1
			
		when:
			params.name = "ale"
			params.surrogateId = "nospam@thank.you"
			params.token = TOKEN
			controller.create()
		then:
			User.count() == 1
			response.json.success == false
			response.json.error == UserController.ERROR_DUPLICATE_SURROGATE_ID
	}
	
	void "test create missing name"() {
		expect:
			User.count() == 1
			
		when:
			params.name = ""
			params.surrogateId = "other"
			params.token = TOKEN
			controller.create()
		then:
			User.count() == 1
			response.json.success == false
			response.json.error == UserController.ERROR_MISSING_NAME
	}
	
	void "test create missing surrogate id"() {
		expect:
			User.count() == 1
			
		when:
			params.name = "ale"
			params.surrogateId = ""
			params.token = TOKEN
			controller.create()
		then:
			User.count() == 1
			response.json.success == false
			response.json.error == UserController.ERROR_MISSING_SURROGATE_ID
	}
	
	void "test getId"() {
		expect:
			User.count() == 1
			
		when:
			params.surrogateId = "nospam@thank.you"
			params.token = TOKEN
			controller.getId()
		then:
			User.count() == 1
			response.json.success == true
			response.json.id != null
	}
	
	void "test getId wrong token"() {
		expect:
			User.count() == 1
			
		when:
			params.surrogateId = "nospam@thank.you"
			params.token = "blah"
			controller.getId()
		then:
			response.json.success == false
			response.json.error == AuthService.ERROR_AUTH_FAILURE
	}
	
	void "test getId missing surrogateId"() {
		expect:
			User.count() == 1
			
		when:
			params.surrogateId = ""
			params.token = TOKEN
			controller.getId()
		then:
			response.json.success == false
			response.json.error == UserController.ERROR_MISSING_SURROGATE_ID
	}
	
	void "test getId surrogateId not found"() {
		expect:
		User.count() == 1
		
	when:
		params.surrogateId = "unknown"
		params.token = TOKEN
		controller.getId()
	then:
		response.json.success == false
		response.json.error == UserController.ERROR_SURROGATE_ID_DOES_NOT_EXIST
	}
	
	void "test get"() {
		expect:
			User.count() == 1
			
		when:
			params.user = "1"
			params.token = TOKEN
			controller.get()
		then:
			response.json.success == true
			response.json.user != null
	}
	
	void "test get unauthorized"() {
		when:
			params.user = "1"
			params.token = "other"
			controller.get()
		then:
			response.json.success == false
			response.json.user == null
			response.json.error == AuthService.ERROR_AUTH_FAILURE
	}
	
	void "test get user doesnt exist"() {
		when:
			params.user = "1000"
			params.token = TOKEN
			controller.get()
		then:
			response.json.success == false
			response.json.user == null
			response.json.error == UserController.ERROR_USER_DOESNT_EXIST
	}
	
	void "test get missing user"() {
		when:
			params.token = TOKEN
			controller.get()
		then:
			response.json.success == false
			response.json.user == null
			response.json.error == UserController.ERROR_MISSING_USER
	}
	
	void "test toggleActive"() {
		when: 
			params.token = TOKEN
			params.user = "1"
			controller.toggleActive()
		then:
			response.json.success == true
			response.json.active != null
	}
	
	void "test toggleActive unauthorized"() {
		when:
			params.user = "1"
			params.token = "other"
			controller.toggleActive()
		then:
			response.json.success == false
			response.json.active == null
			response.json.error == AuthService.ERROR_AUTH_FAILURE
	}
	
	void "test toggleActive user doesnt exist"() {
		when:
			params.user = "1000"
			params.token = TOKEN
			controller.toggleActive()
		then:
			response.json.success == false
			response.json.active == null
			response.json.error == UserController.ERROR_USER_DOESNT_EXIST
	}
	
	void "test toggleActive missing user"() {
		when:
			params.token = TOKEN
			controller.toggleActive()
		then:
			response.json.success == false
			response.json.active == null
			response.json.error == UserController.ERROR_MISSING_USER
	}
}
