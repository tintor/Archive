package tintor.sokoban;

public class Code {
	public static final char Wall = '#';
	public static final char Agent = '@';
	public static final char AgentOnGoal = '+';
	public static final char Box = '$';
	public static final char BoxOnGoal = '*';
	public static final char Goal = '.';
	public static final char Space = ' ';

	// Special!
	public static final char GoalRoom = 'G'; // multiple box destination 
	public static final char Dispenser = 'D'; // multiple box source
	
	public static final char Unreachable = '!';
	public static final char UnreachableGoal = '?';
	public static final char Articulation = '~';
	
	private Code() {}
}