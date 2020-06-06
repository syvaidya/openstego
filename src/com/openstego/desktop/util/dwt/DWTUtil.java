/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */

package com.openstego.desktop.util.dwt;

import com.openstego.desktop.util.CommonUtil;

/**
 * Class to handle Wavelet filters and other DWT utilities.
 * <p>
 * This class is conversion of C to Java for the file "wavelet.c" file provided by Peter Meerwald at:<a
 * href="http://www.cosy.sbg.ac.at/~pmeerw/Watermarking/">http://www.cosy.sbg.ac.at/~pmeerw/Watermarking/</a>
 * <p>
 * Refer to his thesis on watermarking: Peter Meerwald, Digital Image Watermarking in the Wavelet Transfer Domain,
 * Master's Thesis, Department of Scientific Computing, University of Salzburg, Austria, January 2001.
 */
public class DWTUtil {
    /**
     * Constructor is private so that this class is not instantiated
     */
    private DWTUtil() {
    }

    /**
     * Method to perform the wavelet transform
     *
     * @param origImg Original image
     * @param level Decomposition level
     * @param filterGHList List of filters
     * @param method Wavelet filtering method
     * @return Data after performing wavelet transform
     */
    public static ImageTree waveletTransform(Image origImg, int level, FilterGH[] filterGHList, int method) {
        int width = 0;
        int height = 0;
        int min = 0;
        int maxLevel = 0;
        Image coarseImg = null;
        Image horizontalImg = null;
        Image verticalImg = null;
        Image diagonalImg = null;
        Image tempImg = null;
        ImageTree returnTree = null;
        ImageTree tempTree = null;

        width = origImg.getWidth();
        height = origImg.getHeight();

        tempImg = new Image(width, height);
        copyIntoImage(tempImg, origImg, 0, 0);

        returnTree = new ImageTree();
        tempTree = returnTree;
        returnTree.setLevel(0);

        min = origImg.getWidth();
        if (origImg.getHeight() < min) {
            min = origImg.getHeight();
        }

        maxLevel = ((int) (Math.log(min) / Math.log(2))) - 2;
        if (maxLevel < level) {
            level = maxLevel;
        }

        if (level < 1) /* do not transform */
        {
            returnTree.setImage(tempImg);
            return returnTree;
        }

        // Decomposition
        for (int i = 0; i < level; i++) {
            width = (width + 1) / 2;
            height = (height + 1) / 2;

            coarseImg = new Image(width, height);
            horizontalImg = new Image(width, height);
            verticalImg = new Image(width, height);
            diagonalImg = new Image(width, height);

            decomposition(tempImg, coarseImg, horizontalImg, verticalImg, diagonalImg, filterGHList[i].getG(), filterGHList[i].getH(), method);

            tempTree.setCoarse(new ImageTree());
            tempTree.setHorizontal(new ImageTree());
            tempTree.setVertical(new ImageTree());
            tempTree.setDiagonal(new ImageTree());

            tempTree.getCoarse().setLevel(i + 1);
            tempTree.getHorizontal().setLevel(i + 1);
            tempTree.getVertical().setLevel(i + 1);
            tempTree.getDiagonal().setLevel(i + 1);

            tempTree.getHorizontal().setImage(horizontalImg);
            tempTree.getVertical().setImage(verticalImg);
            tempTree.getDiagonal().setImage(diagonalImg);
            tempImg = null;

            if (i != (level - 1)) {
                tempImg = new Image(width, height);
                copyIntoImage(tempImg, coarseImg, 0, 0);
                coarseImg = null;
            }

            tempTree = tempTree.getCoarse();
        }

        tempTree.setImage(coarseImg);
        return returnTree;
    }

