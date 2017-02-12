package geochallenge

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(AchievementContent)
@Mock([User, Achievement, Challenge])
class AchievementContentSpec extends Specification {

    def setup() 
	{
		def user = new User(name: "Joe", email:"not@telling.com")
		user.save(flush: true)
		def challenge = new Challenge(title: "blah", description: "blah blah", user: User.get(1))
		challenge.save(flush: true)
		user.addToChallenges(challenge).save(flush: true)
		def achievement = new Achievement(challenge: challenge, user: user)
		achievement.save(flush: true)
		def content = new AchievementContent(type: ContentType.TEXT, data: "Testing Data".bytes, achievement: achievement)
		content.save(flush: true)
		user.addToAchievements(achievement).save(flush:true)
    }

    def cleanup() 
	{
    }

    void "test add/delete/update"() 
	{
		expect:
			User.count() == 1
			Challenge.count() == 1
			Achievement.count() == 1
			AchievementContent.count() == 1
			new String(AchievementContent.get(1).data, "UTF-8") == "Testing Data"
			AchievementContent.get(1).type == ContentType.TEXT
			AchievementContent.get(1).achievement == Achievement.get(1)
			
		when:
			def content = AchievementContent.get(1)
			content.data = "New data".bytes
			content.type = ContentType.PICTURE
			content.save(flush:true)
		then:
			new String(AchievementContent.get(1).data, "UTF-8") == "New data"
			AchievementContent.get(1).type == ContentType.PICTURE
			
		when:
			AchievementContent.get(1).delete(flush:true)
		then:
			AchievementContent.count() == 0
			
		when:
			new AchievementContent(type: ContentType.VIDEO, data: "Added".bytes, achievement: Achievement.get(1)).save(flush:true)
		then:
			AchievementContent.count() == 1
			new String(AchievementContent.get(2).data, "UTF-8") == "Added"
			AchievementContent.get(2).type == ContentType.VIDEO
    }
	
	void "test getStringData"()
	{
		expect:
			AchievementContent.get(1).getStringData() == "Testing Data"
	}
}
