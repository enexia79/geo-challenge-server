package geochallenge

import java.util.Date

import grails.transaction.Transactional

@Transactional
class ChallengeService {

    def create(challengeInfo, pointsInfo) {
		def challenge = new Challenge(challengeInfo);
		if(challenge.save())
		{
			pointsInfo.each { pointInfo ->
				def point = new Point(title: pointInfo.title, content: pointInfo.content, longitude: pointInfo.longitude, latitude: pointInfo.latitude, challenge: challenge)
				if(challenge.addToPoints(point).save(flush:true)) {
				}
				else {
					challenge.delete()
					
					throw new RuntimeException("Unable to save point")
				}
			}
			
			return challenge
		}
		else
			throw new RuntimeException("Unable to save challenge")
    }
	
	def toJSON(challenge) {
		def jsonObject = [title: challenge.title, description: challenge.description, dateCreated: challenge.dateCreated.getTime(), lastUpdated: challenge.lastUpdated.getTime(),
			expires: challenge.expires ? challenge.expires.getTime() : null, user: challenge.user.id, points: []]
		
		challenge.points.each { point ->
			jsonObject.points.push([title: point.title, longitude: point.longitude, latitude: point.latitude, content: point.content])
		}
		
		return jsonObject
	}
	
}
