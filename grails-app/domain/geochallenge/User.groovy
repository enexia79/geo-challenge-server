package geochallenge

import java.util.Date;

class User {
	String 	name
	String 	email
	Date	dateCreated
	Date	lastUpdated
	Boolean active			= Boolean.TRUE
	
	static hasMany = [achievements: Achievement, challenges: Challenge]
	
    static constraints = {
		name blank: false
		email blank: false, unique: true, email: true
		active nullable: false
		achievements nullable: true
		challenges nullable: true
    }
}
