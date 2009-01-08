/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2008 Samir Vaidya
 */

package net.sourceforge.openstego.util.dct;

import net.sourceforge.openstego.util.ImageUtil;

/**
 * Class to handle Discrete Cosine Transforms (DCT).
 *
 * This class is conversion of C to Java for the file "dct.c" file provided by Peter Meerwald at:
 *    http://www.cosy.sbg.ac.at/~pmeerw/Watermarking/
 *
 * Refer to his thesis on watermarking:
 *    Peter Meerwald, Digital Image Watermarking in the Wavelet Transfer Domain,
 *    Master's Thesis, Department of Scientific Computing, University of Salzburg, Austria, January 2001.
 */
public class DCT
{
    /**
     * Constant for the JPEG block size
     */
    public static final int NJPEG = 8;

    /**
     * Default JPEG quality to use for encoding
     */
    public static final int QUALITY = 75;

    /**
     * Constant for Inverse of Square Root of 2
     */
    private static final double INVROOT2 = 0.7071067814;

    /**
     * JPEG Luminance Quantization Table
     */
    private static final int[][] JPEG_LUMIN_QUANT_TBL = {
        { 16, 11, 10, 16, 24, 40, 51, 61 },
        { 12, 12, 14, 19, 26, 58, 60, 55 },
        { 14, 13, 16, 24, 40, 57, 69, 56 },
        { 14, 17, 22, 29, 51, 87, 80, 62 },
        { 18, 22, 37, 56, 68, 109, 103, 77 },
        { 24, 35, 55, 64, 81, 104, 113, 92 },
        { 49, 64, 78, 87, 103, 121, 120, 101 },
        { 72, 92, 95, 98, 112, 100, 103, 99 }
    };

    /**
     * JPEG Chrominance Quantization Table
     */
    private static final int[][] JPEG_CHROMIN_QUANT_TBL = {
        { 17, 18, 24, 47, 99, 99, 99, 99 },
        { 18, 21, 26, 66, 99, 99, 99, 99 },
        { 24, 26, 56, 99, 99, 99, 99, 99 },
        { 47, 66, 99, 99, 99, 99, 99, 99 },
        { 99, 99, 99, 99, 99, 99, 99, 99 },
        { 99, 99, 99, 99, 99, 99, 99, 99 },
        { 99, 99, 99, 99, 99, 99, 99, 99 },
        { 99, 99, 99, 99, 99, 99, 99, 99 }
    };

    private double[][] nxmCosTableX = null;

    private double[][] nxmCosTableY = null;

    private double[] nxnTmp = null;

    private double[] nxnCosTable = null;

    private double[][] C = new double[NJPEG][NJPEG];

    private double[][] Ct = new double[NJPEG][NJPEG];

    private int[][] tmpIntArray = new int[NJPEG][NJPEG];

    private int[][] Quantum = new int[NJPEG][NJPEG];

    private int nxnLog2N = 0;

    private int N = 0;

    private int M = 0;

    /**
     * Initialize DCT mechanism for N x M matrix
     * @param cols Number of columns
     * @param rows Number of rows
     * @throws IllegalArgumentException
     */
    public void initDctNxM(int cols, int rows) throws IllegalArgumentException
    {
        if(cols <= 0 || rows <= 0)
        {
            throw new IllegalArgumentException("Dimensions out of range");
        }

        int i = 0;
        int j = 0;
        double cx = Math.sqrt(2.0 / cols);
        double cy = Math.sqrt(2.0 / rows);

        if(nxmCosTableX != null && N != cols)
        {
            nxmCosTableX = null;
        }
        if(nxmCosTableY != null && M != rows)
        {
            nxmCosTableY = null;
        }

        if(nxmCosTableX == null)
        {
            nxmCosTableX = new double[cols][cols];
        }
        if(nxmCosTableY == null)
        {
            nxmCosTableY = new double[rows][rows];
        }

        N = cols;
        M = rows;

        for(i = 0; i < cols; i++)
        {
            for(j = 0; j < cols; j++)
            {
                nxmCosTableX[i][j] = cx * Math.cos((Math.PI * ((2 * i + 1) * j)) / (2 * N));
            }
        }

        for(i = 0; i < rows; i++)
        {
            for(j = 0; j < rows; j++)
            {
                nxmCosTableY[i][j] = cy * Math.cos((Math.PI * ((2 * i + 1) * j)) / (2 * M));
            }
        }
    }

