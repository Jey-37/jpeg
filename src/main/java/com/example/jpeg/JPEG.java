package com.example.jpeg;

import com.example.jpeg.huffman.Huffman;
import com.example.jpeg.img.YCbCrColor;

import static com.example.jpeg.img.ImageUtil.*;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.logging.Logger;


public class JPEG
{
	private final static Logger logger = Logger.getLogger(JPEG.class.getName());

	private PressingTask pressingTask;

	public JPEG() {
	}

	public JPEG(PressingTask pressingTask) {
		this.pressingTask = pressingTask;
	}

	public byte[] pressImg(BufferedImage img, double q) {
		q = (int)(q*10)/10.0;

		Instant start = Instant.now();
		YCbCrColor[][] yccImg = RGB2YCbCr(img);
		pressingTask.updateProgress(0.12, 1.0);
		Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
		logger.info("RGB to YCbCr time: " + timeElapsed.toMillis());

		start = Instant.now();
		subsample(yccImg);
		pressingTask.updateProgress(0.14, 1.0);
		end = Instant.now();
        timeElapsed = Duration.between(start, end);
		logger.info("Subsampling time: " + timeElapsed.toMillis());

		start = Instant.now();
		short[][][] Gq = dct(yccImg, q);
		pressingTask.updateProgress(0.47, 1.0);
		end = Instant.now();
        timeElapsed = Duration.between(start, end);
		logger.info("DCT time: " + timeElapsed.toMillis());

		start = Instant.now();
		short[] rleImg = rle(Gq);
		pressingTask.updateProgress(0.52, 1.0);
		end = Instant.now();
        timeElapsed = Duration.between(start, end);
		logger.info("RLE time: " + timeElapsed.toMillis());

        var fullData = new short[rleImg.length+4];
     	fullData[0] = (short)q;
     	fullData[1] = (short)((int)(q*10)%10);
     	fullData[2] = (short)Gq.length;
     	fullData[3] = (short)Gq[0].length;
     	for (int i = 0, j = 4; i < rleImg.length; i++, j++)
     		fullData[j] = rleImg[i];

     	start = Instant.now();
     	byte[] res = Huffman.code(toCharArray(fullData));
		pressingTask.updateProgress(0.6, 1.0);
     	end = Instant.now();
        timeElapsed = Duration.between(start, end);
		logger.info("Huffman coding time: " + timeElapsed.toMillis());

     	return res;
	}

	public BufferedImage unpressImg(byte[] inBytes) throws Exception {

		Instant start = Instant.now();
		char[] decArray = Huffman.decode(inBytes);
		double time = pressingTask.getInitialProgress()+(1.0-pressingTask.getInitialProgress())*0.05;
		pressingTask.updateProgress(time, 1.0);
		Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
		logger.info("Huffman decoding time: " + timeElapsed.toMillis());

        short[] sArray = toShortArray(decArray);
        double q = sArray[0]+sArray[1]/10.0;
        int w = sArray[2], h = sArray[3];

        start = Instant.now();
        short[][][] Gq = irle(Arrays.copyOfRange(sArray, 4, sArray.length), w, h);
		time = pressingTask.getInitialProgress()+(1.0-pressingTask.getInitialProgress())*0.25;
		pressingTask.updateProgress(time, 1.0);
        end = Instant.now();
        timeElapsed = Duration.between(start, end);
		logger.info("IRLE time: " + timeElapsed.toMillis());

        start = Instant.now();
        YCbCrColor[][] yccImg = idct(Gq, q);
		time = pressingTask.getInitialProgress()+(1.0-pressingTask.getInitialProgress())*0.8;
		pressingTask.updateProgress(time, 1.0);
        end = Instant.now();
        timeElapsed = Duration.between(start, end);
		logger.info("IDCT time: " + timeElapsed.toMillis());

        start = Instant.now();
        BufferedImage rgbImg = YCbCr2RGB(yccImg);
		pressingTask.updateProgress(1.0, 1.0);
        end = Instant.now();
        timeElapsed = Duration.between(start, end);
		logger.info("YCbCr to RGB time: " + timeElapsed.toMillis());

        return rgbImg;
	}

