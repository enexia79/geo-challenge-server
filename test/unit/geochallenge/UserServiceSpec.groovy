package geochallenge

import java.awt.ActiveEvent

import grails.test.mixin.Mock
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
	
	void "test toJSON"() {
		expect:
			User.count() == 1

		when:
			def jsonObject = service.toJSON(User.findByName("David"))
		then:
			jsonObject != null
			jsonObject.id == 1
			jsonObject.surrogateId == "testing"
			jsonObject.name == "David"
			jsonObject.dateCreated != null
			jsonObject.lastUpdated != null
			jsonObject.active == true
	}
	
	void "test toJSON List"() {
		expect:
			User.count() == 1
			
		when:
			service.create([name: "Joseph", surrogateId: "testing2"])
			def user = User.get(2)
			user.active = Boolean.FALSE
			user.save()
		then:
			User.count() == 2
			User.findByName("Joseph").surrogateId == "testing2"
			User.findByName("Joseph").isActive() == false
			
		when:
			def jsonObject = service.toJSON(User.getAll())
		then:
			jsonObject != null
			jsonObject[0].id == 1
			jsonObject[0].surrogateId == "testing"
			jsonObject[0].name == "David"
			jsonObject[0].dateCreated != null
			jsonObject[0].lastUpdated != null
			jsonObject[0].active == true
			jsonObject[1].id == 2
			jsonObject[1].surrogateId == "testing2"
			jsonObject[1].name == "Joseph"
			jsonObject[1].dateCreated != null
			jsonObject[1].lastUpdated != null
			jsonObject[1].active == false
	}
}