    /**
     * Method to perform the wavelet transform (WP)
     *
     * @param origImg Original image
     * @param currLevel Current decomposition level
     * @param level Decomposition level
     * @param filterGHList List of filters
     * @param method Wavelet filtering method
     * @return Data after performing wavelet transform
     */
    public static ImageTree waveletTransformWp(Image origImg, int currLevel, int level, FilterGH[] filterGHList, int method) {
        int width = 0;
        int height = 0;
        int min = 0;
        int maxLevel = 0;
        Image coarseImg = null;
        Image horizontalImg = null;
        Image verticalImg = null;
        Image diagonalImg = null;
        Image tempImg = null;
        ImageTree returnTree = null;
        ImageTree tempTree = null;

        width = origImg.getWidth();
        height = origImg.getHeight();

        tempImg = new Image(width, height);
        copyIntoImage(tempImg, origImg, 0, 0);

        returnTree = new ImageTree();
        tempTree = returnTree;
        tempTree.setLevel(currLevel);

        min = origImg.getWidth();
        if (origImg.getHeight() < min) {
            min = origImg.getHeight();
        }

        maxLevel = (int) (Math.log(min) / Math.log(2)) - 2;
        if (maxLevel < level) {
            level = maxLevel;
        }

        if (currLevel >= level) {
            returnTree.setImage(tempImg);
            return returnTree;
        }

        for (int i = currLevel; i < level; i++) {
            width = (width + 1) / 2;
            height = (height + 1) / 2;

            coarseImg = new Image(width, height);
            horizontalImg = new Image(width, height);
            verticalImg = new Image(width, height);
            diagonalImg = new Image(width, height);

            decomposition(tempImg, coarseImg, horizontalImg, verticalImg, diagonalImg, filterGHList[i].getG(), filterGHList[i].getH(), method);

            tempTree.setCoarse(new ImageTree());
            tempTree.getCoarse().setLevel(i + 1);
            tempTree.setHorizontal(waveletTransformWp(horizontalImg, i + 1, level, filterGHList, method));
            tempTree.setVertical(waveletTransformWp(verticalImg, i + 1, level, filterGHList, method));
            tempTree.setDiagonal(waveletTransformWp(diagonalImg, i + 1, level, filterGHList, method));

            horizontalImg = null;
            verticalImg = null;
            diagonalImg = null;
            tempImg = null;

            if (i != (level - 1)) {
                tempImg = new Image(width, height);
                copyIntoImage(tempImg, coarseImg, 0, 0);
                coarseImg = null;
            }

            tempTree = tempTree.getCoarse();
        }

        tempTree.setImage(coarseImg);
        return returnTree;
    }

    /**
     * Method to decompose the image
     *
     * @param inputImg Input image
     * @param coarseImg Coarse image
     * @param horizontalImg Horizontal image
     * @param verticalImg Vertical image
     * @param diagonalImg Diagonal image
     * @param filterG G filter
     * @param filterH H filter
     * @param method Wavelet filtering method
     */
    public static void decomposition(Image inputImg, Image coarseImg, Image horizontalImg, Image verticalImg, Image diagonalImg, Filter filterG,
            Filter filterH, int method) {
        Image tempImg = null;

        // Coarse
        tempImg = new Image(coarseImg.getWidth(), inputImg.getHeight());
        convoluteLines(tempImg, inputImg, filterH, method);
        convoluteRows(coarseImg, tempImg, filterH, method);

        // Horizontal
        convoluteRows(horizontalImg, tempImg, filterG, method);

        // Vertical
        tempImg = new Image(verticalImg.getWidth(), inputImg.getHeight());
        convoluteLines(tempImg, inputImg, filterG, method);
        convoluteRows(verticalImg, tempImg, filterH, method);

        // Diagonal
        convoluteRows(diagonalImg, tempImg, filterG, method);
    }

