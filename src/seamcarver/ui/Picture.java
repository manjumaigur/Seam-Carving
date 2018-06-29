/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seamcarver.ui;

/**
 *
 * @author manju
 */
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class Picture implements ActionListener {
	private BufferedImage image;
	private JFrame frame;	//for onscreen view
	private String filename;
	private boolean isOriginUpperLeft = true;
	private final int width, height;

	public Picture(int width, int height) {
		if (width < 0 || height < 0) throw new IllegalArgumentException("dimensions should bew non negative");
		this.width = width;
		this.height = height;
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}

	public Picture(Picture picture) {
		if (picture == null) throw new IllegalArgumentException("Null argument");
		width = picture.width();
		height = picture.height();
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		filename = picture.filename;
		isOriginUpperLeft = picture.isOriginUpperLeft;
		for (int col = 0; col < width(); col++)
			for (int row = 0; row < height(); row++)
				image.setRGB(col, row, picture.image.getRGB(col, row));
	}

	public Picture(String filename) {
		if (filename == null) throw new IllegalArgumentException("null argument");
		this.filename = filename;
		try {
			File file = new File(filename);
			if (file.isFile()) {
				image = ImageIO.read(file);
			}
			//try to read from file in same directory as this.class file
			else {
				URL url = getClass().getResource(filename);
				if (url == null) {
					url = new URL(filename);
				}
				image = ImageIO.read(url);
			}
			if (image == null) {
				throw new IllegalArgumentException("could not read image file ");
			}
			width = image.getWidth(null);
			height = image.getHeight(null);
		}
		catch (IOException ioe) {
			throw new IllegalArgumentException("could not open file ");
		}
	}

	public Picture(File file) {
		if (file == null) throw new IllegalArgumentException("null argument");
		try {
			image = ImageIO.read(file);
		}
		catch (IOException ioe) {
			throw new IllegalArgumentException("could not read file");
		}
		if (image == null) {
			throw new IllegalArgumentException("could not open file");
		}
		width = image.getWidth(null);
		height = image.getHeight(null);
		filename = file.getName();
	}

	public void setOriginUpperLeft() {
		isOriginUpperLeft = true;
	}

	public void setOriginLowerLeft() {
		isOriginUpperLeft = false;
	}

	public int height() {
		return height;
	}

	public int width() {
		return width;
	}

	public JLabel getJLabel() {
		if (image == null) return null;
		ImageIcon icon = new ImageIcon(image);
		return new JLabel(icon);
	}

	public void show() {
		if (frame == null) {
			frame = new JFrame();
			JMenuBar menuBar = new JMenuBar();
			JMenu menu = new JMenu("File");
			menuBar.add(menu);
			JMenuItem menuItem1 = new JMenuItem("Save ..");
			menuItem1.addActionListener(this);
			menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			menu.add(menuItem1);
			frame.setJMenuBar(menuBar);

			frame.setContentPane(getJLabel());
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			if (filename == null) frame.setTitle(width + "-by-" + height);
			else frame.setTitle(filename);
			frame.setResizable(true);
			frame.pack();
			frame.setVisible(true);
		}
		frame.repaint();
	}

	private void validateRowIndex(int row) {
		if (row < 0 || row >= height())
			throw new IllegalArgumentException("Row index must be between 0 and " + (height-1) + ": " + row);
	}

	private void validateColumnIndex(int column) {
		if (column < 0 || column >= width())
			throw new IllegalArgumentException("Column index must be between 0 and " + (width-1) + ": " + column);
	}

	public Color get(int col, int row) {
		validateColumnIndex(col);
		validateRowIndex(row);
		int rgb = getRGB(col, row);
		return new Color(rgb);
	}

	public int getRGB(int col, int row) {
		validateRowIndex(row);
		validateColumnIndex(col);
		if (isOriginUpperLeft) return image.getRGB(col, row);
		else return image.getRGB(col, height - row - 1);
	}

	public void set(int col, int row, Color color) {
		validateColumnIndex(col);
		validateRowIndex(row);
		if (color == null) throw new IllegalArgumentException("color argument is null");
		int rgb = color.getRGB();
		setRGB(col, row, rgb);
	}

	public void setRGB(int col, int row, int rgb) {
		validateRowIndex(row);
		validateColumnIndex(col);
		if (isOriginUpperLeft) image.setRGB(col, row, rgb);
		else image.setRGB(col, height - row - 1, rgb);
	}

	public boolean equals(Object other) {
		if (other == this) return true;
		if (other == null) return false;
		if (other.getClass() != this.getClass()) return false;
		Picture that = (Picture) other;
		if (this.width() != that.width()) return false;
		if (this.height() != that.height()) return false;
		for (int col = 0; col < width(); col++)
			for (int row = 0; row < height(); row++)
				if (this.getRGB(col, row) != that.getRGB(col, row)) return false;
		return true;
 	}

 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(width +"-by-" + height + " picture (RGB values given in hex)\n");
 		for (int row = 0; row < height; row++) {
 			for (int col = 0; col < width; col++) {
 				int rgb = 0;
 				if (isOriginUpperLeft) rgb = image.getRGB(col, row);
 				else rgb = image.getRGB(col, height - row - 1);
 				sb.append(String.format("#%06X ", rgb & 0xFFFFFF));
 			}
 			sb.append("\n");
 		}
 		return sb.toString().trim();
 	}

 	public void save(String filename) {
 		if (filename == null) throw new IllegalArgumentException("argument to save is null");
 		save( new File(filename));
 	}

 	public void save(File file) {
 		if (file == null) throw new IllegalArgumentException("argument to save() is null");
 		filename = file.getName();
 		if (frame != null) frame.setTitle(filename);
 		String suffix = filename.substring(filename.lastIndexOf('.') + 1); //returns filename i.e returns string after last occurence of dot
 		if ("jpg".equalsIgnoreCase(suffix) || "png".equalsIgnoreCase(suffix)) { //ignore case for file extensions
 			try {
 				ImageIO.write(image, suffix, file);
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else {
 			System.out.println("Error: filename must end in .jpg or .png");
 		}
 	}

 	public void actionPerformed(ActionEvent e) {
 		FileDialog chooser = new FileDialog(frame, "Use a .png or .jpg extension", FileDialog.SAVE);
 		chooser.setVisible(true);
 		if (chooser.getFile() != null) {
 			save(chooser.getDirectory() + File.separator + chooser.getFile());
 		}
 	}
	//client for Picture dataType which can display the image
	public static void main(String[] args) {
		Picture picture = new Picture(args[0]);
		System.out.printf("%d-by-%d\n", picture.width(), picture.height());
		picture.show();
	}
}

