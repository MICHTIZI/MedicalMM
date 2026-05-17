package com.ruoyi.emr.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Match OpenCV pipeline: GRAY -&gt; CLAHE(clipLimit=3, tileGridSize=8x8) -&gt; Laplacian sharpen.
 * tileGridSize is the number of tiles along each axis (NOT pixel tile size).
 */
public final class XrayImageEnhancer
{
    /** OpenCV createCLAHE(..., tileGridSize=(8, 8)) */
    private static final int TILES_GRID_X = 8;
    private static final int TILES_GRID_Y = 8;
    private static final double CLIP_LIMIT = 3.0;
    private static final int[][] LAPLACIAN = {
        { 0, -1, 0 },
        { -1, 5, -1 },
        { 0, -1, 0 }
    };

    private XrayImageEnhancer()
    {
    }

    public static BufferedImage enhance(BufferedImage source)
    {
        if (source == null)
        {
            throw new IllegalArgumentException("source image is null");
        }
        int w = source.getWidth();
        int h = source.getHeight();
        int[][] gray = toGrayMatrix(source, w, h);
        int[][] clahe = applyClahe(gray, w, h);
        int[][] sharp = convolve(clahe, w, h, LAPLACIAN);
        return toGrayBufferedImage(sharp, w, h);
    }

    private static int[][] toGrayMatrix(BufferedImage img, int w, int h)
    {
        BufferedImage grayImg = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        grayImg.getGraphics().drawImage(img, 0, 0, null);
        byte[] pixels = ((DataBufferByte) grayImg.getRaster().getDataBuffer()).getData();
        int[][] m = new int[h][w];
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                m[y][x] = pixels[y * w + x] & 0xFF;
            }
        }
        return m;
    }

    private static BufferedImage toGrayBufferedImage(int[][] m, int w, int h)
    {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        byte[] pixels = ((DataBufferByte) out.getRaster().getDataBuffer()).getData();
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                pixels[y * w + x] = (byte) clamp(m[y][x]);
            }
        }
        return out;
    }

    private static int tileStart(int index, int tiles, int size)
    {
        return index * size / tiles;
    }

    private static int tileEnd(int index, int tiles, int size)
    {
        return (index + 1) * size / tiles;
    }

    private static int[][] applyClahe(int[][] gray, int w, int h)
    {
        int tilesX = TILES_GRID_X;
        int tilesY = TILES_GRID_Y;
        int[][][] lut = new int[tilesY][tilesX][256];

        for (int ty = 0; ty < tilesY; ty++)
        {
            int y0 = tileStart(ty, tilesY, h);
            int y1 = tileEnd(ty, tilesY, h);
            for (int tx = 0; tx < tilesX; tx++)
            {
                int x0 = tileStart(tx, tilesX, w);
                int x1 = tileEnd(tx, tilesX, w);
                int count = (x1 - x0) * (y1 - y0);
                if (count <= 0)
                {
                    continue;
                }
                int[] hist = new int[256];
                for (int y = y0; y < y1; y++)
                {
                    for (int x = x0; x < x1; x++)
                    {
                        hist[gray[y][x]]++;
                    }
                }
                double clipThreshold = CLIP_LIMIT * count / 256.0;
                int redist = 0;
                for (int i = 0; i < 256; i++)
                {
                    if (hist[i] > clipThreshold)
                    {
                        redist += hist[i] - (int) clipThreshold;
                        hist[i] = (int) clipThreshold;
                    }
                }
                int perBin = redist / 256;
                int remainder = redist % 256;
                for (int i = 0; i < 256; i++)
                {
                    hist[i] += perBin;
                }
                for (int i = 0; i < remainder; i++)
                {
                    hist[i]++;
                }
                int cum = 0;
                for (int i = 0; i < 256; i++)
                {
                    cum += hist[i];
                    lut[ty][tx][i] = clamp((cum * 255) / count);
                }
            }
        }

        int[][] out = new int[h][w];
        for (int y = 0; y < h; y++)
        {
            double tyf = (y + 0.5) * tilesY / (double) h - 0.5;
            int ty0 = clampIndex((int) Math.floor(tyf), tilesY - 1);
            int ty1 = clampIndex(ty0 + 1, tilesY - 1);
            double wy = tyf - Math.floor(tyf);

            for (int x = 0; x < w; x++)
            {
                double txf = (x + 0.5) * tilesX / (double) w - 0.5;
                int tx0 = clampIndex((int) Math.floor(txf), tilesX - 1);
                int tx1 = clampIndex(tx0 + 1, tilesX - 1);
                double wx = txf - Math.floor(txf);

                int v = gray[y][x];
                double v00 = lut[ty0][tx0][v];
                double v01 = lut[ty0][tx1][v];
                double v10 = lut[ty1][tx0][v];
                double v11 = lut[ty1][tx1][v];
                double top = v00 * (1 - wx) + v01 * wx;
                double bottom = v10 * (1 - wx) + v11 * wx;
                out[y][x] = clamp((int) Math.round(top * (1 - wy) + bottom * wy));
            }
        }
        return out;
    }

    private static int clampIndex(int t, int max)
    {
        if (t < 0)
        {
            return 0;
        }
        return Math.min(t, max);
    }

    private static int[][] convolve(int[][] src, int w, int h, int[][] kernel)
    {
        int kh = kernel.length;
        int kw = kernel[0].length;
        int oy = kh / 2;
        int ox = kw / 2;
        int[][] dst = new int[h][w];
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                double sum = 0;
                for (int ky = 0; ky < kh; ky++)
                {
                    for (int kx = 0; kx < kw; kx++)
                    {
                        int sy = reflect(y + ky - oy, h);
                        int sx = reflect(x + kx - ox, w);
                        sum += src[sy][sx] * kernel[ky][kx];
                    }
                }
                dst[y][x] = clamp((int) Math.round(sum));
            }
        }
        return dst;
    }

    private static int reflect(int i, int size)
    {
        if (i < 0)
        {
            return -i;
        }
        if (i >= size)
        {
            return 2 * size - i - 2;
        }
        return i;
    }

    private static int clamp(int v)
    {
        if (v < 0)
        {
            return 0;
        }
        return Math.min(v, 255);
    }
}