	private static void subsample(YCbCrColor[][] imgMatr) {
		int avgCb, avgCr;
		for(int i = 0; i < imgMatr.length-1; i += 2) {
			for(int j = 0; j < imgMatr[0].length-1; j += 2) {
				avgCb = imgMatr[i][j].getCb() + imgMatr[i][j+1].getCb() + 
						imgMatr[i+1][j].getCb() + imgMatr[i+1][j+1].getCb();
				avgCr = imgMatr[i][j].getCr() + imgMatr[i][j+1].getCr() +
						imgMatr[i+1][j].getCr() + imgMatr[i+1][j+1].getCr();
				avgCb /= 4;
				avgCr /= 4;
				for(int k = i; k < i+2; k++) {
					for(int m = j; m < j+2; m++) {
						imgMatr[k][m].setCb(avgCb);
						imgMatr[k][m].setCr(avgCr);
					}
				}
			}
		}
	}

	private static short[][][] dct(YCbCrColor[][] imgMatr, double q) {
		int x = imgMatr.length/8*8, y = imgMatr[0].length/8*8;
		var A  = new double[8][8];
		var At = new double[8][8];
		var Q  = new double[8][8];

		for(int i = 0; i < 8; i++) {
			double eps = i == 0 ? Math.sqrt(0.125) : 0.5;
			for(int j = 0; j < 8; j++) {
				A[i][j] = eps * Math.cos((2*j+1)*i*Math.PI/16);
				At[j][i] = A[i][j];
				Q[i][j] = 1+(1+i+j)*q;
			}
		}

		var X  = new double[8][8][3];
		var X1 = new double[8][8][3];
		var Gq = new short[x][y][3];

		for(int i = 0; i < x; i += 8) {
			for(int j = 0; j < y; j += 8) {
				for(int k = 0; k < 8; k++) {
					for(int m = 0; m < 8; m++) {
						X1[k][m][0] = 0;
						X1[k][m][1] = 0;
						X1[k][m][2] = 0;

						for(int n = 0; n < 8; n++) {
							X1[k][m][0] += A[k][n]*imgMatr[i+n][j+m].getY();
							X1[k][m][1] += A[k][n]*imgMatr[i+n][j+m].getCb();
							X1[k][m][2] += A[k][n]*imgMatr[i+n][j+m].getCr();
						}
					}
				}

				for(int k = 0; k < 8; k++) {
					for(int m = 0; m < 8; m++) {
						X[k][m][0] = 0;
						X[k][m][1] = 0;
						X[k][m][2] = 0;
	
						for(int n = 0; n < 8; n++) {
							X[k][m][0] += X1[k][n][0]*At[n][m];
							X[k][m][1] += X1[k][n][1]*At[n][m];
							X[k][m][2] += X1[k][n][2]*At[n][m];
						}

						for(int n = 0; n < 3; n++) {
							X[k][m][n] /= Q[k][m];
							Gq[i+k][j+m][n] = (short)Math.round(X[k][m][n]);
						}
					}
				}
			}
		}
		return Gq;
	}

