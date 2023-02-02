package com.example.jpeg.img;

import java.awt.Color;
import java.awt.image.BufferedImage;


public class ImageUtil
{
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

	public static YCbCrColor[][] RGB2YCbCr(BufferedImage rgbImage) {
		int w = rgbImage.getWidth(), h = rgbImage.getHeight();
		YCbCrColor[][] yccMatrix = new YCbCrColor[w][h];
		for(int i = 0; i < w; i++)
			for(int j = 0; j < h; j++)
				yccMatrix[i][j] = ImageUtil.RGB2YCbCr(new Color(rgbImage.getRGB(i,j)));
		return yccMatrix;
	}

	public static BufferedImage YCbCr2RGB(YCbCrColor[][] yccImage) {
		int w = yccImage.length, h = yccImage[0].length;
		var rgbImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for(int i = 0; i < w; i++)
			for(int j = 0; j < h; j++)
				rgbImg.setRGB(i, j, ImageUtil.YCbCr2RGB(yccImage[i][j]).getRGB());
		return rgbImg;
	}

	public static YCbCrColor RGB2YCbCr(Color rgb) {
		int r = rgb.getRed(), g = rgb.getGreen(), b = rgb.getBlue();
		int y  = (int)(RtoY[r] + GtoY[g] + BtoY[b]);
		int cb = 128 + (int)(RtoCb[r] + GtoCb[g] + BtoCb[b]);
		int cr = 128 + (int)(RtoCr[r] + GtoCr[g] + BtoCr[b]);
		return new YCbCrColor(y, cb, cr);
	}

	public static Color YCbCr2RGB(YCbCrColor ycc) {
		int y = ycc.getY(), cb = ycc.getCb(), cr = ycc.getCr();
		int r = (int)(y + CrtoR[cr]);
		int g = (int)(y + CbtoG[cb] + CrtoG[cr]);
		int b = (int)(y + CbtoB[cb]);
		return new Color(checkColor(r), checkColor(g), checkColor(b));
	}

	public static int checkColor(int c) {
		if (c < 0)
			c = 0;
		if (c > 255)
			c = 255;
		return c;
	}
}
