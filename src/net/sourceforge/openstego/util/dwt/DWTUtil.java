/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.util.dwt;

/**
 * Class to handle Wavelet filters and other DWT utilities.
 * 
 * This class is conversion of C to Java for the file "wavelet.c" file provided by Peter Meerwald at:
 * http://www.cosy.sbg.ac.at/~pmeerw/Watermarking/
 * 
 * Refer to his thesis on watermarking: Peter Meerwald, Digital Image Watermarking in the Wavelet Transfer Domain,
 * Master's Thesis, Department of Scientific Computing, University of Salzburg, Austria, January 2001.
 */
public class DWTUtil
{
    public static ImageTree waveletTransform(Image origImg, int level, FilterGH[] filterGHList, int method)
    {
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
        if(origImg.getHeight() < min)
        {
            min = origImg.getHeight();
        }

        maxLevel = ((int) (Math.log(min) / Math.log(2))) - 2;
        if(maxLevel < level)
        {
            level = maxLevel;
        }

        if(level < 1) /* do not transform */
        {
            returnTree.setImage(tempImg);
            return returnTree;
        }

        /* decomposition */
        for(int i = 0; i < level; i++)
        {
            width = (width + 1) / 2;
            height = (height + 1) / 2;

            coarseImg = new Image(width, height);
            horizontalImg = new Image(width, height);
            verticalImg = new Image(width, height);
            diagonalImg = new Image(width, height);

            decomposition(tempImg, coarseImg, horizontalImg, verticalImg, diagonalImg, filterGHList[i].getG(),
                filterGHList[i].getH(), method);

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

            if(i != (level - 1))
            {
                tempImg = new Image(width, height);
                copyIntoImage(tempImg, coarseImg, 0, 0);
                coarseImg = null;
            }

            tempTree = tempTree.getCoarse();
        }

        tempTree.setImage(coarseImg);
        return returnTree;
    }

    public static ImageTree waveletTransformWp(Image origImg, int currLevel, int level, FilterGH[] filterGHList,
            int method)
    {
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
        if(origImg.getHeight() < min)
        {
            min = origImg.getHeight();
        }

        maxLevel = (int) (Math.log(min) / Math.log(2)) - 2;
        if(maxLevel < level)
        {
            level = maxLevel;
        }

        if(currLevel >= level)
        {
            returnTree.setImage(tempImg);
            return returnTree;
        }

        for(int i = currLevel; i < level; i++)
        {
            width = (width + 1) / 2;
            height = (height + 1) / 2;

            coarseImg = new Image(width, height);
            horizontalImg = new Image(width, height);
            verticalImg = new Image(width, height);
            diagonalImg = new Image(width, height);

            decomposition(tempImg, coarseImg, horizontalImg, verticalImg, diagonalImg, filterGHList[i].getG(),
                filterGHList[i].getH(), method);

            tempTree.setCoarse(new ImageTree());
            tempTree.getCoarse().setLevel(i + 1);
            tempTree.setHorizontal(waveletTransformWp(horizontalImg, i + 1, level, filterGHList, method));
            tempTree.setVertical(waveletTransformWp(verticalImg, i + 1, level, filterGHList, method));
            tempTree.setDiagonal(waveletTransformWp(diagonalImg, i + 1, level, filterGHList, method));

            horizontalImg = null;
            verticalImg = null;
            diagonalImg = null;
            tempImg = null;

            if(i != (level - 1))
            {
                tempImg = new Image(width, height);
                copyIntoImage(tempImg, coarseImg, 0, 0);
                coarseImg = null;
            }

            tempTree = tempTree.getCoarse();
        }

        tempTree.setImage(coarseImg);
        return returnTree;
    }

    public static void decomposition(Image inputImg, Image coarseImg, Image horizontalImg, Image verticalImg,
            Image diagonalImg, Filter filterG, Filter filterH, int method)
    {
        Image tempImg = null;

        /*coarse*/
        tempImg = new Image(coarseImg.getWidth(), inputImg.getHeight());
        convoluteLines(tempImg, inputImg, filterH, method);
        convoluteRows(coarseImg, tempImg, filterH, method);

        /*horizontal*/
        convoluteRows(horizontalImg, tempImg, filterG, method);

        /*vertical*/
        tempImg = new Image(verticalImg.getWidth(), inputImg.getHeight());
        convoluteLines(tempImg, inputImg, filterG, method);
        convoluteRows(verticalImg, tempImg, filterH, method);

        /*diagonal*/
        convoluteRows(diagonalImg, tempImg, filterG, method);
    }

