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

import android.graphics.Path;
import android.graphics.PointF;

import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.cff.CFFParser;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.cff.CFFType1Font;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.Type1Equivalent;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.util.BoundingBox;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.LogFactory;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.LogFactory.Log;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSDictionary;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSName;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.io.IOUtils;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.common.PDRectangle;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.common.PDStream;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.font.encoding.Encoding;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.font.encoding.Type1Encoding;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.Matrix;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.awt.AffineTransform;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.font.UniUtil.getUniNameOfCodePoint;

/**
 * Type 1-equivalent CFF font.
 *
 * @author Villu Ruusmann
 * @author John Hewson
 */
public class PDType1CFont extends PDSimpleFont implements PDType1Equivalent
{
    private static final Log LOG = LogFactory.getLog(PDType1CFont.class);

    private final Map<String, Float> glyphHeights = new HashMap<String, Float>();
    private Float avgWidth = null;
    private Matrix fontMatrix;
    private final AffineTransform fontMatrixTransform;

    private final CFFType1Font cffFont; // embedded font
    private final Type1Equivalent type1Equivalent; // embedded or system font for rendering
    private final boolean isEmbedded;
    private final boolean isDamaged;
    private BoundingBox fontBBox;

    /**
     * Constructor.
     * 
     * @param fontDictionary the corresponding dictionary
     * @throws IOException it something went wrong
     */
    public PDType1CFont(COSDictionary fontDictionary) throws IOException
    {
        super(fontDictionary);

        PDFontDescriptor fd = getFontDescriptor();
        byte[] bytes = null;
        if (fd != null)
        {
            PDStream ff3Stream = fd.getFontFile3();
            if (ff3Stream != null)
            {
                bytes = IOUtils.toByteArray(ff3Stream.createInputStream());
                if (bytes.length == 0)
                {
                    LOG.error("Invalid data for embedded Type1C font " + getName());
                    bytes = null;
                }
            }
        }

        boolean fontIsDamaged = false;
        CFFType1Font cffEmbedded = null;
        try
        {
            if (bytes != null)
            {
                // note: this could be an OpenType file, fortunately CFFParser can handle that
                CFFParser cffParser = new CFFParser();
                cffEmbedded = (CFFType1Font)cffParser.parse(bytes).get(0);
            }
        }
        catch (IOException e)
        {
            LOG.error("Can't read the embedded Type1C font " + getName(), e);
            fontIsDamaged = true;
        }
        isDamaged = fontIsDamaged;
        cffFont = cffEmbedded;

        if (cffFont != null)
        {
            type1Equivalent = cffFont;
            isEmbedded = true;
        }
        else
        {
            Type1Equivalent t1Equiv = ExternalFonts.getType1EquivalentFont(getBaseFont());
            if (t1Equiv != null)
            {
                type1Equivalent = t1Equiv;
            }
            else
            {
                type1Equivalent = ExternalFonts.getType1FallbackFont(getFontDescriptor());
                LOG.warn( "Using fallback font " + type1Equivalent.getName() + " for " + getBaseFont());
            }
            isEmbedded = false;
        }
        readEncoding();
        fontMatrixTransform = getFontMatrix().createAffineTransform();
        fontMatrixTransform.scale(1000, 1000);
    }

    @Override
    public Type1Equivalent getType1Equivalent()
    {
        return type1Equivalent;
    }

    /**
     * Returns the PostScript name of the font.
     */
    public final String getBaseFont()
    {
        return dict.getNameAsString(COSName.BASE_FONT);
    }

    @Override
    public Path getPath( String name) throws IOException
    {
        // Acrobat only draws .notdef for embedded or "Standard 14" fonts, see PDFBOX-2372
        if (name.equals(".notdef") && !isEmbedded() && !isStandard14())
        {
            return new Path();
        }
        else
        {
            return type1Equivalent.getPath(name);
        }
    }

/*
    @Override
    public boolean hasGlyph(String name) throws IOException
    {
        return type1Equivalent.hasGlyph(name);
    }
*/

    @Override
    public final String getName()
    {
        return getBaseFont();
    }

    @Override
    public BoundingBox getBoundingBox() throws IOException
    {
        if (fontBBox == null)
        {
            fontBBox = generateBoundingBox();
        }
        return fontBBox;
    }