    /**
     * Method to convolute lines
     *
     * @param outputImg Output image
     * @param inputImg Input image
     * @param filter Filter to use
     * @param method Wavelet filtering method
     */
    public static void convoluteLines(Image outputImg, Image inputImg, Filter filter, int method) {
        for (int i = 0; i < inputImg.getHeight(); i++) {
            switch (method) {
                case Filter.METHOD_CUTOFF:
                    filterCutOff(inputImg, inputImg.getWidth() * i, inputImg.getWidth(), 1, outputImg, outputImg.getWidth() * i, outputImg.getWidth(),
                        1, filter);
                    break;

                case Filter.METHOD_INVCUTOFF:
                    filterInvCutOff(inputImg, inputImg.getWidth() * i, inputImg.getWidth(), 1, outputImg, outputImg.getWidth() * i,
                        outputImg.getWidth(), 1, filter);
                    break;

                case Filter.METHOD_PERIODICAL:
                    filterPeriodical(inputImg, inputImg.getWidth() * i, inputImg.getWidth(), 1, outputImg, outputImg.getWidth() * i,
                        outputImg.getWidth(), 1, filter);
                    break;

                case Filter.METHOD_INVPERIODICAL:
                    filterInvPeriodical(inputImg, inputImg.getWidth() * i, inputImg.getWidth(), 1, outputImg, outputImg.getWidth() * i,
                        outputImg.getWidth(), 1, filter);
                    break;

                case Filter.METHOD_MIRROR:
                    filterMirror(inputImg, inputImg.getWidth() * i, inputImg.getWidth(), 1, outputImg, outputImg.getWidth() * i, outputImg.getWidth(),
                        1, filter);
                    break;

                case Filter.METHOD_INVMIRROR:
                    filterInvMirror(inputImg, inputImg.getWidth() * i, inputImg.getWidth(), 1, outputImg, outputImg.getWidth() * i,
                        outputImg.getWidth(), 1, filter);
                    break;
            }
        }
    }

    /**
     * Method to convolute rows
     *
     * @param outputImg Output image
     * @param inputImg Input image
     * @param filter Filter to use
     * @param method Wavelet filtering method
     */
    public static void convoluteRows(Image outputImg, Image inputImg, Filter filter, int method) {
        for (int i = 0; i < inputImg.getWidth(); i++) {
            switch (method) {
                case Filter.METHOD_CUTOFF:
                    filterCutOff(inputImg, i, inputImg.getHeight(), inputImg.getWidth(), outputImg, i, outputImg.getHeight(), outputImg.getWidth(),
                        filter);
                    break;

                case Filter.METHOD_INVCUTOFF:
                    filterInvCutOff(inputImg, i, inputImg.getHeight(), inputImg.getWidth(), outputImg, i, outputImg.getHeight(), outputImg.getWidth(),
                        filter);
                    break;

                case Filter.METHOD_PERIODICAL:
                    filterPeriodical(inputImg, i, inputImg.getHeight(), inputImg.getWidth(), outputImg, i, outputImg.getHeight(),
                        outputImg.getWidth(), filter);
                    break;

                case Filter.METHOD_INVPERIODICAL:
                    filterInvPeriodical(inputImg, i, inputImg.getHeight(), inputImg.getWidth(), outputImg, i, outputImg.getHeight(),
                        outputImg.getWidth(), filter);
                    break;

                case Filter.METHOD_MIRROR:
                    filterMirror(inputImg, i, inputImg.getHeight(), inputImg.getWidth(), outputImg, i, outputImg.getHeight(), outputImg.getWidth(),
                        filter);
                    break;

                case Filter.METHOD_INVMIRROR:
                    filterInvMirror(inputImg, i, inputImg.getHeight(), inputImg.getWidth(), outputImg, i, outputImg.getHeight(), outputImg.getWidth(),
                        filter);
                    break;
            }
        }
    }

