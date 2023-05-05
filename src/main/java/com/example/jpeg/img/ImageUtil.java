package com.example.jpeg.img;

import java.awt.Color;
import java.awt.image.BufferedImage;


public class ImageUtil
{
	public static YCbCrColor[][] RGB2YCbCr(BufferedImage rgbImage) {
		int w = rgbImage.getWidth(), h = rgbImage.getHeight();
		YCbCrColor[][] yccMatrix = new YCbCrColor[w][h];
		for(int i = 0; i < w; i++)
			for(int j = 0; j < h; j++)
				yccMatrix[i][j] = YCbCrColor.ofRgbColor(new Color(rgbImage.getRGB(i,j)));
		return yccMatrix;
	}

	public static BufferedImage YCbCr2RGB(YCbCrColor[][] yccImage) {
		int w = yccImage.length, h = yccImage[0].length;
		var rgbImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for(int i = 0; i < w; i++)
			for(int j = 0; j < h; j++)
				rgbImg.setRGB(i, j, yccImage[i][j].toRGBColor().getRGB());
		return rgbImg;
	}

	public static int checkColor(int c) {
		if (c < 0)
			c = 0;
		if (c > 255)
			c = 255;
		return c;
	}
}
