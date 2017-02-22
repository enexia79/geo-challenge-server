package geochallenge

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Point)
@Mock([PointContent, Challenge, User])
class PointSpec extends Specification {

    def setup() {
		def user = new User(name: "Joe", surrogateId:"not@telling.com")
		user.save(flush: true)
		def challenge = new Challenge(title: "title", description: "description", user: user)
		challenge.save(flush:true)
		def point = new Point(title: "Nowhere", longitude: 100.0, latitude: 25.5, challenge: challenge)
		point.save(flush:true)
		def content = new PointContent(type: ContentType.TEXT, data: "Testing Data".bytes, point: point)
		content.save(flush:true)
    }

    def cleanup() {
    }

    void "test add/delete/update"() {
		expect:
			Challenge.count() == 1
			Point.count() == 1
			new String(PointContent.get(1).data, "UTF-8") == "Testing Data"
			PointContent.get(1).type == ContentType.TEXT
			PointContent.get(1).point == Point.get(1)
			Point.get(1).longitude == 100.0
			Point.get(1).latitude == 25.5
			Point.get(1).title == "Nowhere"
			
		when:
			def point = Point.get(1)
			point.longitude = 18.34
			point.latitude = 4.224
			def content = new PointContent(type: ContentType.TEXT, data: "New Data".bytes)
			PointContent.get(1).delete(flush:true)
			content.save(flush:true)
			point.content = content
			point.save(flush:true)
		then:
			Point.get(1).longitude == 18.34
			Point.get(1).latitude == 4.224
			new String(Point.get(1).content.data, "UTF-8") == "New Data"
			
		when:
			Point.get(1).delete(flush:true)
		then:
			Point.count() == 0
			
		when:
			point = new Point(title: "new", longitude: 50.2, latitude: 15.0, challenge: Challenge.get(1))
			point.save(flush:true)
		then:
			Point.count() == 1
			point.title == "new"
			point.longitude == 50.2
			point.latitude == 15.0
    }
}