    public static void convoluteLines(Image outputImg, Image inputImg, Filter filter, int method)
    {
        for(int i = 0; i < inputImg.getHeight(); i++)
        {
            switch(method)
            {
                case Filter.METHOD_CUTOFF:
                    filterCutOff(inputImg, inputImg.getWidth() * i, inputImg.getWidth(), 1, outputImg, outputImg
                            .getWidth()
                            * i, outputImg.getWidth(), 1, filter);
                    break;

                case Filter.METHOD_INVCUTOFF:
                    filterInvCutOff(inputImg, inputImg.getWidth() * i, inputImg.getWidth(), 1, outputImg, outputImg
                            .getWidth()
                            * i, outputImg.getWidth(), 1, filter);
                    break;

                case Filter.METHOD_PERIODICAL:
                    filterPeriodical(inputImg, inputImg.getWidth() * i, inputImg.getWidth(), 1, outputImg, outputImg
                            .getWidth()
                            * i, outputImg.getWidth(), 1, filter);
                    break;

                case Filter.METHOD_INVPERIODICAL:
                    filterInvPeriodical(inputImg, inputImg.getWidth() * i, inputImg.getWidth(), 1, outputImg, outputImg
                            .getWidth()
                            * i, outputImg.getWidth(), 1, filter);
                    break;

                case Filter.METHOD_MIRROR:
                    filterMirror(inputImg, inputImg.getWidth() * i, inputImg.getWidth(), 1, outputImg, outputImg
                            .getWidth()
                            * i, outputImg.getWidth(), 1, filter);
                    break;

                case Filter.METHOD_INVMIRROR:
                    filterInvMirror(inputImg, inputImg.getWidth() * i, inputImg.getWidth(), 1, outputImg, outputImg
                            .getWidth()
                            * i, outputImg.getWidth(), 1, filter);
                    break;
            }
        }
    }

    public static void convoluteRows(Image outputImg, Image inputImg, Filter filter, int method)
    {
        for(int i = 0; i < inputImg.getWidth(); i++)
        {
            switch(method)
            {
                case Filter.METHOD_CUTOFF:
                    filterCutOff(inputImg, i, inputImg.getHeight(), inputImg.getWidth(), outputImg, i, outputImg
                            .getHeight(), outputImg.getWidth(), filter);
                    break;

                case Filter.METHOD_INVCUTOFF:
                    filterInvCutOff(inputImg, i, inputImg.getHeight(), inputImg.getWidth(), outputImg, i, outputImg
                            .getHeight(), outputImg.getWidth(), filter);
                    break;

                case Filter.METHOD_PERIODICAL:
                    filterPeriodical(inputImg, i, inputImg.getHeight(), inputImg.getWidth(), outputImg, i, outputImg
                            .getHeight(), outputImg.getWidth(), filter);
                    break;

                case Filter.METHOD_INVPERIODICAL:
                    filterInvPeriodical(inputImg, i, inputImg.getHeight(), inputImg.getWidth(), outputImg, i, outputImg
                            .getHeight(), outputImg.getWidth(), filter);
                    break;

                case Filter.METHOD_MIRROR:
                    filterMirror(inputImg, i, inputImg.getHeight(), inputImg.getWidth(), outputImg, i, outputImg
                            .getHeight(), outputImg.getWidth(), filter);
                    break;

                case Filter.METHOD_INVMIRROR:
                    filterInvMirror(inputImg, i, inputImg.getHeight(), inputImg.getWidth(), outputImg, i, outputImg
                            .getHeight(), outputImg.getWidth(), filter);
                    break;
            }
        }
    }

    public static void filterCutOff(Image inputImg, int inStart, int inLen, int inStep, Image outputImg, int outStart,
            int outLen, int outStep, Filter f)
    {
        int fStart = 0;
        int fEnd = 0;

        for(int i = 0; i < outLen; i++)
        {
            fStart = max((2 * i) - (inLen - 1), f.getStart());
            fEnd = min((2 * i), f.getEnd());

            for(int j = fStart; j <= fEnd; j++)
            {
                outputImg.getData()[outStart + i * outStep] += f.getData()[j - f.getStart()]
                        * inputImg.getData()[inStart + ((2 * i) - j) * inStep];
            }
        }
    }

    public static void filterInvCutOff(Image inputImg, int inStart, int inLen, int inStep, Image outputImg,
            int outStart, int outLen, int outStep, Filter f)
    {
        int fStart = 0;
        int fEnd = 0;

        for(int i = 0; i < outLen; i++)
        {
            fStart = max(ceilingHalf(f.getStart() + i), 0);
            fEnd = min(floorHalf(f.getEnd() + i), inLen - 1);

            for(int j = fStart; j <= fEnd; j++)
            {
                outputImg.getData()[outStart + i * outStep] += f.getData()[(2 * j) - i - f.getStart()]
                        * inputImg.getData()[inStart + j * inStep];
            }
        }
    }

