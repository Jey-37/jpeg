package com.example.jpeg.huffman;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;


public class BinaryStream
{
	private byte[] bits;
	private int length;
	private int pos;

	public BinaryStream() {
		bits = new byte[16];
	}

	public BinaryStream(int capacity) {
		bits = new byte[remDivOn8(capacity)+16];
	}

	public BinaryStream(BinaryStream bs) {
		bits = Arrays.copyOf(bs.bits, bs.bits.length);
		length = bs.length;
		pos = bs.pos;
	}

	public BinaryStream(byte[] array) {
		Objects.requireNonNull(array, "It's impossible to create stream of null array");
		bits = Arrays.copyOf(array, array.length+16);
		length = bits.length * 8;
	}

	public int length() {
		return length;
	}

	public int capacity() {
		return bits.length*8;
	}

	public void increaseCapacity(int newCapacity) {
		if (remDivOn8(newCapacity) > bits.length)
			bits = Arrays.copyOf(bits, remDivOn8(newCapacity));
	}

	public BinaryStream addOne() {
		return addNumber(1L, 1);
	}

	public BinaryStream addZero() {
		return addNumber(0L, 1);
	}

	public BinaryStream add(BinaryStream bs) {
		Objects.requireNonNull(bs, "Provided BinaryStream must not be null");

		if (bs.length() > (bits.length<<3) - length)
			bits = Arrays.copyOf(bits, remDivOn8(length+bs.length())+16);

		int ind = length >> 3;
		int offset = length % 8, rem = 0;
		int n = remDivOn8(bs.length());
		int mask = (1 << 8-offset)-1;
		for (int i = 0; i < n; i++, ind++) {
			bits[ind] |= (byte)(bs.bits[i]>>offset & mask | rem);
			rem = bs.bits[i] << (8-offset);
		}
		if (ind < bits.length)
			bits[ind] = (byte)rem;
		length += bs.length();

		return this;
	}

	public BinaryStream add(String bitString) {
		Objects.requireNonNull(bitString, "Provided bit string must not be null");

		if (bitString.length() > ((bits.length<<3)- length))
			bits = Arrays.copyOf(bits, remDivOn8(length +bitString.length())+16);

		int ind = length >> 3;
		int digit, offset = (remDivOn8(length)<<3)- length -1;
		if (offset == -1) offset = 7;
		for (int i = 0; i < bitString.length(); i++) {
			digit = Character.digit(bitString.charAt(i), 2);
			if (digit < 0)
				throw new NumberFormatException("Wrong bit string");
			bits[ind] |= (byte)(digit << offset--);
			if (offset == -1) {
				offset = 7;
				ind++;
			}
		}
		length += bitString.length();

		return this;
	}

	public BinaryStream add(int num) {
		return addNumber(num, 32);
	}

	public BinaryStream add(short num) {
		return addNumber(num, 16);
	}

	private BinaryStream addNumber(long num, int numberSize) {
		if (numberSize > ((bits.length<<3)- length))
			bits = Arrays.copyOf(bits, remDivOn8(length + numberSize)+16);

		int ind = length >> 3;
		int d = (remDivOn8(length)<<3) - length, offset, rem = numberSize;
		if (d == 0)
			d = 8;
		while (rem > 0) {
			if (d < rem) {
				offset = rem-d;
				bits[ind++] |= (byte)(num >>> offset);
				rem -= d;
				d = 8;
			} else {
				offset = d-rem;
				bits[ind] |= (byte)(num << offset);
				rem = 0;
			}
		}
		length += numberSize;

		return this;
	}

	public byte[] toArray() {
		return Arrays.copyOf(bits, remDivOn8(length));
	}

	public boolean getBit() {
		if (pos > length)
			throw new NoSuchElementException("It's impossible to get bit from binary stream");
		boolean bit = (bits[pos>>3] >> 7-pos%8 & 1) != 0;
		pos++;
		return bit;
	}

	public int getInt() {
		return (int)getNumber(32);
	}

	public short getShort() {
		return (short)getNumber(16);
	}

	private long getNumber(int numberSize) {
		if (pos+numberSize > length)
			throw new NoSuchElementException("It's impossible to get such number from binary stream");
		long res = 0;
		for (int offset = numberSize-1; offset >= 0; offset--) {
			long bit = bits[pos>>3] >> 7-pos%8 & 1L;
			pos++;
			res |= bit << offset;
		}
		return res;
	}

	public void clear() {
		length = 0;
		pos = 0;
		bits = new byte[16];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(length);
		int n = remDivOn8(length), l = 0;
		for (int i = 0; i < n; i++)
			for (int offset = 7; offset >= 0 && l < length; offset--, l++)
				sb.append(bits[i] >> offset & 1);
		return sb.toString();
	}

	private int remDivOn8(int d) {
		return d % 8 > 0 ? (d>>3)+1 : d>>3;
	}
}