    /**
     * Method to apply cut-off filter
     *
     * @param inputImg Input image
     * @param inStart Start point for input image
     * @param inLen Length of data for input image
     * @param inStep Step for loop for input image
     * @param outputImg Output image
     * @param outStart Start point for output image
     * @param outLen Length of data for output image
     * @param outStep Step for loop for output image
     * @param filter Filter
     */
    public static void filterCutOff(Image inputImg, int inStart, int inLen, int inStep, Image outputImg, int outStart, int outLen, int outStep,
            Filter filter) {
        int fStart = 0;
        int fEnd = 0;

        for (int i = 0; i < outLen; i++) {
            fStart = CommonUtil.max((2 * i) - (inLen - 1), filter.getStart());
            fEnd = CommonUtil.min((2 * i), filter.getEnd());

            for (int j = fStart; j <= fEnd; j++) {
                outputImg.getData()[outStart + i * outStep] += filter.getData()[j - filter.getStart()]
                        * inputImg.getData()[inStart + ((2 * i) - j) * inStep];
            }
        }
    }

    /**
     * Method to apply inverse cut-off filter
     *
     * @param inputImg Input image
     * @param inStart Start point for input image
     * @param inLen Length of data for input image
     * @param inStep Step for loop for input image
     * @param outputImg Output image
     * @param outStart Start point for output image
     * @param outLen Length of data for output image
     * @param outStep Step for loop for output image
     * @param filter Filter
     */
    public static void filterInvCutOff(Image inputImg, int inStart, int inLen, int inStep, Image outputImg, int outStart, int outLen, int outStep,
            Filter filter) {
        int fStart = 0;
        int fEnd = 0;

        for (int i = 0; i < outLen; i++) {
            fStart = CommonUtil.max(CommonUtil.ceilingHalf(filter.getStart() + i), 0);
            fEnd = CommonUtil.min(CommonUtil.floorHalf(filter.getEnd() + i), inLen - 1);

            for (int j = fStart; j <= fEnd; j++) {
                outputImg.getData()[outStart + i * outStep] += filter.getData()[(2 * j) - i - filter.getStart()]
                        * inputImg.getData()[inStart + j * inStep];
            }
        }
    }

    /**
     * Method to apply periodical filter
     *
     * @param inputImg Input image
     * @param inStart Start point for input image
     * @param inLen Length of data for input image
     * @param inStep Step for loop for input image
     * @param outputImg Output image
     * @param outStart Start point for output image
     * @param outLen Length of data for output image
     * @param outStep Step for loop for output image
     * @param filter Filter
     */
    public static void filterPeriodical(Image inputImg, int inStart, int inLen, int inStep, Image outputImg, int outStart, int outLen, int outStep,
            Filter filter) {
        int fStart = 0;
        int fEnd = 0;
        int iStart = 0;

        for (int i = 0; i < outLen; i++) {
            fStart = filter.getStart();
            fEnd = filter.getEnd();
            iStart = CommonUtil.mod(((2 * i) - fStart), inLen);

            for (int j = fStart; j <= fEnd; j++) {
                outputImg.getData()[outStart + i * outStep] += filter.getData()[j - fStart] * inputImg.getData()[inStart + iStart * inStep];
                iStart--;
                if (iStart < 0) {
                    iStart += inLen;
                }
            }
        }
    }

    /**
     * Method to apply inverse periodical filter
     *
     * @param inputImg Input image
     * @param inStart Start point for input image
     * @param inLen Length of data for input image
     * @param inStep Step for loop for input image
     * @param outputImg Output image
     * @param outStart Start point for output image
     * @param outLen Length of data for output image
     * @param outStep Step for loop for output image
     * @param filter Filter
     */
    public static void filterInvPeriodical(Image inputImg, int inStart, int inLen, int inStep, Image outputImg, int outStart, int outLen, int outStep,
            Filter filter) {
        int fStart = 0;
        int fEnd = 0;
        int iStart = 0;

        for (int i = 0; i < outLen; i++) {
            fStart = CommonUtil.ceilingHalf(filter.getStart() + i);
            fEnd = CommonUtil.floorHalf(filter.getEnd() + i);
            iStart = CommonUtil.mod(fStart, inLen);

            for (int j = fStart; j <= fEnd; j++) {
                outputImg.getData()[outStart + i * outStep] += filter.getData()[(2 * j) - i - filter.getStart()]
                        * inputImg.getData()[inStart + iStart * inStep];
                iStart++;
                if (iStart >= inLen) {
                    iStart -= inLen;
                }
            }
        }
    }