	private static YCbCrColor[][] idct(short[][][] Gq, double q) {
		int x = Gq.length, y = Gq[0].length;
		var A  = new double[8][8];
		var At = new double[8][8];
		var Q  = new double[8][8];

		for(int i = 0; i < 8; i++) {
			double eps = i == 0 ? Math.sqrt(0.125) : 0.5;
			for(int j = 0; j < 8; j++) {
				A[i][j] = eps * Math.cos((2*j+1)*i*Math.PI/16);
				At[j][i] = A[i][j];
				Q[i][j] = 1+(1+i+j)*q;
			}
		}

		var X  = new double[8][8][3];
		var X1 = new double[8][8][3];
		var X2 = new double[8][8][3];
		var imgMatr = new YCbCrColor[x][y];

		for(int i = 0; i < x; i += 8) {
			for(int j = 0; j < y; j += 8) {
				for(int k = 0; k < 8; k++)
					for(int m = 0; m < 8; m++)
						for(int n = 0; n < 3; n++)
							X2[k][m][n] = Gq[i+k][j+m][n]*Q[k][m];

				for(int k = 0; k < 8; k++) {
					for(int m = 0; m < 8; m++) {
						X1[k][m][0] = 0;
						X1[k][m][1] = 0;
						X1[k][m][2] = 0;

						for(int n = 0; n < 8; n++) {
							X1[k][m][0] += At[k][n]*X2[n][m][0];
							X1[k][m][1] += At[k][n]*X2[n][m][1];
							X1[k][m][2] += At[k][n]*X2[n][m][2];
						}
					}
				}

				for(int k = 0; k < 8; k++) {
					for(int m = 0; m < 8; m++) {
						X[k][m][0] = 0;
						X[k][m][1] = 0;
						X[k][m][2] = 0;
	
						for(int n = 0; n < 8; n++) {
							X[k][m][0] += X1[k][n][0]*A[n][m];
							X[k][m][1] += X1[k][n][1]*A[n][m];
							X[k][m][2] += X1[k][n][2]*A[n][m];
						}

						imgMatr[i+k][j+m] = new YCbCrColor(
							checkColor((int)Math.round(X[k][m][0])),
							checkColor((int)Math.round(X[k][m][1])),
							checkColor((int)Math.round(X[k][m][2])));
					}
				}
			}
		}
		return imgMatr;
	}

	private static short[] rle(short[][][] Gq) {
		int[][] zzinds = createZigZag();

		var res = new short[Gq.length*Gq[0].length*3];
		int ind = 0;
		short zCounter;

		for(int x = 0; x < 3; x++) {
			for(int i = 0; i < Gq.length; i += 8) {
				for(int j = 0; j < Gq[0].length; j += 8) {
					zCounter = 0;
					for(int k = 0; k < 64; k++) {
						if (Gq[i+zzinds[k][0]][j+zzinds[k][1]][x] == 0) {
							zCounter++;
						} else {
							if (zCounter > 0) {
								res[ind++] = 0;
								res[ind++] = zCounter;
								zCounter = 0;
							}
							res[ind++] = Gq[i+zzinds[k][0]][j+zzinds[k][1]][x];
						}
					}
					if (zCounter > 0) {
						res[ind++] = 0;
						res[ind++] = zCounter;
					}
				}
			}
		}

		return Arrays.copyOf(res, ind);
	}

	private static short[][][] irle(short[] arr, int n, int m) {
		int[][] zzinds = createZigZag();

		var Gq = new short[n][m][3];
		int ind = 0, zi = 0, i = 0, j = 0;
		int d = n*m;

		for(int k = 0; k < arr.length; k++) {
			if (arr[k] == 0) {
				zi += arr[k+1];
				ind += arr[k+1];
				k++;
			} else {
				Gq[i+zzinds[zi][0]][j+zzinds[zi][1]][ind/d] = arr[k];
				ind++;
				zi++;
			}
			if (zi > 63) {
				zi = 0;
				j += 8;
				if (j >= m) {
					j = 0;
					i += 8;
					if (i >= n) i = 0;
				}
			}
		}

		return Gq;
	}

	private static int[][] createZigZag() {
		int[][] zigZag = new int[64][2];
		int index = 0;
		for(int n = 0; n < 8; n++) {
  			int x = 0, y = 0;
  			if (n%2 == 0) {
   				x = 0; y = n;
   				for(int m = 0; m <= n; m++) {
    				zigZag[index][0] = y--;
    				zigZag[index][1] = x++;
    				index++;
   				}
  			} else {
   				x = n; y = 0;
   				for(int m = 0; m <= n; m++) {
    				zigZag[index][0] = y++;
    				zigZag[index][1] = x--;
    				index++;
   				}
  			}
		}
		for(int n = 1; n < 8; n++) {
  			int x = 0, y = 0;
  			if (n%2 == 1) {
   				x = n; y = 7;
   				for(int m = 0; m <= 7-n; m++) {
    				zigZag[index][0] = y--;
    				zigZag[index][1] = x++;
    				index++;
   				}
  			} else {
   				x = 7; y = n;
   				for(int m = 0; m <= 7-n; m++) {
    				zigZag[index][0] = y++;
    				zigZag[index][1] = x--;
    				index++;
   				}
  			}
		}
		return zigZag;
	}

