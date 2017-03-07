package geochallenge

class Point {
	String	title
	String	content
	double	longitude
	double	latitude
	
	static belongsTo	= [challenge: Challenge]
	
    static constraints = {
		title nullable: true
		longitude nullable:false
		latitude nullable:false
		content nullable:true
		challenge nullable:false
    }
}
