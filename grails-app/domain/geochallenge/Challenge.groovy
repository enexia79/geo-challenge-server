package geochallenge

import java.util.Date;

class Challenge {
	String	title
	String	description
	Double	latitude
	Double	longitude
	Date	dateCreated
	Date	lastUpdated
	Date	expires
	Set		points = new LinkedHashSet()
	
	static hasMany 		= [points: Point]
	static belongsTo 	= [user: User]
	
    static constraints = {
		title blank: false
		description nullable: true
		expires nullable: true
		user nullable: false
		latitude: nullable: false
		longitude: nullable: false
    }
	
	def getAchievements() {
		return Achievement.findAllByChallenge(this)
	}
	
	def getAchievementsCount() {
		return getAchievements().size()
	}
	
	def getDistance(latitude, longitude) {
		return distance(latitude, longitude, this.latitude, this.longitude)
	}
	
	public boolean isExpired() {
		if(expires) {
			return expires.compareTo(new Date()) < 0
		}
		else
			return false
	}
	
	public List getPointsInOrder() {
		return Point.findAllByChallenge(this, [sort: "id", order: "asc"]);
	}
	
	/**
	 * Calculate distance between two points in latitude and longitude taking
	 * into account height difference. If you are not interested in height
	 * difference pass 0.0. Uses Haversine method as its base.
	 *
	 * latitude, longitude Start point latitude2, longitude2 End point elevation Start altitude in meters
	 * elevation2 End altitude in meters
	 * @returns Distance in Meters
	 */
	public static double distance(latitude, longitude, latitude2,
			longitude2, elevation = 0.0, elevation2 = 0.0) {
	
		final int R = 6378; // Radius of the earth
	
		Double latDistance = Math.toRadians(latitude2 - latitude);
		Double lonDistance = Math.toRadians(longitude2 - longitude);
		Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(latitude2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters
	
		double height = elevation - elevation2;
		
		distance = Math.pow(distance, 2) + Math.pow(height, 2);
	
		return Math.sqrt(distance);
	}
	
	/**
	 * latitude, longitude Start point Direction ENUM (NORTH, SOUTH, EAST, WEST) and distance in meters
	 * @returns Transposed gps point in given direction with given distance
	 */
	public static def transposePoint(latitude, longitude, direction, distance) {
		Double newLatitude, newLongitude;
		
		final int 		DNS = 111111; // estimated distance for 1 degree latitude
		final double	DEW = 111321*Math.cos(Math.toRadians(latitude)); // estimated distance for 1 degree longitude
		
		switch(direction) {
			case Direction.NORTH:
				newLatitude 	= latitude + distance*(1/DNS)
				newLongitude 	= longitude
				break;
			case Direction.SOUTH:
				newLatitude 	= latitude - distance*(1/DNS)
				newLongitude 	= longitude
				break;
			case Direction.EAST:
				newLatitude 	= latitude
				newLongitude 	= longitude + distance*(1/DEW)
				break;
			case Direction.WEST:
				newLatitude 	= latitude
				newLongitude 	= longitude - distance*(1/DEW)
				break;
		}
		
		return [latitude: newLatitude, longitude: newLongitude];
	}
}