    /**
     * Perform forward DCT for N x M matrix
     * @param pixels Input matrix
     * @param dcts DCT matrix
     */
    public void fwdDctNxM(int[][] pixels, double[][] dcts)
    {
        int x = 0;
        int y = 0;
        int i = 0;
        int j = 0;
        double t = 0.0;
        double cx0 = Math.sqrt(1.0 / N);
        double cy0 = Math.sqrt(1.0 / M);

        for(x = 0; x < N; x++)
        {
            for(y = 0; y < M; y++)
            {
                t += (pixels[x][y] - 128);
            }
        }
        dcts[0][0] = cx0 * cy0 * t;

        for(i = 1; i < N; i++)
        {
            t = 0.0;
            for(x = 0; x < N; x++)
            {
                for(y = 0; y < M; y++)
                {
                    t += (pixels[x][y] - 128) * nxmCosTableX[x][i];
                }
            }
            dcts[i][0] = cy0 * t;
        }

        for(j = 1; j < M; j++)
        {
            t = 0.0;
            for(x = 0; x < N; x++)
            {
                for(y = 0; y < M; y++)
                {
                    t += (pixels[x][y] - 128) * nxmCosTableY[y][j];
                }
            }
            dcts[0][j] = cx0 * t;
        }

        for(i = 1; i < N; i++)
        {
            for(j = 1; j < M; j++)
            {
                t = 0.0;
                for(x = 0; x < N; x++)
                {
                    for(y = 0; y < M; y++)
                    {
                        t += (pixels[x][y] - 128) * nxmCosTableX[x][i] * nxmCosTableY[y][j];
                    }
                }
                dcts[i][j] = t;
            }
        }
    }

    /**
     * Perform inverse DCT on the N x M matrix
     * @param dcts Input DCT matrix
     * @param pixels Output matrix
     */
    public void invDctNxM(double[][] dcts, int[][] pixels)
    {
        int x = 0;
        int y = 0;
        int i = 0;
        int j = 0;
        double t = 0.0;
        double cx0 = Math.sqrt(1.0 / N);
        double cy0 = Math.sqrt(1.0 / M);

        for(x = 0; x < N; x++)
        {
            for(y = 0; y < M; y++)
            {
                t = cx0 * cy0 * dcts[0][0];

                for(i = 1; i < N; i++)
                {
                    t += cy0 * dcts[i][0] * nxmCosTableX[x][i];
                }

                for(j = 1; j < M; j++)
                {
                    t += cx0 * dcts[0][j] * nxmCosTableY[y][j];
                }

                for(i = 1; i < N; i++)
                {
                    for(j = 1; j < M; j++)
                    {
                        t += dcts[i][j] * nxmCosTableX[x][i] * nxmCosTableY[y][j];
                    }
                }

                pixels[x][y] = ImageUtil.pixelRange((int) (t + 128.5));
            }
        }
    }

    /**
     * Initialize DCT mechanism for N x M matrix
     * @param width Width of the matrix
     * @param height Height of the matrix
     * @throws IllegalArgumentException
     */
    public void initDctNxN(int width, int height) throws IllegalArgumentException
    {
        if(width != height || width <= 0)
        {
            throw new IllegalArgumentException("Dimensions out of range");
        }

        if(nxnTmp != null && M != height)
        {
            nxnTmp = null;
        }

        N = width;
        M = height;

        nxnTmp = new double[height];
        initCosArray();
    }

    /**
     * Perform forward DCT for N x N matrix
     * @param pixels Input matrix
     * @param dcts DCT matrix
     */
    public void fwdDctNxN(int[][] pixels, double[][] dcts)
    {
        int u = 0;
        int v = 0;
        double two_over_sqrtncolsnrows = 2.0 / Math.sqrt((double) N * M);

        for(u = 0; u < N; u++)
        {
            for(v = 0; v < M; v++)
            {
                dcts[u][v] = (pixels[u][v] - 128);
            }
        }

        for(u = 0; u <= M - 1; u++)
        {
            fctNoScale(dcts[u]);
        }

        for(v = 0; v <= N - 1; v++)
        {
            for(u = 0; u <= M - 1; u++)
            {
                nxnTmp[u] = dcts[u][v];
            }

            fctNoScale(nxnTmp);
            for(u = 0; u <= M - 1; u++)
            {
                dcts[u][v] = nxnTmp[u] * two_over_sqrtncolsnrows;
            }
        }
    }

