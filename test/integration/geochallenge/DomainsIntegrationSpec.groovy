package geochallenge

import grails.test.spock.IntegrationSpec

class DomainsIntegrationSpec extends IntegrationSpec {

    def setup() {
		def user = new User(name: "Billy", surrogateId: "blah@nobody.net")
		user.save()
		def challenge = new Challenge(title: "title", description: "description", user: user)
		challenge.save()
		user.addToChallenges(challenge).save()
		def point = new Point(longitude: 20.0, latitude: 3.5, challenge: challenge)
		point.save()
		challenge.addToPoints(point).save()
		point = new Point(longitude: 21.1, latitude: 2.2, challenge: challenge, content: "Testing Data")
		point.save()
		challenge.addToPoints(point).save()
		def achievement = new Achievement(user: user, challenge: challenge, content: "Testing Data")
		achievement.save()
    }

    def cleanup() {
    }

    void "test cascade delete"() {
		expect:
			User.count() == 1
			Challenge.count() == 1
			Point.count() == 2
			Achievement.count() == 1
			Challenge.get(1).title == "title"
			Challenge.get(1).description == "description"
			Challenge.get(1).user == User.get(1)
			Challenge.get(1).points?.size() == 2
			
		when:
			Achievement.get(1).delete()
		then:
			Achievement.count() == 0
			
		when:
			User.get(1).removeFromChallenges(Challenge.get(1)).save()
			Challenge.get(1).delete()
		then:
			Challenge.count() == 0
			Point.count() == 0
    }
	
	void "test user cascade delete"() {
		expect:
			User.count() == 1
			Challenge.count() == 1
			Point.count() == 2
			Achievement.count() == 1
			Challenge.get(2).title == "title"
			Challenge.get(2).description == "description"
			Challenge.get(2).user == User.get(2)
			Challenge.get(2).points?.size() == 2
			
		when:
			def user = User.get(2)
			user.removeFromAchievements(Achievement.get(2)).save()
			Achievement.get(2).delete()
			user.delete()
		then:
			User.count() == 0
			Achievement.count() == 0
			Challenge.count() == 0
			Point.count() == 0
	}
}
