/*
 * Copyright 2023 convey authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing;

/**
 * The base class for luminance sources which supports 
 * cropping and rotating based upon the luminance values.
 *
 * @author gilbert@convey.de (Gilbet schwaab)
 */
public class BaseLuminanceSource extends LuminanceSource {

/**
 * the following channel weights give nearly the same
 * gray scale picture as the java version with BufferedImage.TYPE_BYTE_GRAY
 * they are used in sub classes for luminance / gray scale calculation
 */

  private static int RChannelWeight = 19562;
  private static int GChannelWeight = 38550;
  private static int BChannelWeight = 7424;
  private static int ChannelWeight = 16;

  private byte[] luminances;

/**
 * Initializes a new instance of the BaseLuminanceSource class.
 */
  public BaseLuminanceSource(int width, int height) {
    super(width, height);

    this.luminances = new byte[width * height];
  }
  
/**
 * Initializes a new instance of the BaseLuminanceSource class.
 */
  private BaseLuminanceSource(byte[] luminanceArray, int width, int height) {
    super(width, height);

    this.luminances = new byte[width * height];
    System.arraycopy(luminanceArray, 0, this.luminances, 0, width * height);
  }

/**
 * Fetches one row of luminance data from the underlying platform's bitmap. Values range from
 * 0 (black) to 255 (white). It is preferable for implementations of this method
 * to only fetch this row rather than the whole image, since no 2D Readers may be installed and
 * getMatrix() may never be called.
 */
  @Override
  public byte[] getRow(int y, byte[] row) {
    if (y < 0 || y >= getHeight()) {
      throw new IllegalArgumentException("Requested row is outside the image: " + y);
    }
    int width = getWidth();
    if (row == null || row.length < width) {
      row = new byte[width];
    }
    System.arraycopy(this.luminances, y * width, row, 0, width);
    return row;
  }

  @Override
  public byte[] getMatrix() {
      return this.luminances;
  }
  
/**
 * Returns a new object with rotated image data by 90 degrees counterclockwise.
 * Only callable if isRotateSupported() is true.
 */
  @Override
  public boolean isRotateSupported() {
    return true;
  }

  @Override
  public LuminanceSource rotateCounterClockwise() {
    byte[] rotatedLuminances = new byte[getWidth() * getHeight()];
    int newWidth = getHeight();
    int newHeight = getWidth();
    byte[] localLuminances = getMatrix();
    for (int yold = 0; yold < getHeight(); yold++)
    {
	  for (int xold = 0; xold < getWidth(); xold++)
	  {
		int ynew = newHeight - xold - 1;
		int xnew = yold;
		rotatedLuminances[ynew * newWidth + xnew] = localLuminances[yold * getWidth() + xold];
	  }
    }
	return new BaseLuminanceSource(rotatedLuminances, newWidth, newHeight);
  }
  
  @Override
  public boolean isCropSupported() {
    return true;
  }


/**
 * Returns a new object with cropped image data. Implementations may keep a reference to the
 * original data rather than a copy. Only callable if CropSupported is true.
 */
  @Override
  public LuminanceSource crop(int left, int top, int width, int height) {
    if (left + width > getWidth() || top + height > getHeight()) {
	  throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
	}
    byte[] croppedLuminances = new byte[width * height];
    byte[] oldLuminances = getMatrix();
    int oldWidth = getWidth();
    int oldRightBound = left + width;
    int oldBottomBound = top + height;
    for (int yold = top, ynew = 0; yold < oldBottomBound; yold++, ynew++)
    {
      for (int xold = left, xnew = 0; xold < oldRightBound; xold++, xnew++)
      {
        croppedLuminances[ynew * width + xnew] = oldLuminances[yold * oldWidth + xold];
      }
    }
	return new BaseLuminanceSource(croppedLuminances, width, height);
  }
}
