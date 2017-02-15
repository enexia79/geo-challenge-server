package geochallenge

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(PointContent)
@Mock([Point, Challenge, User])
class PointContentSpec extends Specification {

    def setup() 
	{
		def user = new User(name: "Joe", email:"not@telling.com")
		user.save(flush: true)
		def challenge = new Challenge(title: "title", description: "description", user: user)
		challenge.save(flush:true)
		def point = new Point(title: "Nowhere", longitude: 100.0, latitude: 25.5, challenge: challenge)
		point.save(flush:true)
		def content = new PointContent(type: ContentType.TEXT, data: "Testing Data".bytes, point: point)
		content.save(flush:true)
    }

    def cleanup() 
	{
    }

    void "test add/delete/update"() 
	{
		expect:
			Point.count() == 1
			PointContent.count() == 1
			new String(PointContent.get(1).data, "UTF-8") == "Testing Data"
			PointContent.get(1).type == ContentType.TEXT
			PointContent.get(1).point == Point.get(1)
			
		when:
			def content = PointContent.get(1)
			content.data = "New data".bytes
			content.type = ContentType.PICTURE
			content.save(flush:true)
		then:
			new String(PointContent.get(1).data, "UTF-8") == "New data"
			PointContent.get(1).type == ContentType.PICTURE
			
		when:
			PointContent.get(1).delete(flush:true)
		then:
			PointContent.count() == 0
			
		when:
			new PointContent(type: ContentType.VIDEO, data: "Added".bytes, point: Point.get(1)).save(flush:true)
		then:
			PointContent.count() == 1
			new String(PointContent.get(2).data, "UTF-8") == "Added"
			PointContent.get(2).type == ContentType.VIDEO
    }
	
	void "test getStringData"()
	{
		expect:
			PointContent.get(1).getStringData() == "Testing Data"
	}
}
