package com.example.jpeg.img;


public class YCbCrColor
{
	private int y, cb, cr;

	public YCbCrColor(int ycc) {
		y = ycc & 0xFF;
		cb = (ycc >> 8) & 0xFF;
		cr = (ycc >> 16) & 0xFF;
	}

	public YCbCrColor(int _y, int _cb, int _cr) {
		y = _y; cb = _cb; cr = _cr;
	}

	public YCbCrColor(YCbCrColor newColor) {
		y = newColor.getY();
		cb = newColor.getCb();
		cr = newColor.getCr();
	}

	public int getY() { return y; }
	public int getCb() { return cb; }
	public int getCr() { return cr; }
	public int getYCbCr() { return cr<<16 | cb<<8 | y; }
	public void setY(int _y) { y = _y; }
	public void setCb(int _cb) { cb = _cb; }
	public void setCr(int _cr) { cr = _cr; }
}
