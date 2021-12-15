package org.eclipse.californium.scandium.examples;

import java.math.BigInteger;
import java.util.Arrays;

import com.beust.jcommander.IStringConverter;

public class HexStringToBytesConverter implements IStringConverter<byte []>{

	@Override
	public byte[] convert(String value) {
		long longVal = Long.parseUnsignedLong(value, 16);
		BigInteger bitIntVal = BigInteger.valueOf(longVal);
		return bitIntVal.toByteArray();
	}
	
	public static void main (String args []) {
		HexStringToBytesConverter conv = new HexStringToBytesConverter();
		byte [] bytes = conv.convert("1234");
		System.out.println(Arrays.toString(bytes) + " " + Arrays.toString(new byte [] {0x12, 0x34}));
	}

}