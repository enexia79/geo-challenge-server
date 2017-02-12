package geochallenge

import java.util.Date;

class Achievement {

	Challenge 			challenge
	Date 				dateCreated
	Date 				lastUpdated
	
	static belongsTo 	= [user: User]
	static hasOne		= [content: AchievementContent]
	
    static constraints = {
		challenge nullable: false
		content nullable: true
		user nullable: false
    }
}
