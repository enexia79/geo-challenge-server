package geochallenge;

public enum Direction
{
	NORTH ("north"),
	SOUTH ("south"),
	EAST ("east"),
	WEST ("west")
	
	private final String code;
	
	Direction(String code)
	{
		this.code = code;
	}
	
	public String toString()
	{
		return code;
	}
	
	public boolean equals(Locale other)
	{
		return code.equals(other.code);
	}
	
	public static Direction valueOfCode(String code)
	{
		values().find { it.code == code }
	}
}
