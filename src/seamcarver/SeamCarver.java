/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seamcarver;

/**
 *
 * @author manju
 */
import java.awt.Color;
import seamcarver.ui.Picture;

public class SeamCarver {
	private final double BORDER_ENERGY = 1000.0;
	private Picture picture;
	private double[] distTo;
	private int[] edgeTo;
	private double[] picEnergy;
	public SeamCarver(Picture picture) {
		if (picture == null) throw new IllegalArgumentException("Null argument");
		this.picture = picture;
	}

	public Picture picture() {
		return picture;
	}

	public int width() {
		return picture.width();
	}

	public int height() {
		//returns number of rows(pixels)
		return picture.height();
	}

	public double energy(int col, int row) {
		if (col == 0 || row == 0 || col == width()-1 || row == height()-1) {
			return BORDER_ENERGY;
		}
		int pixel = pixelNumber(col,row);
		if (picEnergy[pixel] != 0.0)
			return picEnergy[pixel];
		return Math.sqrt(gradX(col, row) + gradY(col, row));
	}

	private double gradX(int col, int row) {
		Color leftPixelColor = picture.get(col-1,row);
		Color rightPixelColor = picture.get(col+1,row);
		double Rx = rightPixelColor.getRed() - leftPixelColor.getRed();
		double Gx = rightPixelColor.getGreen() - leftPixelColor.getGreen();
		double Bx = rightPixelColor.getBlue() - leftPixelColor.getBlue();
		return (Rx*Rx)+(Gx*Gx)+(Bx*Bx);
	}

	private double gradY(int col, int row) {
		Color upperPixelColor = picture.get(col,row+1);
		Color lowerPixelColor = picture.get(col,row-1);
		double Ry = lowerPixelColor.getRed() - upperPixelColor.getRed();
		double Gy = lowerPixelColor.getGreen() - upperPixelColor.getGreen();
		double By = lowerPixelColor.getBlue() - upperPixelColor.getBlue();
		return (Ry*Ry)+(Gy*Gy)+(By*By);
	}

	public int[] findHorizontalSeam() {
		distTo = new double[width() * height()];
		edgeTo = new int[width() * height()];
		picEnergy = new double[width() * height()];
		for (int row = 0; row < height(); row++) {
			for (int col = 0; col < width() ; col++) {
				int pixel = pixelNumber(col, row);
				if (col == 0)	distTo[pixel] = 0.0;
				else distTo[pixel] = Double.POSITIVE_INFINITY;
				edgeTo[pixel] = -1;
				picEnergy[pixel] = energy(col,row);
			}
		}

		for (int col = 0; col < width()-1; col++) {
			for (int row = 0; row < height(); row++) {
				int pixel = pixelNumber(col, row);
				if (row-1 >= 0) relax(pixel, pixelNumber(col+1, row-1));
				relax(pixel, pixelNumber(col+1, row));
				if (row+1 <= height()-1) relax(pixel, pixelNumber(col+1, row+1));
			}
		}
		double rowMin = Double.POSITIVE_INFINITY;
		int lastSeamPixel = -1;
		for (int row = 0; row < height(); row++) {
			int pixel = pixelNumber(width()-1,row);
			if (distTo[pixel] < rowMin) {
				rowMin = distTo[pixel];
				lastSeamPixel = pixel;
			}
		}
		return horizSeam(lastSeamPixel);
	}

	public int[] findVerticalSeam() {
		distTo = new double[width() * height()];
		edgeTo = new int[width() * height()];
		picEnergy = new double[width() * height()];
		for (int col = 0; col < width(); col++) {
			for (int row = 0; row < height(); row++) {
				int pixel = pixelNumber(col, row);
				if (row == 0) distTo[pixel] = 0.0;
				else distTo[pixel] = Double.POSITIVE_INFINITY;
				edgeTo[pixel] = -1;
				picEnergy[pixel] = energy(col,row);
			}
		}

		for (int row = 0; row < height()-1; row++) {
			for (int col = 0; col < width(); col++) {
				int pixel = pixelNumber(col, row);
				if (col-1 >= 0) relax(pixel,pixelNumber(col-1,row+1));
				relax(pixel,pixelNumber(col,row+1));
				if (col+1 <= width()-1) relax(pixel,pixelNumber(col+1,row+1));
			}
		}
		double colMin = Double.POSITIVE_INFINITY;
		int lastSeamPixel = -1;
		for (int col = 0; col < width(); col++) {
			int pixel = pixelNumber(col,height()-1);
			if (distTo[pixel] < colMin) {
				colMin = distTo[pixel];
				lastSeamPixel = pixel;
			}
		}
		return verticalSeam(lastSeamPixel);
	}

	public void removeHorizontalSeam(int[] seam) {
		if (height() <= 1) throw new IllegalArgumentException("height <= 1");
		if (seam.length != width()) throw new IllegalArgumentException("Seam length not equal to width");
		Picture updatedPic = new Picture(width(), height()-1);
		for (int col = 0; col < width(); col++) {
			for (int row = 0 ; row < seam[col]; row++) {
					updatedPic.set(col, row, picture.get(col, row));
					int pixel = pixelNumber(col,row);
			}
			for (int row = seam[col]+1; row < height(); row++) {
				updatedPic.set(col, row-1, picture.get(col, row));
				int pixel = pixelNumber(col,row-1);
			}
		}
		picture = updatedPic;
		picEnergy = null;
		distTo = null;
		edgeTo = null;
	}

	public void removeVerticalSeam(int[] seam) {
		if (width() <= 1) throw new IllegalArgumentException("width <= 1");
		if (seam.length != height()) throw new IllegalArgumentException("Seam length not equal to height");
		Picture updatedPic = new Picture(width()-1, height());
		for (int row = 0; row < height(); row++) {
			for (int col = 0; col < seam[row]; col++) {
				updatedPic.set(col, row, picture.get(col,row));
				int pixel = pixelNumber(col,row);
			}
			for (int col = seam[row]+1; col < width(); col++) {
				updatedPic.set(col-1, row, picture.get(col,row));
				int pixel = pixelNumber(col-1, row);
			}
		}
		picture = updatedPic;
		picEnergy = null;
		distTo = null;
		edgeTo = null;
	}

	private int[] horizSeam(int lastSeamPixel) {
		int[] newSeam = new int[width()];
		for (int v = lastSeamPixel; v >= 0; v=edgeTo[v]) {
			int row = pixelRow(v);
			int col = pixelColumn(v);
			newSeam[col] = row;
		}
		return newSeam;
	}

	private int[] verticalSeam(int lastSeamPixel) {
		int[] newSeam = new int[height()];
		for (int v = lastSeamPixel; v >= 0; v=edgeTo[v]) {
			int row = pixelRow(v);
			int col = pixelColumn(v);
			newSeam[row] = col;
		}
		return newSeam;
	}

	private void relax(int v, int w) {
		if (distTo[w] > distTo[v] + picEnergy[w]) {
			distTo[w] = distTo[v] + picEnergy[w];
			edgeTo[w] = v;
		}
	}

	private int pixelRow(int pixel) {
		return pixel / width();
	}

	private int pixelColumn(int pixel) {
		return pixel % width();
	}

	private int pixelNumber(int col, int row) {
		return (width()*row + col);
	}

}
