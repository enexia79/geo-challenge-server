package geochallenge

class AchievementContent {
	ContentType type
	
	byte[]	data
	
	static belongsTo = [achievement: Achievement]
	
    static constraints = {
		type nullable: false
		data nullable: false
		achievement nullable: false
    }

	static mapping = {
		data sqlType:'longblob'
	}
	
	String getStringData() {
		return new String(data, "UTF-8")
	}
}