	private static char[] toCharArray(short[] arr) {
		char[] cArr = new char[arr.length];
		for(int i = 0; i < arr.length; i++) {
			cArr[i] = (char)((int)arr[i] - Short.MIN_VALUE);
		}
		return cArr;
	}

	private static short[] toShortArray(char[] arr) {
		short[] sArr = new short[arr.length];
		for(int i = 0; i < arr.length; i++) {
			sArr[i] = (short)(arr[i] + Short.MIN_VALUE);
		}
		return sArr;
	}

	/*private static short[][][] dct(YCbCrColor[][] imgMatr, double q) {
		int x = imgMatr.length, y = imgMatr[0].length;
		var A  = new double[8][8];
		var At = new double[8][8];
		var Q  = new double[8][8];

		for(int i = 0; i < 8; i++) {
			double eps = i == 0 ? Math.sqrt(0.125) : 0.5;
			for(int j = 0; j < 8; j++) {
				A[i][j] = eps * Math.cos((2*j+1)*i*Math.PI/16);
				At[j][i] = A[i][j];
				Q[i][j] = 1+(1+i+j)*q;
			}
		}

		var X  = new double[8][8][3];
		var X1 = new double[8][8][3];
		var Gq = new short[x][y][3];
		int w, z;

		for(int i = 0; i < x; i += 8) {
			for(int j = 0; j < y; j += 8) {
				for(int k = 0; k < 8; k++) {
					for(int m = 0; m < 8; m++) {
						X1[k][m][0] = 0;
						X1[k][m][1] = 0;
						X1[k][m][2] = 0;

						z = j+m < y ? j+m : y-(j+m-y)-1;
	
						for(int n = 0; n < 8; n++) {
							w = i+n < x ? i+n : x-(i+n-x)-1;
							X1[k][m][0] += A[k][n]*imgMatr[w][z].getY();
							X1[k][m][1] += A[k][n]*imgMatr[w][z].getCb();
							X1[k][m][2] += A[k][n]*imgMatr[w][z].getCr();
						}
					}
				}

				for(int k = 0; k < 8; k++) {
					for(int m = 0; m < 8; m++) {
						X[k][m][0] = 0;
						X[k][m][1] = 0;
						X[k][m][2] = 0;
	
						for(int n = 0; n < 8; n++) {
							X[k][m][0] += X1[k][n][0]*At[n][m];
							X[k][m][1] += X1[k][n][1]*At[n][m];
							X[k][m][2] += X1[k][n][2]*At[n][m];
						}

						if (i+k < x && j+m < y) {
							for(int n = 0; n < 3; n++) {
								X[k][m][n] /= Q[k][m];
								Gq[i+k][j+m][n] = (short)Math.round(X[k][m][n]);
							}
						}
					}
				}
			}
		}
		return Gq;
	}

	private static YCbCrColor[][] idct(short[][][] Gq, double q) {
		int x = Gq.length, y = Gq[0].length;
		var A  = new double[8][8];
		var At = new double[8][8];
		var Q  = new double[8][8];

		for(int i = 0; i < 8; i++) {
			double eps = i == 0 ? Math.sqrt(0.125) : 0.5;
			for(int j = 0; j < 8; j++) {
				A[i][j] = eps * Math.cos((2*j+1)*i*Math.PI/16);
				At[j][i] = A[i][j];
				Q[i][j] = 1+(1+i+j)*q;
			}
		}

		var X  = new double[8][8][3];
		var X1 = new double[8][8][3];
		var X2 = new double[8][8][3];
		var imgMatr = new YCbCrColor[x][y];
		int w, z;

		for(int i = 0; i < x; i += 8) {
			for(int j = 0; j < y; j += 8) {
				for(int k = 0; k < 8; k++)
					for(int m = 0; m < 8; m++)
						if (i+k < x && j+m < y)
							for(int n = 0; n < 3; n++)
								X2[k][m][n] = Gq[i+k][j+m][n]*Q[k][m];

				for(int k = 0; k < 8; k++) {
					for(int m = 0; m < 8; m++) {
						X1[k][m][0] = 0;
						X1[k][m][1] = 0;
						X1[k][m][2] = 0;

						z = m < 8 ? m : 15-m;
	
						for(int n = 0; n < 8; n++) {
							w = n < 8 ? n : 15-n;
							X1[k][m][0] += At[k][n]*X2[w][z][0];
							X1[k][m][1] += At[k][n]*X2[w][z][1];
							X1[k][m][2] += At[k][n]*X2[w][z][2];
						}
					}
				}

				for(int k = 0; k < 8; k++) {
					for(int m = 0; m < 8; m++) {
						X[k][m][0] = 0;
						X[k][m][1] = 0;
						X[k][m][2] = 0;
	
						for(int n = 0; n < 8; n++) {
							X[k][m][0] += X1[k][n][0]*A[n][m];
							X[k][m][1] += X1[k][n][1]*A[n][m];
							X[k][m][2] += X1[k][n][2]*A[n][m];
						}

						if (i+k < x && j+m < y) {
							imgMatr[i+k][j+m] = new YCbCrColor(
								ImageUtil.checkColor((int)Math.round(X[k][m][0])),
								ImageUtil.checkColor((int)Math.round(X[k][m][1])),
								ImageUtil.checkColor((int)Math.round(X[k][m][2])));
						}
					}
				}
			}
		}
		return imgMatr;
	}

	private static short[] rle(short[][][] Gq) {
		int[][] zzinds = createZigZag();

		var res = new short[Gq.length*Gq[0].length*3];
		int ind = 0;
		short zCounter;

		for(int x = 0; x < 3; x++) {
			for(int i = 0; i < Gq.length; i += 8) {
				for(int j = 0; j < Gq[0].length; j += 8) {
					zCounter = 0;
					for(int k = 0; k < 64; k++) {
						if (i+zzinds[k][0] < Gq.length && j+zzinds[k][1] < Gq[0].length) {
							if (Gq[i+zzinds[k][0]][j+zzinds[k][1]][x] == 0) {
								zCounter++;
							} else {
								if (zCounter > 0) {
									res[ind++] = 0;
									res[ind++] = zCounter;
									zCounter = 0;
								}
								res[ind++] = Gq[i+zzinds[k][0]][j+zzinds[k][1]][x];
							}
						}
					}
					if (zCounter > 0) {
						res[ind++] = 0;
						res[ind++] = zCounter;
					}
				}
			}
		}

		return Arrays.copyOf(res, ind);
	}

	private static short[][][] irle(short[] arr, int n, int m) {
		int[][] zzinds = createZigZag();

		var Gq = new short[n][m][3];
		int ind = 0, zi = 0, i = 0, j = 0;
		int d = n*m;

		for(int k = 0; k < arr.length; k++) {
			if (arr[k] == 0) {
				if (i+8 >= n || j+8 >= m) {
					int e = arr[k+1];
					for(; zi < 64; zi++)
						if (i+zzinds[zi][0] < n && j+zzinds[zi][1] < m)
							if (--e == 0)
								break;
					zi++;
				} else
					zi += arr[k+1];
				ind += arr[k+1];
				k++;
			} else {
				if (i+zzinds[zi][0] >= n || j+zzinds[zi][1] >= m)
					for(; zi < 64; zi++)
						if (i+zzinds[zi][0] < n && j+zzinds[zi][1] < m)
							break;
				if (zi > 63) {
					zi = 0;
					j += 8;
					if (j >= m) {
						j = 0;
						i += 8;
						if (i >= n) i = 0;
					}
				}
				Gq[i+zzinds[zi][0]][j+zzinds[zi][1]][ind/d] = arr[k];
				ind++;
				zi++;
			}
			if (zi > 63) {
				zi = 0;
				j += 8;
				if (j >= m) {
					j = 0;
					i += 8;
					if (i >= n) i = 0;
				}
			}
		}

		return Gq;
	}*/
}