    /**
     * Perform inverse DCT on the N x N matrix
     * @param dcts Input DCT matrix
     * @param pixels Output matrix
     */
    public void invDctNxN(double[][] dcts, int[][] pixels)
    {
        int u = 0;
        int v = 0;
        double two_over_sqrtncolsnrows = 2.0 / Math.sqrt((double) N * M);
        double[][] tmp = null;

        tmp = new double[N][N];
        for(u = 0; u < N; u++)
        {
            for(v = 0; v < M; v++)
            {
                tmp[u][v] = dcts[u][v];
            }
        }

        for(u = 0; u <= M - 1; u++)
        {
            invFctNoScale(tmp[u]);
        }

        for(v = 0; v <= N - 1; v++)
        {
            for(u = 0; u <= M - 1; u++)
            {
                nxnTmp[u] = tmp[u][v];
            }

            invFctNoScale(nxnTmp);
            for(u = 0; u <= M - 1; u++)
            {
                tmp[u][v] = nxnTmp[u] * two_over_sqrtncolsnrows;
            }
        }

        for(u = 0; u < N; u++)
        {
            for(v = 0; v < M; v++)
            {
                pixels[u][v] = ImageUtil.pixelRange((int) (tmp[u][v] + 128.5));
            }
        }

        tmp = null;
    }

    /**
     * Perform forward DCT in place for N x N matrix
     * @param coeffs DCT matrix
     */
    public void fwdDctInPlaceNxN(double[][] coeffs)
    {
        int u = 0;
        int v = 0;
        double two_over_sqrtncolsnrows = 2.0 / Math.sqrt((double) N * M);

        for(u = 0; u <= M - 1; u++)
        {
            fctNoScale(coeffs[u]);
        }

        for(v = 0; v <= N - 1; v++)
        {
            for(u = 0; u <= M - 1; u++)
            {
                nxnTmp[u] = coeffs[u][v];
            }

            fctNoScale(nxnTmp);
            for(u = 0; u <= M - 1; u++)
            {
                coeffs[u][v] = nxnTmp[u] * two_over_sqrtncolsnrows;
            }
        }
    }

    /**
     * Perform inverse DCT in place for N x N matrix
     * @param coeffs DCT matrix
     */
    public void invDctInPlaceNxN(double[][] coeffs)
    {
        int u = 0;
        int v = 0;
        double two_over_sqrtncolsnrows = 2.0 / Math.sqrt((double) N * M);

        for(u = 0; u <= M - 1; u++)
        {
            invFctNoScale(coeffs[u]);
        }

        for(v = 0; v <= N - 1; v++)
        {
            for(u = 0; u <= M - 1; u++)
            {
                nxnTmp[u] = coeffs[u][v];
            }

            invFctNoScale(nxnTmp);
            for(u = 0; u <= M - 1; u++)
            {
                coeffs[u][v] = nxnTmp[u] * two_over_sqrtncolsnrows;
            }
        }
    }

    /**
     * Initialize quantization table based on the quality
     */
    public void initQuantum8x8()
    {
        for(int i = 0; i < NJPEG; i++)
        {
            for(int j = 0; j < NJPEG; j++)
            {
                Quantum[i][j] = 1 + ((1 + i + j) * QUALITY);
            }
        }
    }

    /**
     * Initialize quantization table based on JPEG luminance quantization 
     */
    public void initQuantumJpegLumin()
    {
        int quality = QUALITY;

        if(quality < 50)
        {
            quality = 5000 / quality;
        }
        else
        {
            quality = 200 - quality * 2;
        }

        for(int i = 0; i < NJPEG; i++)
        {
            for(int j = 0; j < NJPEG; j++)
            {
                if(quality != 0)
                {
                    Quantum[i][j] = (JPEG_LUMIN_QUANT_TBL[i][j] * quality + 50) / 100;
                }
                else
                {
                    Quantum[i][j] = JPEG_LUMIN_QUANT_TBL[i][j];
                }
            }
        }
    }

