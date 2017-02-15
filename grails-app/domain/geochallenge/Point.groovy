package geochallenge

class Point {
	String	title
	double	longitude
	double	latitude
	
	static hasOne 		= [content: PointContent]
	static belongsTo	= [challenge: Challenge]
	
    static constraints = {
		title nullable: true
		longitude nullable:false
		latitude nullable:false
		content nullable:true
		challenge nullable:false
    }
}
