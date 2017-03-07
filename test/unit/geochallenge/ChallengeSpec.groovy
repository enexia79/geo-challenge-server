package geochallenge

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Challenge)
@Mock([User, Point, Achievement])
class ChallengeSpec extends Specification {

    def setup() {
		def user = new User(name: "Billy", surrogateId: "blah@nobody.net")
		user.save(flush:true)
		def challenge = new Challenge(title: "title", description: "description", user: user)
		challenge.save(flush:true)
		user.addToChallenges(challenge).save(flush: true)
		def point = new Point(longitude: 20.0, latitude: 3.5, challenge: Challenge.get(1))
		point.save(flush:true)
		Challenge.get(1).addToPoints(point).save(flush:true)
		point = new Point(longitude: 21.1, latitude: 2.2, challenge: challenge)
		point.save(flush:true)
		challenge.addToPoints(point).save(flush:true)
    }

    def cleanup() {
    }

    void "test add/update/delete"() {
		expect:
			User.count() == 1
			Challenge.count() == 1
			Point.count() == 2
			Challenge.get(1).title == "title"
			Challenge.get(1).description == "description"
			Challenge.get(1).user == User.get(1)
			Challenge.get(1).points?.size() == 2
			
		when:
			def challenge = Challenge.get(1)
			challenge.title = "new title"
			challenge.description = "new description"
			challenge.save(flush:true)
			challenge.removeFromPoints(Point.get(2)).save(flush:true)
		then:
			Challenge.get(1).title == "new title"
			Challenge.get(1).description == "new description"
			Challenge.get(1).points?.size() == 1
			
		when:
			Challenge.get(1).delete(flush:true)
		then:
			Challenge.count() == 0
    }
	
	void "test expires"() {
		expect:
			User.count() == 1
			Challenge.count() == 1
			Point.count() == 2
			Challenge.get(1).title == "title"
			Challenge.get(1).description == "description"
			Challenge.get(1).user == User.get(1)
			Challenge.get(1).points?.size() == 2
			Challenge.get(1).isExpired() == false
			
		when:
			def challenge 		= Challenge.get(1)
			def expires			= new Date(new Date().getTime() - 1000)
			challenge.expires	= expires
			challenge.save()
		then:
			Challenge.get(1).expires.equals(expires)
			Challenge.get(1).isExpired() == true
			
		when:
			expires				= new Date(new Date().getTime() + 10000)
			challenge.expires	= expires
			challenge.save()
		then:
			Challenge.get(1).expires.equals(expires)
			Challenge.get(1).isExpired() == false
	}
}