    /**
     * Initialize quantization table based on JPEG chrominance quantization 
     */
    public void initQuantumJpegChromin()
    {
        int quality = QUALITY;

        if(quality < 50)
        {
            quality = 5000 / quality;
        }
        else
        {
            quality = 200 - quality * 2;
        }

        for(int i = 0; i < NJPEG; i++)
        {
            for(int j = 0; j < NJPEG; j++)
            {
                if(quality != 0)
                {
                    Quantum[i][j] = (JPEG_CHROMIN_QUANT_TBL[i][j] * quality + 50) / 100;
                }
                else
                {
                    Quantum[i][j] = JPEG_CHROMIN_QUANT_TBL[i][j];
                }
            }
        }
    }

    /**
     * Quantize the DCT matrix based on the quantization table
     * @param transform DCT matrix
     */
    public void quantize8x8(double[][] transform)
    {
        for(int i = 0; i < NJPEG; i++)
        {
            for(int j = 0; j < NJPEG; j++)
            {
                transform[i][j] = round(transform[i][j] / Quantum[i][j]);
            }
        }
    }

    /**
     * De-quantize the DCT matrix based on the quantization table 
     * @param transform DCT matrix
     */
    public void dequantize8x8(double[][] transform)
    {
        for(int i = 0; i < NJPEG; i++)
        {
            for(int j = 0; j < NJPEG; j++)
            {
                transform[i][j] = round(transform[i][j] * Quantum[i][j]);
            }
        }
    }

    /**
     * Initialize DCT mechanism  for 8x8 block
     */
    public void initDct8x8()
    {
        int i = 0;
        int j = 0;
        double sqJpeg = Math.sqrt(NJPEG);
        double sqJpeg2 = Math.sqrt(2.0 / NJPEG);

        for(j = 0; j < NJPEG; j++)
        {
            C[0][j] = 1.0 / sqJpeg;
            Ct[j][0] = C[0][j];
        }

        for(i = 1; i < NJPEG; i++)
        {
            for(j = 0; j < NJPEG; j++)
            {
                C[i][j] = sqJpeg2 * Math.cos(Math.PI * (2 * j + 1) * i / (2.0 * NJPEG));
                Ct[j][i] = C[i][j];
            }
        }
    }

    /**
     * Perform forward DCT on the 8x8 matrix
     * @param input Input matrix
     * @param output Output matrix
     */
    public void fwdDct8x8(int[][] input, double[][] output)
    {
        double[][] temp = new double[NJPEG][NJPEG];
        double temp1 = 0.0;
        int i = 0;
        int j = 0;
        int k = 0;

        // MatrixMultiply(temp, input, Ct)
        for(i = 0; i < NJPEG; i++)
        {
            for(j = 0; j < NJPEG; j++)
            {
                temp[i][j] = 0.0;
                for(k = 0; k < NJPEG; k++)
                {
                    temp[i][j] += (input[i][k] - 128) * Ct[k][j];
                }
            }
        }

        // MatrixMultiply(output, C, temp)
        for(i = 0; i < NJPEG; i++)
        {
            for(j = 0; j < NJPEG; j++)
            {
                temp1 = 0.0;
                for(k = 0; k < NJPEG; k++)
                {
                    temp1 += C[i][k] * temp[k][j];
                }
                output[i][j] = temp1;
            }
        }
    }

    /**
     * Perform forward DCT on a given 8x8 block of the input matrix
     * @param input Input matrix
     * @param col Starting column number for the 8x8 block
     * @param row Starting row number for the 8x8 block
     * @param output Output matrix
     */
    public void fwdDctBlock8x8(int[][] input, int col, int row, double[][] output)
    {
        for(int i = 0; i < NJPEG; i++)
        {
            for(int j = 0; j < NJPEG; j++)
            {
                tmpIntArray[i][j] = input[col + i][row + j];
            }
        }

        fwdDct8x8(tmpIntArray, output);
    }

    /**
     * Perform inverse DCT on the 8x8 matrix
     * @param input Input matrix
     * @param output Output matrix
     */
    public void invDct8x8(double[][] input, int[][] output)
    {
        double[][] temp = new double[NJPEG][NJPEG];
        double temp1 = 0.0;
        int i = 0;
        int j = 0;
        int k = 0;

        // MatrixMultiply(temp, input, C)
        for(i = 0; i < NJPEG; i++)
        {
            for(j = 0; j < NJPEG; j++)
            {
                temp[i][j] = 0.0;
                for(k = 0; k < NJPEG; k++)
                {
                    temp[i][j] += input[i][k] * C[k][j];
                }
            }
        }

        // MatrixMultiply(output, Ct, temp)
        for(i = 0; i < NJPEG; i++)
        {
            for(j = 0; j < NJPEG; j++)
            {
                temp1 = 0.0;
                for(k = 0; k < NJPEG; k++)
                {
                    temp1 += Ct[i][k] * temp[k][j];
                }
                temp1 += 128.0;
                output[i][j] = ImageUtil.pixelRange(round(temp1));
            }
        }
    }