    /**
     * Method to apply mirror filter
     *
     * @param inputImg Input image
     * @param inStart Start point for input image
     * @param inLen Length of data for input image
     * @param inStep Step for loop for input image
     * @param outputImg Output image
     * @param outStart Start point for output image
     * @param outLen Length of data for output image
     * @param outStep Step for loop for output image
     * @param filter Filter
     */
    public static void filterMirror(Image inputImg, int inStart, int inLen, int inStep, Image outputImg, int outStart, int outLen, int outStep,
            Filter filter) {
        int fStart = 0;
        int fEnd = 0;
        int inPos = 0;

        for (int i = 0; i < outLen; i++) {
            fStart = filter.getStart();
            fEnd = filter.getEnd();

            for (int j = fStart; j <= fEnd; j++) {
                inPos = ((2 * i) - j);
                if (inPos < 0) {
                    inPos = -inPos;
                    if (inPos >= inLen) {
                        continue;
                    }
                }
                if (inPos >= inLen) {
                    inPos = 2 * inLen - 2 - inPos;
                    if (inPos < 0) {
                        continue;
                    }
                }
                outputImg.getData()[outStart + i * outStep] += filter.getData()[j - fStart] * inputImg.getData()[inStart + inPos * inStep];
            }
        }
    }

    /**
     * Method to apply inverse mirror filter
     *
     * @param inputImg Input image
     * @param inStart Start point for input image
     * @param inLen Length of data for input image
     * @param inStep Step for loop for input image
     * @param outputImg Output image
     * @param outStart Start point for output image
     * @param outLen Length of data for output image
     * @param outStep Step for loop for output image
     * @param filter Filter
     */
    public static void filterInvMirror(Image inputImg, int inStart, int inLen, int inStep, Image outputImg, int outStart, int outLen, int outStep,
            Filter filter) {
        int fStart = 0;
        int fEnd = 0;
        int inPos = 0;

        for (int i = 0; i < outLen; i++) {
            fStart = CommonUtil.ceilingHalf(filter.getStart() + i);
            fEnd = CommonUtil.floorHalf(filter.getEnd() + i);

            for (int j = fStart; j <= fEnd; j++) {
                inPos = j;
                if (inPos < 0) {
                    if (filter.isHiPass()) {
                        inPos = -inPos - 1;
                    } else {
                        inPos = -inPos;
                    }
                    if (inPos >= inLen) {
                        continue;
                    }
                }
                if (inPos >= inLen) {
                    if (filter.isHiPass()) {
                        inPos = 2 * inLen - 2 - inPos;
                    } else {
                        inPos = 2 * inLen - 1 - inPos;
                    }
                    if (inPos < 0) {
                        continue;
                    }
                }
                outputImg.getData()[outStart + i * outStep] += filter.getData()[2 * j - i - filter.getStart()]
                        * inputImg.getData()[inStart + inPos * inStep];
            }
        }
    }

    /**
     * Method to perform inverse wavelet transform
     *
     * @param tree Forward transformed DWT data
     * @param filterGHList List of filters
     * @param method Wavelet filter method
     * @return Inverse transformed image data
     */
    public static Image inverseTransform(ImageTree tree, FilterGH[] filterGHList, int method) {
        int width = 0;
        int height = 0;
        Image retImg = null;
        Image coarseImg = null;
        Image verticalImg = null;
        Image horizontalImg = null;
        Image diagonalImg = null;

        if (tree.getImage() == null) {
            coarseImg = inverseTransform(tree.getCoarse(), filterGHList, method);
            horizontalImg = inverseTransform(tree.getHorizontal(), filterGHList, method);
            verticalImg = inverseTransform(tree.getVertical(), filterGHList, method);
            diagonalImg = inverseTransform(tree.getDiagonal(), filterGHList, method);

            width = coarseImg.getWidth() + horizontalImg.getWidth();
            height = coarseImg.getHeight() + verticalImg.getHeight();

            retImg = new Image(width, height);

            if (tree.getFlag() == 0) // If flag is set it is a doubletree tiling
            {
                invDecomposition(retImg, coarseImg, horizontalImg, verticalImg, diagonalImg, filterGHList[tree.getLevel()], method);
            } else {
                copyIntoImage(retImg, coarseImg, 0, 0);
                copyIntoImage(retImg, horizontalImg, coarseImg.getWidth(), 0);
                copyIntoImage(retImg, verticalImg, 0, coarseImg.getHeight());
                copyIntoImage(retImg, diagonalImg, coarseImg.getWidth(), coarseImg.getHeight());
            }

            return retImg;
        }
        return tree.getImage();
    }

