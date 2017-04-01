package geochallenge

import grails.transaction.Transactional

@Transactional
class AchievementService {

    def create(achievementInfo) {
		def achievement = new Achievement(achievementInfo);
		if(achievement.save())
			return achievement
		else
			throw new RuntimeException("Unable to save challenge")
    }
	
	def getAllByUser(user) {
		return Achievement.findAllByUser(user, [sort: "dateCreated", order: "desc"])
	}
	
	def getAllByChallenge(challenge) {
		return Achievement.findAllByChallenge(challenge, [sort: "dateCreated", order: "desc"])
	}
	
	def toJSON(data) {
		def jsonObject
		
		if(data instanceof List) {
			jsonObject = []
			data.each { challenge ->
				jsonObject.push(toJSON(challenge))
			}
		}
		else 
			jsonObject = [name: data.user.name, user: data.user.id, challenge: data.challenge.id, content: data.content, dateCreated: data.dateCreated.getTime(), lastUpdated: data.lastUpdated.getTime()]
		
		return jsonObject
	}
}
