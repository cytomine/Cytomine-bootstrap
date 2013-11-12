package be.cytomine.image

import be.cytomine.processing.image.filters.Auto_Threshold
import com.vividsolutions.jts.geom.Coordinate
import ij.ImagePlus
import ij.gui.PolygonRoi
import ij.gui.Roi
import ij.gui.Wand
import ij.process.ImageConverter
import ij.process.ImageProcessor

import java.awt.*
import java.awt.image.BufferedImage

/**
 *  TODOSTEVBEN:: doc + clean
 */
class ImageProcessingService {

    private static final int BLACK = 0
    private static final int WHITE = 255

    static transactional = false

    public def isInROI(ImagePlus ip, x, y) {
        return (x >= 0 && x < ip.getWidth() && y >= 0 && y < ip.getHeight())
    }

    public BufferedImage applyMaskToAlpha(BufferedImage image, BufferedImage mask) {
        //TODO:: document this method
        int width = image.getWidth()
        int height = image.getHeight()
        int[] imagePixels = image.getRGB(0, 0, width, height, null, 0, width)
        int[] maskPixels = mask.getRGB(0, 0, width, height, null, 0, width)
        int black_rgb = Color.BLACK.getRGB()
        for (int i = 0; i < imagePixels.length; i++)
        {
            int color = imagePixels[i] & 0x00FFFFFF; // mask away any alpha present
            int alphaValue = (maskPixels[i] == black_rgb) ? 0x00 : 0xFF
            int maskColor = alphaValue << 24 // shift value into alpha bits
            imagePixels[i] = color | maskColor
        }
        BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        combined.setRGB(0, 0, width, height, imagePixels, 0, width)
        return combined
    }
}
