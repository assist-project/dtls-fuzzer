package se.uu.it.modeltester.test;

public class Fragment {
	private final int offset;
	private final int length;
	
	public Fragment(int offset, int length) {
		this.offset = offset; 
		this.length = length;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}
	
	public String toString() {
		return "F[" + offset + "," + length + ">";
	}
}
