/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.font;

import android.graphics.PointF;
import android.util.Log;

import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.cff.CFFCIDFont;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.cff.CFFFont;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.cff.CFFParser;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.cff.CFFType1Font;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.cff.Type2CharString;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.util.BoundingBox;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSDictionary;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSName;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.io.IOUtils;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.common.PDRectangle;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.common.PDStream;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.Matrix;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.awt.AffineTransform;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.font.UniUtil.getUniNameOfCodePoint;

/**
 * Type 0 CIDFont (CFF).
 * 
 * @author Ben Litchfield
 * @author John Hewson
 */
public class PDCIDFontType0 extends PDCIDFont
{

    private final CFFCIDFont cidFont;  // Top DICT that uses CIDFont operators
    private final CFFType1Font t1Font; // Top DICT that does not use CIDFont operators
    
    private final Map<Integer, Float> glyphHeights = new HashMap<Integer, Float>();
    private final boolean isEmbedded;
    private final boolean isDamaged;

    private Float avgWidth = null;
    private Matrix fontMatrix;
    private AffineTransform fontMatrixTransform;
    private BoundingBox fontBBox;

    /**
     * Constructor.
     * 
     * @param fontDictionary The font dictionary according to the PDF specification.
     * @param parent The parent font.
     */
    public PDCIDFontType0(COSDictionary fontDictionary, PDType0Font parent) throws IOException
    {
        super(fontDictionary, parent);

        PDFontDescriptor fd = getFontDescriptor();
        byte[] bytes = null;
        if (fd != null)
        {
            PDStream ff3Stream = fd.getFontFile3();
            if (ff3Stream != null)
            {
                bytes = IOUtils.toByteArray(ff3Stream.createInputStream());
            }
        }

        boolean fontIsDamaged = false;
        CFFFont cffFont = null;
        if (bytes != null && bytes.length > 0 && ( bytes[0] & 0xff ) == '%')
        {
            // PDFBOX-2642 contains a corrupt PFB font instead of a CFF
            Log.e("PdfBoxAndroid", "Unsupported: Type1 font instead of CFF in " + fd.getFontName());
            fontIsDamaged = true;
        } else if (bytes != null)
        {
            CFFParser cffParser = new CFFParser();
            try
            {
                cffFont = cffParser.parse(bytes).get(0);
            } catch (IOException e)
            {
                Log.e("PdfBoxAndroid", "Can't read the embedded CFF font " + fd.getFontName(), e);
                fontIsDamaged = true;
            }
        }


        if (cffFont != null)
        {
            // embedded
            if (cffFont instanceof CFFCIDFont)
            {
                cidFont = (CFFCIDFont)cffFont;
                t1Font = null;
            }
            else
            {
                cidFont = null;
                t1Font = (CFFType1Font)cffFont;
            }
            isEmbedded = true;
            isDamaged = false;
        }
        else
        {
            // substitute
            CFFCIDFont cidSub = ExternalFonts.getCFFCIDFont(getBaseFont());
            if (cidSub != null)
            {
                cidFont = cidSub;
                t1Font = null;
            }
            else
            {
                COSDictionary cidSystemInfo = (COSDictionary)
                        dict.getDictionaryObject(COSName.CIDSYSTEMINFO);

                String registryOrdering = null;
                if (cidSystemInfo != null)
                {
                    String registry = cidSystemInfo.getNameAsString(COSName.REGISTRY);
                    String ordering = cidSystemInfo.getNameAsString(COSName.ORDERING);
                    if (registry != null && ordering != null)
                    {
                        registryOrdering = registry + "-" + ordering;
                    }
                }

                cidSub = ExternalFonts.getCFFCIDFontFallback(registryOrdering, getFontDescriptor());
                cidFont = cidSub;
                t1Font = null;

                if (cidSub.getName().equals("AdobeBlank"))
                {
                    // this error often indicates that the user needs to install the Adobe Reader
                    // Asian and Extended Language Pack
                    if (!fontIsDamaged)
                    {
                        Log.e("PdfBoxAndroid", "Missing CID-keyed font " + getBaseFont());
                    }
                }
                else
                {
                    Log.w("PdfBoxAndroid", "Using fallback for CID-keyed font " + getBaseFont());
                }
            }
            isEmbedded = false;
            isDamaged = fontIsDamaged;
        }
        fontMatrixTransform = getFontMatrix().createAffineTransform();
        fontMatrixTransform.scale(1000, 1000);
    }

