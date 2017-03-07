package geochallenge

import java.util.Date;

class Achievement {

	Challenge 			challenge
	String				content
	Date 				dateCreated
	Date 				lastUpdated
	
	static belongsTo 	= [user: User]
	
    static constraints = {
		challenge nullable: false
		content nullable: true
		user nullable: false
    }
}
