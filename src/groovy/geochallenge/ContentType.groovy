package geochallenge;

public enum ContentType
{
	TEXT ("text"),
	VIDEO ("video"),
	PICTURE ("picture")
	
	private final String type;
	
	ContentType(String type)
	{
		this.type = type;
	}
	
	public String toString()
	{
		return type;
	}
	
	public boolean equals(ContentType other)
	{
		return type.equals(other.type);
	}
	
	public static ContentType valueOfCode(String type)
	{
		values().find { it.type == type }
	}
}