    @Override
    public final Matrix getFontMatrix()
    {
        if (fontMatrix == null)
        {
            List<Number> numbers;
            if (cidFont != null)
            {
                numbers = cidFont.getFontMatrix();
            }
            else
            {
                numbers = t1Font.getFontMatrix();
            }

            if (numbers != null && numbers.size() == 6)
            {
                fontMatrix = new Matrix(numbers.get(0).floatValue(), numbers.get(1).floatValue(),
                                        numbers.get(2).floatValue(), numbers.get(3).floatValue(),
                                        numbers.get(4).floatValue(), numbers.get(5).floatValue());
            }
            else
            {
                fontMatrix = new Matrix(0.001f, 0, 0, 0.001f, 0, 0);
            }
        }
        return fontMatrix;
    }

    @Override
    public BoundingBox getBoundingBox()
    {
        if (fontBBox == null)
        {
            fontBBox = generateBoundingBox();
        }
        return fontBBox;
    }

    private BoundingBox generateBoundingBox()
    {
        if (getFontDescriptor() != null) {
            PDRectangle bbox = getFontDescriptor().getFontBoundingBox();
            if (bbox.getLowerLeftX() != 0 || bbox.getLowerLeftY() != 0 ||
                bbox.getUpperRightX() != 0 || bbox.getUpperRightY() != 0) {
                return new BoundingBox(bbox.getLowerLeftX(), bbox.getLowerLeftY(),
                                       bbox.getUpperRightX(), bbox.getUpperRightY());
            }
        }
        if (cidFont != null)
        {
            return cidFont.getFontBBox();
        }
        else
        {
            return t1Font.getFontBBox();
        }
    }

    /**
     * Returns the embedded CFF CIDFont, or null if the substitute is not a CFF font.
     */
    public CFFFont getCFFFont()
    {
        if (cidFont != null)
        {
            return cidFont;
        }
        else if (t1Font instanceof CFFType1Font)
        {
            return (CFFType1Font)t1Font;
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the Type 2 charstring for the given CID, or null if the substituted font does not
     * contain Type 2 charstrings.
     *
     * @param cid CID
     * @throws IOException if the charstring could not be read
     */
    public Type2CharString getType2CharString(int cid) throws IOException
    {
        if (cidFont != null)
        {
            return cidFont.getType2CharString(cid);
        }
        else if (t1Font instanceof CFFType1Font)
        {
            return ((CFFType1Font)t1Font).getType2CharString(cid);
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the name of the glyph with the given character code. This is done by looking up the
     * code in the parent font's ToUnicode map and generating a glyph name from that.
     */
    private String getGlyphName(int code) throws IOException
    {
        String unicodes = parent.toUnicode(code);
        if (unicodes == null)
        {
            return ".notdef";
        }
        return getUniNameOfCodePoint(unicodes.codePointAt(0));
    }

    /**
     * Returns the CID for the given character code. If not found then CID 0 is returned.
     *
     * @param code character code
     * @return CID
     */
    @Override
    public int codeToCID(int code)
    {
        return parent.getCMap().toCID(code);
    }

    @Override
    public int codeToGID(int code)
    {
        int cid = codeToCID(code);
        if (cidFont != null)
        {
            // The CIDs shall be used to determine the GID value for the glyph procedure using the
            // charset table in the CFF program
            return cidFont.getCharset().getGIDForCID(cid);
        }
        else
        {
            // The CIDs shall be used directly as GID values
            return cid;
        }
    }

    @Override
    public byte[] encode(int unicode)
    {
        // todo: we can use a known character collection CMap for a CIDFont
        //       and an Encoding for Type 1-equivalent
        throw new UnsupportedOperationException();
    }

    @Override
    public float getWidthFromFont(int code) throws IOException
    {
        int cid = codeToCID(code);
        float width;
        if (cidFont != null)
        {
            width = getType2CharString(cid).getWidth();
        }
        else if (isEmbedded && t1Font instanceof CFFType1Font)
        {
            width = ((CFFType1Font)t1Font).getType2CharString(cid).getWidth();
        }
        else
        {
            width = t1Font.getWidth(getGlyphName(code));
        }
        
        PointF p = new PointF(width, 0);
        fontMatrixTransform.transform(p, p);
        return (float)p.x;
    }

    @Override
    public boolean isEmbedded()
    {
        return isEmbedded;
    }

    @Override
    public boolean isDamaged()
    {
        return isDamaged;
    }

    @Override
    public float getHeight(int code) throws IOException
    {
        int cid = codeToCID(code);

        float height = 0;
        if (!glyphHeights.containsKey(cid))
        {
            height =  (float) getType2CharString(cid).getBounds().height();
            glyphHeights.put(cid, height);
        }
        return height;
    }

    @Override
    public float getAverageFontWidth()
    {
        if (avgWidth == null)
        {
            avgWidth = getAverageCharacterWidth();
        }
        return avgWidth;
    }

    // todo: this is a replacement for FontMetrics method
    private float getAverageCharacterWidth()
    {
        // todo: not implemented, highly suspect
        return 500;
    }
}
