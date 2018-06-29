/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seamcarver;

import seamcarver.ui.Picture;

/**
 *
 * @author manju
 */
public class ImageOperations {
    
    public Picture resize(Picture inputImage, int width, int height) {
        if (width >= inputImage.width() || height >= inputImage.height()) {
            throw new IllegalArgumentException("Enter valid width and height");
        }
        int removeColumns = inputImage.width() - width;
        int removeRows = inputImage.height() - height;
        
        SeamCarver sc = new SeamCarver(inputImage);
      
        for (int i = 0; i < removeRows; i++) {
            int[] horizontalSeam = sc.findHorizontalSeam();
            sc.removeHorizontalSeam(horizontalSeam);
        }
        
        for (int i = 0; i < removeColumns; i++) {
            int[] verticalSeam = sc.findVerticalSeam();
            sc.removeVerticalSeam(verticalSeam);
        }
        Picture outputImage = sc.picture();
        return outputImage;
    }
}