    /**
     * Perform inverse DCT to given 8x8 block of the output matrix
     * @param input Input matrix (8x8)
     * @param output Output matrix
     * @param col Starting column number for the 8x8 block
     * @param row Starting row number for the 8x8 block
     */
    public void invDctBlock8x8(double[][] input, int[][] output, int col, int row)
    {
        invDct8x8(input, tmpIntArray);

        for(int i = 0; i < NJPEG; i++)
        {
            for(int j = 0; j < NJPEG; j++)
            {
                output[col + i][row + j] = tmpIntArray[i][j];
            }
        }
    }

    /**
     * Check whether the coefficient is part of the middle frequencies
     * @param coeff Coefficient number
     * @return Integer to indicate band of frequency
     */
    public int isMidFreqCoeff8x8(int coeff)
    {
        switch(coeff)
        {
            case 3:
            case 10:
            case 17:
            case 24:
                return 1;

            case 4:
            case 11:
            case 18:
            case 25:
            case 32:
                return 2;

            case 5:
            case 12:
            case 19:
            case 26:
            case 33:
            case 40:
                return 3;

            case 13:
            case 20:
            case 27:
            case 34:
            case 41:
                return 4;

            case 28:
            case 35:
                return 5;

            default:
                return 0;
        }
    }

    /**
     * Utility rounding method
     * @param a Input value
     * @return Rounded value
     */
    private int round(double a)
    {
        return ((a < 0) ? (int) (a - 0.5) : (int) (a + 0.5));
    }

    private void initCosArray() throws IllegalArgumentException
    {
        int i;
        int group;
        int base;
        int item;
        int nitems;
        int halfN;
        double factor;

        nxnLog2N = -1;
        do
        {
            nxnLog2N++;
            if((1 << nxnLog2N) > N)
            {
                throw new IllegalArgumentException("dct_NxN: " + N + " is not a power of 2");
            }
        }
        while((1 << nxnLog2N) < N);

        nxnCosTable = new double[N];
        halfN = N / 2;

        for(i = 0; i <= halfN - 1; i++)
        {
            nxnCosTable[halfN + i] = 4 * i + 1;
        }
        for(group = 1; group <= nxnLog2N - 1; group++)
        {
            base = 1 << (group - 1);
            nitems = base;
            factor = 1.0 * (1 << (nxnLog2N - group));
            for(item = 1; item <= nitems; item++)
            {
                nxnCosTable[base + item - 1] = factor * nxnCosTable[halfN + item - 1];
            }
        }

        for(i = 1; i <= N - 1; i++)
        {
            nxnCosTable[i] = 1.0 / (2.0 * Math.cos(nxnCosTable[i] * Math.PI / (2.0 * N)));
        }
    }

    private void bitrev(double[] f, int len, int startIdx)
    {
        int i;
        int j;
        int m;
        double tmp;

        if(len <= 2)
            return; /* No action necessary if n=1 or n=2 */

        j = 1;
        for(i = 1; i <= len; i++)
        {
            if(i < j)
            {
                tmp = f[startIdx + j - 1];
                f[startIdx + j - 1] = f[startIdx + i - 1];
                f[startIdx + i - 1] = tmp;
            }

            m = len >> 1;
            while(j > m)
            {
                j = j - m;
                m = (m + 1) >> 1;
            }
            j = j + m;
        }
    }

    private void invSums(double[] f)
    {
        int stepsize;
        int stage;
        int curptr;
        int nthreads;
        int thread;
        int step;
        int nsteps;

        for(stage = 1; stage <= nxnLog2N - 1; stage++)
        {
            nthreads = 1 << (stage - 1);
            stepsize = nthreads << 1;
            nsteps = (1 << (nxnLog2N - stage)) - 1;

            for(thread = 1; thread <= nthreads; thread++)
            {
                curptr = N - thread;
                for(step = 1; step <= nsteps; step++)
                {
                    f[curptr] += f[curptr - stepsize];
                    curptr -= stepsize;
                }
            }
        }
    }

