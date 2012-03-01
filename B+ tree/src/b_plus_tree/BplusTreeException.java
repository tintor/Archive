package b_plus_tree;

/// <summary>
/// Generic error including programming errors.
/// </summary>
public class BplusTreeException extends Exception {
	public BplusTreeException(String message)// : base(message) 
	{
		// do nothing extra
		super(message);
	}
}