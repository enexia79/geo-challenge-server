package geochallenge

import java.util.Date;

class User {
	String 	name
	String 	surrogateId  // Represents the Unique Social ID this account was created with i.e. Facebook or Google
	Date	dateCreated
	Date	lastUpdated
	Boolean active			= Boolean.TRUE
	
	static hasMany = [achievements: Achievement, challenges: Challenge]
	
    static constraints = {
		name blank: false
		surrogateId blank: false, unique: true
		active nullable: false
		achievements nullable: true
		challenges nullable: true
    }
}