    private BoundingBox generateBoundingBox() throws IOException
    {
        if (getFontDescriptor() != null) {
            PDRectangle bbox = getFontDescriptor().getFontBoundingBox();
            if (bbox != null
                    && (bbox.getLowerLeftX() != 0 || bbox.getLowerLeftY() != 0
                    || bbox.getUpperRightX() != 0 || bbox.getUpperRightY() != 0))
            {
                return new BoundingBox(bbox.getLowerLeftX(), bbox.getLowerLeftY(),
                                       bbox.getUpperRightX(), bbox.getUpperRightY());
            }
        }
        return type1Equivalent.getFontBBox();
    }

    //@Override
    public String codeToName(int code)
    {
        return getEncoding().getName(code);
    }
    
    @Override
    protected Encoding readEncodingFromFont() throws IOException
    {
        if (!isEmbedded() && getStandard14AFM() != null)
        {
            // read from AFM
            return new Type1Encoding(getStandard14AFM());
        }
        else
        {
            return Type1Encoding.fromFontBox(type1Equivalent.getEncoding());
        }
    }

    @Override
    public int readCode(InputStream in) throws IOException
    {
        return in.read();
    }

    @Override
    public final Matrix getFontMatrix()
    {
        if (fontMatrix == null)
        {
            List<Number> numbers = null;
            try
            {
                numbers = cffFont.getFontMatrix();
            }
            catch (NullPointerException e)
            {
                fontMatrix = DEFAULT_FONT_MATRIX;
            }

            if (numbers != null && numbers.size() == 6)
            {
                fontMatrix = new Matrix(
                        numbers.get(0).floatValue(), numbers.get(1).floatValue(),
                        numbers.get(2).floatValue(), numbers.get(3).floatValue(),
                        numbers.get(4).floatValue(), numbers.get(5).floatValue());
            }
            else
            {
                return super.getFontMatrix();
            }
        }
        return fontMatrix;
    }

    @Override
    public boolean isDamaged()
    {
        return isDamaged;
    }

    @Override
    public float getWidthFromFont(int code) throws IOException
    {
        String name = codeToName(code);
        float width = type1Equivalent.getWidth(name);

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
    public float getHeight(int code) throws IOException
    {
        String name = codeToName(code);
        float height = 0;
        if (!glyphHeights.containsKey(name))
        {
            height = (float)cffFont.getType1CharString(name).getBounds().height(); // todo: cffFont could be null
            glyphHeights.put(name, height);
        }
        return height;
    }

    @Override
    protected byte[] encode(int unicode) throws IOException
    {
        String name = getGlyphList().codePointToName(unicode);
        if (!encoding.contains(name))
        {
            throw new IllegalArgumentException(
                    String.format("U+%04X ('%s') is not available in this font's encoding: %s",
                                  unicode, name, encoding.getEncodingName()));
        }

        String nameInFont = getNameInFont(name);
        
        Map<String, Integer> inverted = encoding.getNameToCodeMap();

        if (nameInFont.equals(".notdef") || !type1Equivalent.hasGlyph(nameInFont))
        {
            throw new IllegalArgumentException(
                    String.format("No glyph for U+%04X in font %s", unicode, getName()));
        }

        int code = inverted.get(name);
        return new byte[] { (byte)code };
    }
    
    @Override
    public float getStringWidth(String string) throws IOException
    {
        float width = 0;
        for (int i = 0; i < string.length(); i++)
        {
            int codePoint = string.codePointAt(i);
            String name = getGlyphList().codePointToName(codePoint);
            width += cffFont.getType1CharString(name).getWidth();
        }
        return width;
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

    /**
     * Returns the embedded Type 1-equivalent CFF font.
     * 
     * @return the cffFont
     */
    public CFFType1Font getCFFType1Font()
    {
        return cffFont;
    }

    // todo: this is a replacement for FontMetrics method
    private float getAverageCharacterWidth()
    {
        // todo: not implemented, highly suspect
        return 500;
    }
    
    /**
     * Maps a PostScript glyph name to the name in the underlying font, for example when
     * using a TTF font we might map "W" to "uni0057".
     */
    private String getNameInFont(String name) throws IOException
    {
        if (isEmbedded() || type1Equivalent.hasGlyph(name))
        {
            return name;
        }
        else
        {
            // try unicode name
            String unicodes = getGlyphList().toUnicode(name);
            if (unicodes != null && unicodes.length() == 1)
            {
                String uniName = getUniNameOfCodePoint(unicodes.codePointAt(0));
                if (type1Equivalent.hasGlyph(uniName))
                {
                    return uniName;
                }
            }
        }
        return ".notdef";
    }
}
