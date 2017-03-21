package geochallenge

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ChallengeService)
@Mock([User, Challenge, Point, Achievement])
class ChallengeServiceSpec extends Specification {

    def setup() {
		def user = new User(name: "Billy", surrogateId: "blah@nobody.net")
		user.save(flush:true)
		def challenge = new Challenge(title: "title", description: "description", user: user, latitude: 40.244830, longitude: -111.627304)
		challenge.save(flush:true)
		user.addToChallenges(challenge).save(flush: true)
		def point = new Point(longitude: challenge.longitude, latitude: challenge.latitude, challenge: Challenge.get(1), content: "Testing Data")
		point.save(flush:true)
		Challenge.get(1).addToPoints(point).save(flush:true)
		point = new Point(longitude: 21.1, latitude: 2.2, challenge: challenge)
		point.save(flush:true)
		challenge.addToPoints(point).save(flush:true)
		user = new User(name: "Bob", surrogateId: "blah@somebody.net")
		user.save(flush:true)
    }

    def cleanup() {
    }

    void "test create"() {
		expect:
			User.count() == 2
			Challenge.count() == 1
			Point.count() == 2
			Achievement.count() == 0
			
		when:
			def pointInfo	= [title: "title", longitude: 1.5, latitude: 2.5, content: "Your not done"]
			def pointInfo2	= [title: "title2", longitude: 3.0, latitude: 4.0]
			def challenge 	= service.create([title: "title2", description: "description2", expires: new Date(), user: User.findByName("Billy")], [pointInfo, pointInfo2])
		then:
			Challenge.count() == 2
			Point.count() == 4
			challenge != null
			challenge.points.size() == 2
			challenge.points.toList().get(1).title == "title2"
			challenge.expires != null
			challenge.latitude == pointInfo.latitude
			challenge.longitude == pointInfo.longitude
    }
	
	void "test search default popular search"() {
		expect:
			User.count() == 2
			Challenge.count() == 1
			Point.count() == 2
			Achievement.count() == 0
			
		when:
			def challenge = new Challenge(title: "title2", description: "description", user: User.get(1), latitude: 39.973277, longitude: -112.124345)
			challenge.save(flush:true)
			User.get(1).addToChallenges(challenge).save(flush: true)
			def point = new Point(latitude: challenge.latitude, longitude: challenge.longitude, challenge: Challenge.get(1), content: "Testing Data")
			point.save(flush:true)
			Challenge.get(1).addToPoints(point).save(flush:true)
			def achievement = new Achievement(user: User.get(2), challenge: challenge)
			achievement.save(flush:true)
			def challenges = service.search(max: 100) 
		then:
			Challenge.count() == 2
			Achievement.count() == 1
			challenges.size() == 2
			challenges.toList().first().id == Challenge.findByTitle("title2").id
	}
	
	void "test search user"() {
		expect:
			User.count() == 2
			Challenge.count() == 1
			Point.count() == 2
			Achievement.count() == 0
			
		when:
			def challenge = new Challenge(title: "title2", description: "description", user: User.get(1), latitude: 39.973277, longitude: -112.124345)
			challenge.save(flush:true)
			User.get(1).addToChallenges(challenge).save(flush: true)
			def point = new Point(latitude: challenge.latitude, longitude: challenge.longitude, challenge: Challenge.get(1), content: "Testing Data")
			point.save(flush:true)
			Challenge.get(1).addToPoints(point).save(flush:true)
			def achievement = new Achievement(user: User.get(2), challenge: challenge)
			achievement.save(flush:true)
			def challenges 	= service.search(user: User.get(1), max: 100)
			def challenges2	= service.search(user: User.get(2), max: 100)
		then:
			Challenge.count() == 2
			Achievement.count() == 1
			challenges.size() == 2
			challenges2.size() == 0
	}
	
	void "test search nearby"() {
		expect:
			User.count() == 2
			Challenge.count() == 1
			Point.count() == 2
			Achievement.count() == 0
			
		when:
			def challenge = new Challenge(title: "title2", description: "description", user: User.get(1), latitude: 39.973277, longitude: -112.124345)
			challenge.save(flush:true)
			User.get(1).addToChallenges(challenge).save(flush: true)
			def point = new Point(latitude: challenge.latitude, longitude: challenge.longitude, challenge: Challenge.get(1), content: "Testing Data")
			point.save(flush:true)
			Challenge.get(1).addToPoints(point).save(flush:true)
			def achievement = new Achievement(user: User.get(2), challenge: challenge)
			achievement.save(flush:true)
			def challenges = service.search(latitude: 40.229939, longitude: -111.680585, radius: 25000, sort: ChallengeService.SORT_NEARBY, max: 100)
		then:
			Challenge.count() == 2
			Achievement.count() == 1
			challenges.size() == 1
			challenges.toList().first().id == Challenge.findByTitle("title").id
	}
	
	void "test search max"() {
		expect:
			User.count() == 2
			Challenge.count() == 1
			Point.count() == 2
			Achievement.count() == 0
			
		when:
			def challenge = new Challenge(title: "title2", description: "description", user: User.get(1), latitude: 39.973277, longitude: -112.124345)
			challenge.save(flush:true)
			User.get(1).addToChallenges(challenge).save(flush: true)
			def point = new Point(latitude: challenge.latitude, longitude: challenge.longitude, challenge: Challenge.get(1), content: "Testing Data")
			point.save(flush:true)
			Challenge.get(1).addToPoints(point).save(flush:true)
			def achievement = new Achievement(user: User.get(2), challenge: challenge)
			achievement.save(flush:true)
			def challenges = service.search(max: 1)
		then:
			Challenge.count() == 2
			Achievement.count() == 1
			challenges.size() == 1
	}
	
