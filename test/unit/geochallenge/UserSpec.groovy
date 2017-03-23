package geochallenge

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(User)
class UserSpec extends Specification {

    def setup() {
		def user = new User(name: 'david', surrogateId: 'nospam@nowhere.com')
		
		user.save(flush:true)
    }

    def cleanup() {
    }
	
    void "test firstLoad"() {
    	def user = new User(name: 'test', surrogateId: 'another@gmail.com')
		when:
			user.save(flush:true)
		then:
			user.id != null
		
		when:
			def foundUser = user.get(user.id)
		then:
			foundUser.name.equals('test')
			foundUser.surrogateId.equals('another@gmail.com')
    }
	
	void "test delete"() {
		def user = User.findBySurrogateId('nospam@nowhere.com')
		when:
			user.delete()
		then:
			User.findBySurrogateId('nospam@nowhere.com') == null;
	}
	
	void "test unique surrogateID"() {
		when:
			(new User(name: 'david', surrogateId: 'nospam@nowhere.com')).save(failOnError:false)
		then:
			User.count() == 1
	}
	
	void "test default values"() {
		when:
			def user 	= new User(name: 'alejandra', surrogateId: 'bogas@nowhere.com').save(flush:true)
			def user2 	= User.get(user.id)
			def user3 	= new User(name: 'dac', surrogateId: 'youwish@here.com', active: Boolean.FALSE).save(flush:true)
			def user4	= User.findByName('dac')
		then:
			user.id != null
			user2.active == true
			user3.id != null
			user4.active == false
	}
}
