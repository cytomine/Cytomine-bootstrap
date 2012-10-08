package be.cytomine.processing.image.filters;

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 1/06/11
 * Time: 15:07
 */

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.RankFilters;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;


/**
 Implementation of a local thresholding (adaptive threshold),  as described
 at the HIPR page (http://www.dai.ed.ac.uk/HIPR2/adpthrsh.htm).

 The statistics included here are the mean, median, min and max vakues of the local neighborhood.
 Works with stacks.

 080420	Included the constant C
 Updated some bugs in the definition of the constant field values.

 Gary.
 031113
 */

public class DynamicThreshold implements PlugInFilter {
    ImagePlus imp;
    boolean gui = false;
    // public static final int 	DESPECKLE 	6
    public static final int 	MAX = 2;
    public static final int 	MEAN = 0;
    public static final int 	MEDIAN 	= 4;
    public static final int 	MIN 	= 1;

    boolean mean=false, median=false, min=false, max=false, meanMaxMin=true, canceled = false;
    double radius=3;
    int c=7;

    public int setup(String arg, ImagePlus imp) {
        if (IJ.versionLessThan("1.40"))
            return DONE;
        this.imp = imp;
        return DOES_8G+NO_UNDO;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setC(int c) {
        this.c = c;
    }

    public void setMean(boolean mean) {
        this.mean = mean;
    }

    public void setMedian(boolean median) {
        this.median = median;
    }

    public void setMin(boolean min) {
        this.min = min;
    }

    public void setMax(boolean max) {
        this.max = max;
    }

    public void setMeanMaxMin(boolean meanMaxMin) {
        this.meanMaxMin = meanMaxMin;
    }

    private ImagePlus imMean;
    private ImagePlus imMedian;
    private ImagePlus imMin;
    private ImagePlus imMax;
    private ImagePlus imMeanMaxMin;

    public ImagePlus getImMean() {
        return imMean;
    }

    public ImagePlus getImMedian() {
        return imMedian;
    }

    public ImagePlus getImMin() {
        return imMin;
    }

    public ImagePlus getImMax() {
        return imMax;
    }

    public ImagePlus getImMeanMaxMin() {
        return imMeanMaxMin;
    }

    public void run(ImageProcessor ip) {
        ImageStack stack = imp.getStack();
        int w = stack.getWidth();
        int h = stack.getHeight();
        int nSlices = imp.getStackSize();

        if (gui) {getDetails(); if (canceled) return;}

        int mSize= (int)radius;
        radius = radius/2;

        ImageStack imsMean = null;
        ImageStack imsMedian = null;
        ImageStack imsMax = null;
        ImageStack imsMin = null;
        ImageStack imsMeanMaxMin = null;

        ByteProcessor bpSlice;
        ByteProcessor bpTemp = new ByteProcessor(w,h);
        RankFilters rf = new RankFilters();

        for (int i = 0; i < nSlices; i++) {
            if (gui) IJ.showStatus("a: "+i+"/"+nSlices);
            imp.setSlice(i+1);
            bpSlice = (ByteProcessor)stack.getProcessor(i+1);
            byte[] sliceArray = (byte[])bpSlice.getPixelsCopy();

            if (mean){
                imsMean = new ImageStack(w,h);
                bpTemp.setPixels(bpSlice.getPixelsCopy());
                rf.rank(bpTemp,radius, MEAN);
                bpTemp=getNewProcessor(bpTemp,bpSlice,c);
                imsMean.addSlice("Test",bpTemp);
                imMean = new ImagePlus("",imsMean);
                imMean.setStack(null,imsMean);
            }
            if(median){
                imsMedian = new ImageStack(w,h);
                bpTemp.setPixels(bpSlice.getPixelsCopy());
                rf.rank(bpTemp,radius, MEDIAN);
                bpTemp=getNewProcessor(bpTemp,bpSlice,c);
                imsMedian.addSlice("Test",bpTemp);
                imMedian = new ImagePlus("",imsMedian);
                imMedian.setStack(null,imsMedian);
            }
            if(min){
                imsMin = new ImageStack(w,h);
                bpTemp.setPixels(bpSlice.getPixelsCopy());
                rf.rank(bpTemp,radius, MIN);
                bpTemp=getNewProcessor(bpTemp,bpSlice,c);
                imsMin.addSlice("Test",bpTemp);
                imMin = new ImagePlus("",imsMin);
                imMin.setStack(null,imsMin);
            }
            if(max){
                imsMax = new ImageStack(w,h);
                bpTemp.setPixels(bpSlice.getPixelsCopy());
                rf.rank(bpTemp,radius, MAX);
                bpTemp=getNewProcessor(bpTemp,bpSlice,c);
                imsMax.addSlice("Test",bpTemp);
                imMax = new ImagePlus("",imsMax);
                imMax.setStack(null,imsMax);
            }
            if(meanMaxMin){
                imsMeanMaxMin = new ImageStack(w,h);
                bpTemp.setPixels(bpSlice.getPixelsCopy());
                bpTemp=dFilter(bpTemp,mSize);
                bpTemp=getNewProcessor(bpTemp,bpSlice,c);
                imsMeanMaxMin.addSlice("Test",bpTemp);
                imMeanMaxMin = new ImagePlus("",imsMeanMaxMin);
                imMeanMaxMin.setStack(null,imsMeanMaxMin);
            }

        }

        if (!gui) return;
        if (mean) createImagePlus(imsMean, "Mean images");
        if (median) createImagePlus(imsMedian, "Median images");
        if (max) createImagePlus(imsMax, "Maximum images");
        if (min) createImagePlus(imsMin, "Minimum images");
        if (meanMaxMin) createImagePlus(imsMeanMaxMin, "MaxMin images");

    }



    void getDetails() {
        GenericDialog gd = new GenericDialog("Dynamic thresholding...");
        gd.addNumericField("Mask size: ", 3, 0);
        gd.addCheckbox("Display mean image", mean);
        gd.addCheckbox("Display median image", median);
        gd.addCheckbox("Display max image", max);
        gd.addCheckbox("Display min image ", min);
        gd.addCheckbox("Display (max+min)/2 image ", meanMaxMin);
        gd.addNumericField("Constant C: ", 7, 0);

        gd.showDialog();
        if (gd.wasCanceled()) {
            canceled = true;
            return;
        }
        radius= (int)gd.getNextNumber();
        mean = gd.getNextBoolean();
        median = gd.getNextBoolean();
        max = gd.getNextBoolean();
        min= gd.getNextBoolean();
        meanMaxMin= gd.getNextBoolean();
        c= (int)gd.getNextNumber();

    }

    ByteProcessor dFilter(ByteProcessor bpTmp, int r){
        int ww = bpTmp.getWidth();
        int hh = bpTmp.getHeight();
        byte[] tmpArray = (byte[])bpTmp.getPixelsCopy();
        int[] fArray=new int[r*r];
        int r2=(int)(r/2);
        int index=0;

        for (int y=0;y<hh;y++){
            for (int x=0;x<ww;x++){
                int i=0;
                for (int yi=0;yi<r;yi++){
                    for (int xi=0;xi<r;xi++){
                        // = xi+yi*r;
                        fArray[i]=bpTmp.getPixel(x-r2+xi,y-r2+yi);
                        i++;
                    }
                }
                tmpArray[index]=(byte)getMeanMaxMin(fArray);
                index++;
            }
        }
        ByteProcessor tmp = new ByteProcessor(ww,hh, tmpArray, null);
        return tmp;
    }

    int getMeanMaxMin(int[] a){
        int max=-999999;
        int min=999999;

        for (int i=0;i<a.length;i++){
            if (a[i]<min) min=a[i];
            if (a[i]>max) max=a[i];
        }
        return (max+min)/2;
    }



    ByteProcessor getNewProcessor(ByteProcessor bpTmp, ByteProcessor bpSlc, int constant){
        int ww = bpSlc.getWidth();
        int hh = bpSlc.getHeight();
        byte[] tmpArray = (byte[])bpTmp.getPixelsCopy();
        byte[] slcArray = (byte[])bpSlc.getPixelsCopy();
        for (int i=0; i<tmpArray.length;i++){
            tmpArray[i]=(byte)(tmpArray[i]-slcArray[i]);//-constant);
            if (constant >0) {
                if (tmpArray[i]<constant) tmpArray[i]=(byte)255;
                else tmpArray[i]=(byte)0;
            }
        }
        ByteProcessor tmp = new ByteProcessor(ww,hh, tmpArray, null);
        return tmp;
    }


    void createImagePlus(ImageStack imsTemp, String txt){
        ImagePlus impTemp = new ImagePlus(txt,imsTemp);
        impTemp.setStack(null,imsTemp);
        impTemp.show();
    }
}