    public static void filterPeriodical(Image inputImg, int inStart, int inLen, int inStep, Image outputImg,
            int outStart, int outLen, int outStep, Filter f)
    {
        int fStart = 0;
        int fEnd = 0;
        int iStart = 0;

        for(int i = 0; i < outLen; i++)
        {
            fStart = f.getStart();
            fEnd = f.getEnd();
            iStart = mod(((2 * i) - fStart), inLen);

            for(int j = fStart; j <= fEnd; j++)
            {
                outputImg.getData()[outStart + i * outStep] += f.getData()[j - fStart]
                        * inputImg.getData()[inStart + iStart * inStep];
                iStart--;
                if(iStart < 0)
                {
                    iStart += inLen;
                }
            }
        }
    }

    public static void filterInvPeriodical(Image inputImg, int inStart, int inLen, int inStep, Image outputImg,
            int outStart, int outLen, int outStep, Filter f)
    {
        int fStart = 0;
        int fEnd = 0;
        int iStart = 0;

        for(int i = 0; i < outLen; i++)
        {
            fStart = ceilingHalf(f.getStart() + i);
            fEnd = floorHalf(f.getEnd() + i);
            iStart = mod(fStart, inLen);

            for(int j = fStart; j <= fEnd; j++)
            {
                outputImg.getData()[outStart + i * outStep] += f.getData()[(2 * j) - i - f.getStart()]
                        * inputImg.getData()[inStart + iStart * inStep];
                iStart++;
                if(iStart >= inLen)
                {
                    iStart -= inLen;
                }
            }
        }
    }

    public static void filterMirror(Image inputImg, int inStart, int inLen, int inStep, Image outputImg, int outStart,
            int outLen, int outStep, Filter f)
    {
        int fStart = 0;
        int fEnd = 0;
        int inPos = 0;

        for(int i = 0; i < outLen; i++)
        {
            fStart = f.getStart();
            fEnd = f.getEnd();

            for(int j = fStart; j <= fEnd; j++)
            {
                inPos = ((2 * i) - j);
                if(inPos < 0)
                {
                    inPos = -inPos;
                    if(inPos >= inLen)
                    {
                        continue;
                    }
                }
                if(inPos >= inLen)
                {
                    inPos = 2 * inLen - 2 - inPos;
                    if(inPos < 0)
                    {
                        continue;
                    }
                }
                outputImg.getData()[outStart + i * outStep] += f.getData()[j - fStart]
                        * inputImg.getData()[inStart + inPos * inStep];
            }
        }
    }

    public static void filterInvMirror(Image inputImg, int inStart, int inLen, int inStep, Image outputImg,
            int outStart, int outLen, int outStep, Filter f)
    {
        int fStart = 0;
        int fEnd = 0;
        int inPos = 0;

        for(int i = 0; i < outLen; i++)
        {
            fStart = ceilingHalf(f.getStart() + i);
            fEnd = floorHalf(f.getEnd() + i);

            for(int j = fStart; j <= fEnd; j++)
            {
                inPos = j;
                if(inPos < 0)
                {
                    if(f.isHiPass())
                    {
                        inPos = -inPos - 1;
                    }
                    else
                    {
                        inPos = -inPos;
                    }
                    if(inPos >= inLen)
                    {
                        continue;
                    }
                }
                if(inPos >= inLen)
                {
                    if(f.isHiPass())
                    {
                        inPos = 2 * inLen - 2 - inPos;
                    }
                    else
                    {
                        inPos = 2 * inLen - 1 - inPos;
                    }
                    if(inPos < 0)
                    {
                        continue;
                    }
                }
                outputImg.getData()[outStart + i * outStep] += f.getData()[2 * j - i - f.getStart()]
                        * inputImg.getData()[inStart + inPos * inStep];
            }
        }
    }

    public static Image inverseTransform(ImageTree tree, FilterGH[] filterGHList, int method)
    {
        int width = 0;
        int height = 0;
        Image retImg = null;
        Image coarseImg = null;
        Image verticalImg = null;
        Image horizontalImg = null;
        Image diagonalImg = null;

        if(tree.getImage() == null)
        {
            coarseImg = inverseTransform(tree.getCoarse(), filterGHList, method);
            horizontalImg = inverseTransform(tree.getHorizontal(), filterGHList, method);
            verticalImg = inverseTransform(tree.getVertical(), filterGHList, method);
            diagonalImg = inverseTransform(tree.getDiagonal(), filterGHList, method);

            width = coarseImg.getWidth() + horizontalImg.getWidth();
            height = coarseImg.getHeight() + verticalImg.getHeight();

            retImg = new Image(width, height);

            if(tree.getFlag() == 0) /*if flag is set it is a doubletree tiling*/
            {
                invDecomposition(retImg, coarseImg, horizontalImg, verticalImg, diagonalImg, filterGHList[tree
                        .getLevel()], method);
            }
            else
            {
                copyIntoImage(retImg, coarseImg, 0, 0);
                copyIntoImage(retImg, horizontalImg, coarseImg.getWidth(), 0);
                copyIntoImage(retImg, verticalImg, 0, coarseImg.getHeight());
                copyIntoImage(retImg, diagonalImg, coarseImg.getWidth(), coarseImg.getHeight());
            }

            return retImg;
        }
        else
        {
            return tree.getImage();
        }
    }

