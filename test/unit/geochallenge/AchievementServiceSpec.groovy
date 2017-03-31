package geochallenge

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(AchievementService)
@Mock([User, Challenge, Point, Achievement])
class AchievementServiceSpec extends Specification {

    def setup() {
		def user = new User(name: 'david', surrogateId: 'nospam@nowhere.com')
		user.save(flush: true)
		user = new User(name: 'ale', surrogateId: 'unavailable@whatever.com')
		user.save(flush: true)
		def challenge = new Challenge(user: user.get(1), title: "alpha", latitude: 2, longitude: 1)
		challenge.save(flush: true)
		challenge = new Challenge(user: user, title: "beta", latitude: 2, longitude: 1)
		challenge.save(flush: true)
		challenge = new Challenge(user: User.get(1), title: "charlie", latitude: 2, longitude: 1)
		challenge.save(flush: true)
		def achievement = new Achievement(user: user, challenge: Challenge.findByTitle("alpha"), content: "alpha content")
		achievement.save(flush: true)
		achievement = new Achievement(user: user, challenge: Challenge.findByTitle("charlie"), content: "charlie content")
		achievement.save(flush: true)
		achievement = new Achievement(user: User.get(1), challenge: Challenge.findByTitle("beta"), content: "beta content")
		achievement.save(flush: true)
    }

    def cleanup() {
    }

    void "test create"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
		
		when:
			service.create([user: User.get(1), challenge: Challenge.get(1), content: "test content"])
			def achievement = Achievement.get(4)
		then:
			Achievement.count() == 4
			achievement.user == User.get(1)
			achievement.challenge == Challenge.get(1)
			achievement.content == "test content"
    }
	
	void "test findAllByUser"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			def achievements = service.getAllByUser(User.get(2))
		then:
			achievements.size() == 2
	}
	
	void "test findAllByChallenge"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			def achievement = new Achievement(user: User.get(2), challenge: Challenge.get(2))
			achievement.save()
		then:
			Achievement.count() == 4
			
		when:
			def achievements = service.getAllByChallenge(Challenge.get(2))
		then:
			achievements.size() == 2
			
		when:
			achievements = service.getAllByChallenge(Challenge.get(1))
		then:
			achievements.size() == 1
	}
	
	void "test toJSON"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			def jsonObject = service.toJSON(Achievement.get(1))
		then:
			jsonObject != null
			jsonObject.name == "ale"
			jsonObject.user == 2
			jsonObject.challenge == 1
			jsonObject.content == "alpha content"
			jsonObject.dateCreated != null
			jsonObject.lastUpdated != null
	}
	
	void "test toJSON List"() {
		expect:
			User.count() == 2
			Challenge.count() == 3
			Achievement.count() == 3
			
		when:
			def jsonObject = service.toJSON(Achievement.getAll())
		then:
			jsonObject != null
			jsonObject.size() == 3
			jsonObject[0].name == "ale"
			jsonObject[0].user == 2
			jsonObject[0].challenge == 1
			jsonObject[0].content == "alpha content"
			jsonObject[0].dateCreated != null
			jsonObject[0].lastUpdated != null
			jsonObject[1].name == "ale"
			jsonObject[1].user == 2
			jsonObject[1].challenge == 3
			jsonObject[1].content == "charlie content"
			jsonObject[1].dateCreated != null
			jsonObject[1].lastUpdated != null
			jsonObject[2].name == "david"
			jsonObject[2].user == 1
			jsonObject[2].challenge == 2
			jsonObject[2].content == "beta content"
			jsonObject[2].dateCreated != null
			jsonObject[2].lastUpdated != null
	}
}