    private void fwdSums(double[] f)
    {
        int stepsize;
        int stage;
        int curptr;
        int nthreads;
        int thread;
        int step;
        int nsteps;

        for(stage = nxnLog2N - 1; stage >= 1; stage--)
        {
            nthreads = 1 << (stage - 1);
            stepsize = nthreads << 1;
            nsteps = (1 << (nxnLog2N - stage)) - 1;
            for(thread = 1; thread <= nthreads; thread++)
            {
                curptr = nthreads + thread - 1;
                for(step = 1; step <= nsteps; step++)
                {
                    f[curptr] += f[curptr + stepsize];
                    curptr += stepsize;
                }
            }
        }
    }

    private void scramble(double[] f, int len)
    {
        int i;
        int ii1;
        int ii2;
        double tmp;

        bitrev(f, len, 0);
        bitrev(f, len >> 1, 0);
        bitrev(f, len >> 1, len >> 1);

        ii1 = len - 1;
        ii2 = len >> 1;
        for(i = 0; i < (len >> 2); i++)
        {
            tmp = f[ii1];
            f[ii1] = f[ii2];
            f[ii2] = tmp;

            ii1--;
            ii2++;
        }
    }

    private void unscramble(double[] f, int len)
    {
        int i;
        int ii1;
        int ii2;
        double tmp;

        ii1 = len - 1;
        ii2 = len >> 1;

        for(i = 0; i < (len >> 2); i++)
        {
            tmp = f[ii1];
            f[ii1] = f[ii2];
            f[ii2] = tmp;

            ii1--;
            ii2++;
        }

        bitrev(f, len >> 1, 0);
        bitrev(f, len >> 1, len >> 1);
        bitrev(f, len, 0);
    }

    private void invButterflies(double[] f)
    {
        int stage;
        int ii1;
        int ii2;
        int butterfly;
        int ngroups;
        int group;
        int wingspan;
        int increment;
        int baseptr;
        double Cfac;
        double T;

        for(stage = 1; stage <= nxnLog2N; stage++)
        {
            ngroups = 1 << (nxnLog2N - stage);
            wingspan = 1 << (stage - 1);
            increment = wingspan << 1;
            for(butterfly = 1; butterfly <= wingspan; butterfly++)
            {
                Cfac = nxnCosTable[wingspan + butterfly - 1];
                baseptr = 0;
                for(group = 1; group <= ngroups; group++)
                {
                    ii1 = baseptr + butterfly - 1;
                    ii2 = ii1 + wingspan;
                    T = Cfac * f[ii2];
                    f[ii2] = f[ii1] - T;
                    f[ii1] = f[ii1] + T;
                    baseptr += increment;
                }
            }
        }
    }

    private void fwdButterflies(double[] f)
    {
        int stage;
        int ii1;
        int ii2;
        int butterfly;
        int ngroups;
        int group;
        int wingspan;
        int increment;
        int baseptr;
        double Cfac;
        double T;

        for(stage = nxnLog2N; stage >= 1; stage--)
        {
            ngroups = 1 << (nxnLog2N - stage);
            wingspan = 1 << (stage - 1);
            increment = wingspan << 1;
            for(butterfly = 1; butterfly <= wingspan; butterfly++)
            {
                Cfac = nxnCosTable[wingspan + butterfly - 1];
                baseptr = 0;
                for(group = 1; group <= ngroups; group++)
                {
                    ii1 = baseptr + butterfly - 1;
                    ii2 = ii1 + wingspan;
                    T = f[ii2];
                    f[ii2] = Cfac * (f[ii1] - T);
                    f[ii1] = f[ii1] + T;
                    baseptr += increment;
                }
            }
        }
    }

    private void invFctNoScale(double[] f)
    {
        f[0] *= INVROOT2;
        invSums(f);
        bitrev(f, N, 0);
        invButterflies(f);
        unscramble(f, N);
    }

    private void fctNoScale(double[] f)
    {
        scramble(f, N);
        fwdButterflies(f);
        bitrev(f, N, 0);
        fwdSums(f);
        f[0] *= INVROOT2;
    }
}
