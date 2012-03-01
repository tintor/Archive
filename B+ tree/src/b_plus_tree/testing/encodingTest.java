package b_plus_tree.testing;

import java.util.*;

import b_plus_tree.*;

public class encodingTest 
{

public static void main(String argv[]) 
			throws Exception
{
	hBplusTreeBytes HT = (hBplusTreeBytes) 
		hBplusTreeBytes.Initialize("/tmp/junk.bin", "/tmp/junk2.bin", 6);
	String stuff = "cæser";
	String test = HT.PrefixForByteCount(stuff, 5);
	System.out.println("test="+test);
	//HT[stuff] = "goober";
	byte[] bytes = new byte[0];
	HT.set(stuff, bytes);
}

}