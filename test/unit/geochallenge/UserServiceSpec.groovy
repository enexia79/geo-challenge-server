package geochallenge

import java.awt.ActiveEvent

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(UserService)
@Mock(User)
class UserServiceSpec extends Specification {
    def setup() {
		def user = new User(name: "David", surrogateId: "testing")
		user.save()
    }

    def cleanup() {
    }

    void "test create"() {
		expect:
			User.count() == 1
			
		when:
			service.create([name: "Joseph", surrogateId: "testing2"])
		then:
			User.count() == 2
			User.findByName("Joseph").surrogateId == "testing2"
			service.create([name: "Joseph", surrogateId: "testing2"]) == null
			User.count() == 2
    }
	
	void "test getid"() {
		expect:
			User.count() == 1
			service.getIdBySurrogateId("testing") == 1
	}
	
	void "test toggle active"() {
		expect:
			User.count() == 1
			User.findByName("David")?.active == true
			
		when:
			service.toggleActive(User.findByName("David"))
		then:
			User.findByName("David")?.active == false
			
		when:
			service.toggleActive(User.findByName("David").id)
		then:
			User.findByName("David")?.active == true
	}
}
