package com.example.jpeg.img;

import java.awt.*;
import static com.example.jpeg.img.ImageUtil.checkColor;

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

	public static YCbCrColor ofRgbColor(Color rgb) {
		int r = rgb.getRed(), g = rgb.getGreen(), b = rgb.getBlue();
		int y  = (int)(RtoY[r] + GtoY[g] + BtoY[b]);
		int cb = 128 + (int)(RtoCb[r] + GtoCb[g] + BtoCb[b]);
		int cr = 128 + (int)(RtoCr[r] + GtoCr[g] + BtoCr[b]);
		return new YCbCrColor(y, cb, cr);
	}

	public Color toRGBColor() {
		int r = (int)(y + CrtoR[cr]);
		int g = (int)(y + CbtoG[cb] + CrtoG[cr]);
		int b = (int)(y + CbtoB[cb]);
		return new Color(checkColor(r), checkColor(g), checkColor(b));
	}

	public int getY() { return y; }
	public int getCb() { return cb; }
	public int getCr() { return cr; }
	public int getYCbCr() { return cr<<16 | cb<<8 | y; }
	public void setY(int _y) { y = _y; }
	public void setCb(int _cb) { cb = _cb; }
	public void setCr(int _cr) { cr = _cr; }

	private static final double[] RtoY = new double[256];
	private static final double[] RtoCb = new double[256];
	private static final double[] RtoCr = new double[256];
	private static final double[] GtoY = new double[256];
	private static final double[] GtoCb = new double[256];
	private static final double[] GtoCr = new double[256];
	private static final double[] BtoY = new double[256];
	private static final double[] BtoCb = new double[256];
	private static final double[] BtoCr = new double[256];

	private static final double[] CrtoR = new double[256];
	private static final double[] CbtoG = new double[256];
	private static final double[] CrtoG = new double[256];
	private static final double[] CbtoB = new double[256];
	static {
		for(int i = 0; i < 256; i++) {
			RtoY[i]  = 0.299*i;
			RtoCb[i] = -0.168736*i;
			RtoCr[i] = 0.5*i;
			GtoY[i]  = 0.587*i;
			GtoCb[i] = -0.331264*i;
			GtoCr[i] = -0.418688*i;
			BtoY[i]  = 0.114*i;
			BtoCb[i] = 0.5*i;
			BtoCr[i] = -0.081312*i;

			CrtoR[i] = 1.402*(i-128);
			CbtoG[i] = -0.344136*(i-128);
			CrtoG[i] = -0.714136*(i-128);
			CbtoB[i] = 1.772*(i-128);
		}
	}
}