    /**
     * Method to perform inverse decomposition
     *
     * @param sumImg Sum image
     * @param coarseImg Coarse image
     * @param horizontalImg Horizontal image
     * @param verticalImg Vertical image
     * @param diagonalImg Diagonal image
     * @param filterGH Filter
     * @param method Wavelet filter method
     */
    public static void invDecomposition(Image sumImg, Image coarseImg, Image horizontalImg, Image verticalImg, Image diagonalImg, FilterGH filterGH,
            int method) {
        Image tempImg = null;
        Filter filterG = null;
        Filter filterH = null;

        if (filterGH.getType() == FilterGH.TYPE_ORTHOGONAL) {
            filterG = filterGH.getG();
            filterH = filterGH.getH();
        } else {
            filterG = filterGH.getGi();
            filterH = filterGH.getHi();
        }

        // Coarse
        tempImg = new Image(coarseImg.getWidth(), sumImg.getHeight());
        convoluteRows(tempImg, coarseImg, filterH, method);

        // Horizontal
        convoluteRows(tempImg, horizontalImg, filterG, method);
        convoluteLines(sumImg, tempImg, filterH, method);

        // Vertical
        tempImg = new Image(verticalImg.getWidth(), sumImg.getHeight());
        convoluteRows(tempImg, verticalImg, filterH, method);

        // Diagonal
        convoluteRows(tempImg, diagonalImg, filterG, method);
        convoluteLines(sumImg, tempImg, filterG, method);
    }

    /**
     * Method to get the deepest level possible for given image width and height
     *
     * @param width Image width
     * @param height Image height
     * @return Deepest possible level
     */
    public static int findDeepestLevel(int width, int height) {
        int level = 0;
        int w = width;
        int h = height;

        while ((w % 2 == 0) && (h % 2 == 0)) {
            w = w / 2;
            h = h / 2;
            level++;
        }

        return level - 1;
    }

    /**
     * Set pixel value in the image
     *
     * @param image Image
     * @param x X position of pixel
     * @param y Y position of pixel
     * @param val Pixel value
     */
    public static void setPixel(Image image, int x, int y, double val) {
        if (!(image == null || x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight())) {
            image.getData()[x + (y * image.getWidth())] = val;
        }
    }

    /**
     * Get pixel value from the image
     *
     * @param image Image
     * @param x X position of pixel
     * @param y Y position of pixel
     * @return Pixel value
     */
    public static double getPixel(Image image, int x, int y) {
        if (image == null || x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight()) {
            return 0.0;
        } else {
            return image.getData()[x + y * image.getWidth()];
        }
    }

    /**
     * Utility method to copy image data to another image portion
     *
     * @param img1 Output image
     * @param img2 Input image
     * @param x X position in output image
     * @param y Y position in output image
     */
    private static void copyIntoImage(Image img1, Image img2, int x, int y) {
        int count = 0;
        int start = 0;
        int aim = 0;
        double[] temp = null;

        temp = img2.getData();
        start = img1.getWidth() * y + x;

        for (int i = 0; i < img2.getHeight(); i++) {
            for (int j = 0; j < img2.getWidth(); j++) {
                aim = start + j + img1.getWidth() * i;
                img1.getData()[aim] = temp[count];
                count++;
            }
        }
    }
}