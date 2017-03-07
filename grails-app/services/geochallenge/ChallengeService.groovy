package geochallenge

import java.util.Date

import grails.transaction.Transactional

@Transactional
class ChallengeService {

	public static final String SORT_POPULAR = "popular"
	public static final String SORT_NEARBY 	= "nearby"

    def create(challengeInfo, pointsInfo) {
		challengeInfo.latitude 	= pointsInfo[0].latitude;
		challengeInfo.longitude = pointsInfo[0].longitude;
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
	
	/**
	 * 
	 * @param user User search i.e. Challenged owned by this user (optional)
	 * @param longitude (gps coord of current location of requester) (optional)
	 * @param latitude (gps coord of current location of requester) (optional)
	 * @param radius (max distance from requester in meters) (optional)
	 * @param max (max number of results to be returned)
	 * @return list of challenges
	 */
	def search(searchCriteria) {
		def user 		= searchCriteria.user
		def longitude	= searchCriteria.longitude
		def latitude	= searchCriteria.latitude
		def radius		= searchCriteria.radius
		def max			= searchCriteria.max
		def sort		= searchCriteria.sort ? searchCriteria.sort : SORT_POPULAR
		
		if(max == null) 
			throw new RuntimeException("Missing required parameter max (max_results)")
		
		if(sort == SORT_NEARBY && (longitude == null || latitude == null || radius == null))
			throw new RuntimeException("Missing required parameter(s) for sort nearby : longitude, latitude & radius")
		
		def all = user ? Challenge.findAllByUser(user) : Challenge.getAll()
		def results = []
		if(longitude && latitude && radius)
			all.each { challenge ->
				if(!challenge.isExpired())
					if(challenge.getDistance(latitude, longitude) <= radius)
						results.push(challenge)
			}
		else
			all.each { challenge ->
				if(!challenge.isExpired())
					results.push(challenge)
			}
			
		if(sort == SORT_POPULAR) {
			results.sort { a, b ->
				b.getAchievementsCount() - a.getAchievementsCount()
			}
		}
		else if (sort == SORT_NEARBY) { 
			results.sort { a, b ->
				b.getDistance(latitude, longitude) - a.getDistance(latitude, longitude)
			}
		}
		else
			throw new RuntimeException("Unknown sort type")
		
		return results.take(max)
	}
	
	def toJSON(challenge) {
		def jsonObject = [title: challenge.title, description: challenge.description, dateCreated: challenge.dateCreated.getTime(), lastUpdated: challenge.lastUpdated.getTime(),
			expires: challenge.expires ? challenge.expires.getTime() : null, user: challenge.user.id, points: []]
		
		challenge.points.each { point ->
			jsonObject.points.push([title: point.title, latitude: point.latitude, longitude: point.longitude, content: point.content])
		}
		
		return jsonObject
	}
}
