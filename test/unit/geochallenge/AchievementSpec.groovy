package geochallenge

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Achievement)
@Mock([User, Challenge])
class AchievementSpec extends Specification {

    def setup() 
	{
		def user = new User(name: "Joe", surrogateId:"not@telling.com")
		user.save(flush: true)
		def challenge = new Challenge(title: "blah", description: "blah blah", user: User.get(1), longitude: 10, latitude: 5)
		challenge.save(flush: true)
		user.addToChallenges(challenge).save(flush: true)
		def achievement = new Achievement(challenge: challenge, user: user, content: "Testing Data")
		achievement.save(flush: true)
		user.addToAchievements(achievement).save(flush:true)
    }

    def cleanup() 
	{
    }

    void "test add/delete"() 
	{
		expect:
			User.count() == 1
			Challenge.count() == 1
			Achievement.count() == 1
			Achievement.get(1).content == "Testing Data"
			User.get(1).achievements?.iterator().next() == Achievement.get(1)
			
		when:
			def achievement = new Achievement(challenge: Challenge.get(1), user: User.get(1))
			achievement.save(flush: true)
			User.get(1).addToAchievements(achievement).save(flush:true)
		then:
			Achievement.count() == 2
			User.get(1).achievements.size() == 2
			
		when:
			achievement = Achievement.get(1)
			User.get(1).removeFromAchievements(achievement).save(flush: true)
			achievement.delete(flush: true)
		then:
			Achievement.count() == 1
			User.get(1).achievements.size() == 1
    }
}