    public static void invDecomposition(Image sumImg, Image coarseImg, Image horizontalImg, Image verticalImg,
            Image diagonalImg, FilterGH filterGH, int method)
    {
        Image tempImg = null;
        Filter filterG = null;
        Filter filterH = null;

        if(filterGH.getType() == FilterGH.TYPE_ORTHOGONAL)
        {
            filterG = filterGH.getG();
            filterH = filterGH.getH();
        }
        else
        {
            filterG = filterGH.getGi();
            filterH = filterGH.getHi();
        }

        /*coarse*/
        tempImg = new Image(coarseImg.getWidth(), sumImg.getHeight());
        convoluteRows(tempImg, coarseImg, filterH, method);

        /*horizontal*/
        convoluteRows(tempImg, horizontalImg, filterG, method);
        convoluteLines(sumImg, tempImg, filterH, method);

        /*vertical*/
        tempImg = new Image(verticalImg.getWidth(), sumImg.getHeight());
        convoluteRows(tempImg, verticalImg, filterH, method);

        /*diagonal*/
        convoluteRows(tempImg, diagonalImg, filterG, method);
        convoluteLines(sumImg, tempImg, filterG, method);
    }

    /**
     * Returns the floor of the half of the input value
     * 
     * @param num Input number
     * @return Floor of the half of the input number
     */
    public static int floorHalf(int num)
    {
        if((num & 1) == 1)
        {
            return (num - 1) / 2;
        }
        else
        {
            return num / 2;
        }
    }

    /**
     * Returns the ceiling of the half of the input value
     * 
     * @param num Input number
     * @return Ceiling of the half of the input number
     */
    public static int ceilingHalf(int num)
    {
        if((num & 1) == 1)
        {
            return (num + 1) / 2;
        }
        else
        {
            return num / 2;
        }
    }

    /**
     * Returns the modulus of the input value (taking care of the sign of the value)
     * 
     * @param num Input number
     * @param div Divisor for modulus
     * @return Modulus of num by div
     */
    public static int mod(int num, int div)
    {
        if(num < 0)
        {
            return div - (-num % div);
        }
        else
        {
            return num % div;
        }
    }

    public static int max(int x, int y)
    {
        return (x > y) ? x : y;
    }

    public static double max(double x, double y)
    {
        return (x > y) ? x : y;
    }

    public static int min(int x, int y)
    {
        return (x < y) ? x : y;
    }

    public static double min(double x, double y)
    {
        return (x < y) ? x : y;
    }

    /**
     * Utility method to limit the value within [0,255] range
     * 
     * @param p Input value
     * @return Limited value
     */
    public static int pixelRange(int p)
    {
        return ((p > 255) ? 255 : (p < 0) ? 0 : p);
    }

    public static int findDeepestLevel(int width, int height)
    {
        int level = 0;
        int w = width;
        int h = height;

        while((w % 2 == 0) && (h % 2 == 0))
        {
            w = w / 2;
            h = h / 2;
            level++;
        }

        return level - 1;
    }

    public static void setPixel(Image image, int x, int y, double val)
    {
        if((image != null) && (x >= 0) && (x < image.getWidth()) && (y >= 0) && (y < image.getHeight()))
        {
            image.getData()[x + (y * image.getWidth())] = val;
        }
    }

    public static double getPixel(Image image, int x, int y)
    {
        if((image != null) && (x >= 0) && (x < image.getWidth()) && (y >= 0) && (y < image.getHeight()))
        {
            return image.getData()[x + (y * image.getWidth())];
        }
        else
        {
            return Double.NaN;
        }
    }

    private static void copyIntoImage(Image img1, Image img2, int x, int y)
    {
        int count = 0;
        int start = 0;
        int aim = 0;
        double[] temp = null;

        temp = img2.getData();
        start = img1.getWidth() * y + x;

        for(int i = 0; i < img2.getHeight(); i++)
        {
            for(int j = 0; j < img2.getWidth(); j++)
            {
                aim = start + j + img1.getWidth() * i;
                img1.getData()[aim] = temp[count];
                count++;
            }
        }
    }
}