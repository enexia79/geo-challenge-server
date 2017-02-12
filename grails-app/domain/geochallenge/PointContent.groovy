package geochallenge

class PointContent {
	ContentType type
	
	byte[]	data
	
	static belongsTo = [point: Point]
	
    static constraints = {
		type nullable: false
		data nullable: false
		point nullable: false
    }

	static mapping = {
		data sqlType:'longblob'
	}
	
	String getStringData() {
		return new String(data, "UTF-8")
	}
}