	void "test search expired"() {
		expect:
			User.count() == 2
			Challenge.count() == 1
			Point.count() == 2
			Achievement.count() == 0
			
		when:
			def challenge = new Challenge(title: "title2", description: "description", user: User.get(1), latitude: 39.973277, longitude: -112.124345, expires: new Date(new Date().getTime() - (5 * 60 * 1000)))
			challenge.save(flush:true)
			User.get(1).addToChallenges(challenge).save(flush: true)
			def point = new Point(latitude: challenge.latitude, longitude: challenge.longitude, challenge: Challenge.get(1), content: "Testing Data")
			point.save(flush:true)
			Challenge.get(1).addToPoints(point).save(flush:true)
			def achievement = new Achievement(user: User.get(2), challenge: challenge)
			achievement.save(flush:true)
			def challenges = service.search(max: 100)
		then:
			Challenge.count() == 2
			Achievement.count() == 1
			challenges.size() == 1
	}
	
	void "test search not expired"() {
		expect:
			User.count() == 2
			Challenge.count() == 1
			Point.count() == 2
			Achievement.count() == 0
			
		when:
			def challenge = new Challenge(title: "title2", description: "description", user: User.get(1), latitude: 39.973277, longitude: -112.124345, expires: new Date(new Date().getTime() + (5 * 60 * 1000)))
			challenge.save(flush:true)
			User.get(1).addToChallenges(challenge).save(flush: true)
			def point = new Point(latitude: challenge.latitude, longitude: challenge.longitude, challenge: Challenge.get(1), content: "Testing Data")
			point.save(flush:true)
			Challenge.get(1).addToPoints(point).save(flush:true)
			def achievement = new Achievement(user: User.get(2), challenge: challenge)
			achievement.save(flush:true)
			def challenges = service.search(max: 100)
		then:
			Challenge.count() == 2
			Achievement.count() == 1
			challenges.size() == 2
	}
	
	void "test search include expired"() {
		expect:
			User.count() == 2
			Challenge.count() == 1
			Point.count() == 2
			Achievement.count() == 0
			
		when:
			def challenge = new Challenge(title: "title2", description: "description", user: User.get(1), latitude: 39.973277, longitude: -112.124345, expires: new Date(new Date().getTime() - (5 * 60 * 1000)))
			challenge.save(flush:true)
			User.get(1).addToChallenges(challenge).save(flush: true)
			def point = new Point(latitude: challenge.latitude, longitude: challenge.longitude, challenge: Challenge.get(1), content: "Testing Data")
			point.save(flush:true)
			Challenge.get(1).addToPoints(point).save(flush:true)
			def achievement = new Achievement(user: User.get(2), challenge: challenge)
			achievement.save(flush:true)
			def challenges = service.search(includeExpired: true, max: 100)
		then:
			Challenge.count() == 2
			Achievement.count() == 1
			challenges.size() == 2
	}
	
	void "test toJSON"() {
		expect:
			User.count() == 2
			Challenge.count() == 1
			Point.count() == 2
			
		when:
			def jsonObject = service.toJSON(Challenge.findByTitle("title"))
		then:
			jsonObject != null
			jsonObject.title == "title"
			jsonObject.description == "description"
			jsonObject.user == 1
			jsonObject.dateCreated != null
			jsonObject.lastUpdated != null
			jsonObject.points.size() == 2
			jsonObject.points[0].latitude == Point.get(1).latitude
			jsonObject.points[0].longitude == Point.get(1).longitude
			jsonObject.points[0].content == "Testing Data"
			jsonObject.points[1].longitude == 21.1
			jsonObject.points[1].latitude == 2.2
			jsonObject.points[1].content == null
	}
	
	void "test toJSON List"() {
		expect:
			User.count() == 2
			Challenge.count() == 1
			Point.count() == 2
			
		when:
			def pointInfo	= [title: "title", latitude: 2.5, longitude: 1.5, content: "Your not done"]
			def pointInfo2	= [title: "title2", latitude: 4.0, longitude: 3.0]
			def challenge 	= service.create([title: "title2", description: "description2", expires: new Date(), user: User.get(1)], [pointInfo, pointInfo2])
		then:
			Challenge.count() == 2
			
		when:
			def jsonObject = service.toJSON(Challenge.getAll())
		then:
			jsonObject != null
			jsonObject[0].title == "title"
			jsonObject[0].description == "description"
			jsonObject[0].user == 1
			jsonObject[0].points.size() == 2
			jsonObject[0].points[0].latitude == Point.get(1).latitude
			jsonObject[0].points[0].longitude == Point.get(1).longitude
			jsonObject[0].points[0].content == "Testing Data"
			jsonObject[0].points[1].latitude == 2.2
			jsonObject[0].points[1].longitude == 21.1
			jsonObject[0].points[1].content == null
			jsonObject[1].title == "title2"
			jsonObject[1].description == "description2"
			jsonObject[1].user == 1
			jsonObject[1].points.size() == 2
			jsonObject[1].points[0].latitude == Point.get(3).latitude
			jsonObject[1].points[0].longitude == Point.get(3).longitude
			jsonObject[1].points[0].content == "Your not done"
			jsonObject[1].points[1].latitude ==	4.0
			jsonObject[1].points[1].longitude == 3.0
			jsonObject[1].points[1].content == null
	}
}
