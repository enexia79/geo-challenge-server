package geochallenge

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ChallengeService)
@Mock([User, Challenge, Point])
class ChallengeServiceSpec extends Specification {

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

    void "test create"() {
		expect:
			User.count() == 1
			Challenge.count() == 1
			Point.count() == 2
			PointContent.count() == 0
			
		when:
			def pointInfo	= [title: "title", longitude: 1.0, latitude: 2.0, type: ContentType.TEXT.toString(), data: "Your not done"]
			def pointInfo2	= [title: "title2", longitude: 3.0, latitude: 4.0, type: ContentType.VIDEO.toString()]
			def challenge 	= service.create([title: "title2", description: "description2", expires: new Date(), user: User.findByName("Billy")], [pointInfo, pointInfo2])
		then:
			Challenge.count() == 2
			Point.count() == 4
			PointContent.count() == 1
			challenge != null
			challenge.points.size() == 2
			challenge.points.toList().get(1).title == "title2"
			challenge.expires != null
    }
}
