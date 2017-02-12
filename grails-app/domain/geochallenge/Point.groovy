package geochallenge

class Point {
	String	title
	double	longitude
	double	latitude
	
	static hasOne 	= [content: PointContent]
	
    static constraints = {
		title nullable: true
		longitude nullable:false
		latitude nullable:false
		content nullable:true
    }
}
