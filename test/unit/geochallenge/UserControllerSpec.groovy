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
		def user = new User(name: 'david', surrogateId: 'enexia@gmail.com')
		
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
			params.surrogateId = "enexia@gmail.com"
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
			params.surrogateId = "enexia@gmail.com"
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
			params.surrogateId = "enexia@gmail.com"
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
}
