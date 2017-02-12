package geochallenge

import java.util.Date;

class Challenge {
	String	title
	String	description
	Date	dateCreated
	Date	lastUpdated
	Date	expires
	User	user
	
	static hasMany 		= [points: Point]
	//static belongsTo 	= [user: User]
	
    static constraints = {
		title blank: false
		description nullable: true
		expires nullable: true
		user nullable: false
    }
}