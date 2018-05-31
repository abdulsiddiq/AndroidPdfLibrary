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
package pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.rendering;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;

import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.LogFactory;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.LogFactory.Log;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.PDFGraphicsStreamEngine;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSArray;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSBase;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSName;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSNumber;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.PDPage;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.common.PDRectangle;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.font.PDCIDFontType0;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.font.PDCIDFontType2;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.font.PDFont;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.font.PDTrueTypeFont;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.font.PDType0Font;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.font.PDType1CFont;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.font.PDType1Font;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.color.PDColor;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.color.PDColorSpace;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.color.PDPattern;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.form.PDFormXObject;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.image.PDImage;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.shading.PDShading;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.state.PDSoftMask;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.state.RenderingMode;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.Matrix;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.Vector;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.awt.AffineTransform;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Paints a page in a PDF document to a Graphics context. May be subclassed to provide custom
 * rendering.
 * 
 * <p>If you want to do custom graphics processing rather than Graphics2D rendering, then you should
 * subclass PDFGraphicsStreamEngine instead. Subclassing PageDrawer is only suitable for cases
 * where the goal is to render onto a Graphics2D surface.
 * 
 * @author Ben Litchfield
 */
public class PageDrawer extends PDFGraphicsStreamEngine
{
    private static final Log LOG = LogFactory.getLog(PageDrawer.class);


    // the graphics device to draw to, xform is the initial transform of the device (i.e. DPI)
    Paint paint;
    Canvas canvas;
    private AffineTransform xform;

    // the page box to draw (usually the crop box but may be another)
    private PDRectangle pageSize;

    private int pageRotation;

    // whether image of a transparency group must be flipped
    // needed when in a tiling pattern
    private boolean flipTG = false;

    // clipping winding rule used for the clipping path
    private Path.FillType clipWindingRule = null;
    private Path linePath = new Path();

    // last clipping path
    private Region lastClip;

    // buffered clipping area for text being drawn
    private Region textClippingArea;

    // glyph cache
    private final Map<PDFont, Glyph2D> fontGlyph2D = new HashMap<PDFont, Glyph2D>();


    /**
     * Constructor.
     *
     * @throws IOException If there is an error loading properties from the file.
     */
    public PageDrawer( PDPage page ) throws IOException
    {
        super(page);
    }


    /**
     * Sets high-quality rendering hints on the current Graphics2D.
     */
    private void setRenderingHints()
    {
/*
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                  RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                                  RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                  RenderingHints.VALUE_ANTIALIAS_ON);
*/
        paint.setAntiAlias(true);
    }

    /**
     * Draws the page to the requested context.
     *
     * @param pageSize The size of the page to draw.
     * @throws IOException If there is an IO error while drawing the page.
     */
    public void drawPage( Paint p, Canvas c, PDRectangle pageSize ) throws IOException
    {
        paint = p;
        canvas = c;
        xform = new AffineTransform(canvas.getMatrix());
        this.pageSize = pageSize;
        pageRotation = getPage().getRotation() % 360;

        setRenderingHints();

        canvas.translate(0, pageSize.getHeight());
        canvas.scale(1, - 1);

        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStrokeJoin(Paint.Join.MITER);
        paint.setStrokeWidth(1.0f);


        // adjust for non-(0,0) crop box
        canvas.translate(- pageSize.getLowerLeftX(), - pageSize.getLowerLeftY());

        processPage(getPage());

        for (PDAnnotation annotation : getPage().getAnnotations())
        {
            showAnnotation(annotation);
        }

    }

/*
    */

    /**
     * Draws the pattern stream to the requested context.
     *
     * @param color color for this tiling.
     * @throws IOException If there is an IO error while drawing the page.
     *//*

    void drawTilingPattern(Graphics2D g, PDTilingPattern pattern, PDColorSpace colorSpace,
                                  PDColor color, Matrix patternMatrix) throws IOException
    {
        Graphics2D oldGraphics = graphics;
        graphics = g;

        GeneralPath oldLinePath = linePath;
        linePath = new GeneralPath();
        int oldClipWindingRule = clipWindingRule;
        clipWindingRule = -1;

        Area oldLastClip = lastClip;
        lastClip = null;

        boolean oldFlipTG = flipTG;
        flipTG = true;

        setRenderingHints();
        processTilingPattern(pattern, color, colorSpace, patternMatrix);

        flipTG = oldFlipTG;
        graphics = oldGraphics;
        linePath = oldLinePath;
        lastClip = oldLastClip;
        clipWindingRule = oldClipWindingRule;
    }
*/
    private float clampColor( float color )
    {
        return color < 0 ? 0 : ( color > 1 ? 1 : color );
    }

/*
    */

    /**
     * Returns an AWT paint for the given PDColor.
     *
     * @throws IOException
     *//*

    protected Paint getPaint( PDColor color) throws IOException
    {
        PDColorSpace colorSpace = color.getColorSpace();
        if (!(colorSpace instanceof PDPattern))
        {
            float[] rgb = colorSpace.toRGB(color.getComponents());
            return new Color(clampColor(rgb[0]), clampColor(rgb[1]), clampColor(rgb[2]));
        }
        else
        {
            PDPattern patternSpace = (PDPattern)colorSpace;
            PDAbstractPattern pattern = patternSpace.getPattern(color);
            if (pattern instanceof PDTilingPattern)
            {
                PDTilingPattern tilingPattern = (PDTilingPattern) pattern;

                if (tilingPattern.getPaintType() == PDTilingPattern.PAINT_COLORED)
                {
                    // colored tiling pattern
                    return tilingPaintFactory.create(tilingPattern, null, null, xform);
                }
                else
                {
                    // uncolored tiling pattern
                    return tilingPaintFactory.create(tilingPattern, 
                            patternSpace.getUnderlyingColorSpace(), color, xform);
                }
            }
            else
            {
                PDShadingPattern shadingPattern = (PDShadingPattern)pattern;
                PDShading shading = shadingPattern.getShading();
                if (shading == null)
                {
                    LOG.error("shadingPattern is null, will be filled with transparency");
                    return new Color(0,0,0,0);
                }
                return shading.toPaint(Matrix.concatenate(getInitialMatrix(),
                                                          shadingPattern.getMatrix()));

            }
        }
    }
*/

    // sets the clipping path using caching for performance, we track lastClip manually because
    // Graphics2D#getClip() returns a new object instead of the same one passed to setClip
    private void setClip()
    {
        Region clippingPath = getGraphicsState().getCurrentClippingPath();
        if (clippingPath != lastClip)
        {
            canvas.clipPath(clippingPath.getBoundaryPath());
            lastClip = clippingPath;
        }
    }

    @Override
    public void beginText() throws IOException
    {
        setClip();
    }

    @Override
    public void endText() throws IOException
    {
        endTextClip();
    }


    /**
     * End buffering the text clipping path, if any.
     */
    private void endTextClip()
    {
        PDGraphicsState state = getGraphicsState();
        RenderingMode renderingMode = state.getTextState().getRenderingMode();

        // apply the buffered clip as one area
        if (renderingMode.isClip() && ! textClippingArea.isEmpty())
        {
            state.intersectClippingPath(textClippingArea);
            textClippingArea = null;

            // PDFBOX-3681: lastClip needs to be reset, because after intersection it is still the same 
            // object, thus setClip() would believe that it is cached.
            lastClip = null;
        }
    }

    @Override
    protected void showFontGlyph( Matrix textRenderingMatrix, PDFont font, int code, String unicode,
                                  Vector displacement ) throws IOException
    {
        AffineTransform at = textRenderingMatrix.createAffineTransform();
        at.concatenate(font.getFontMatrix().createAffineTransform());

        Glyph2D glyph2D = createGlyph2D(font);
        drawGlyph2D(glyph2D, font, code, displacement, at);
    }

    /**
     * Render the font using the Glyph2D interface.
     *
     * @param glyph2D      the Glyph2D implementation provided a GeneralPath for each glyph
     * @param font         the font
     * @param code         character code
     * @param displacement the glyph's displacement (advance)
     * @param at           the transformation
     * @throws IOException if something went wrong
     */
    private void drawGlyph2D( Glyph2D glyph2D, PDFont font, int code, Vector displacement,
                              AffineTransform at ) throws IOException
    {
        PDGraphicsState state = getGraphicsState();
        RenderingMode renderingMode = state.getTextState().getRenderingMode();

        Path path = glyph2D.getPathForCharacterCode(code);
        if (path != null)
        {
            // stretch non-embedded glyph if it does not match the width contained in the PDF
            if (! font.isEmbedded())
            {
                float fontWidth = font.getWidthFromFont(code);
                if (fontWidth > 0 && // ignore spaces
                        Math.abs(fontWidth - displacement.getX() * 1000) > 0.0001)
                {
                    float pdfWidth = displacement.getX() * 1000;
                    at.scale(pdfWidth / fontWidth, 1);
                }
            }

            // render glyph
//            Shape glyph = at.createTransformedShape(path);
            path.transform(at.toMatrix());

            if (renderingMode.isFill())
            {
//                graphics.setComposite(state.getNonStrokingJavaComposite());
//                graphics.setPaint(getNonStrokingPaint());
                paint.setColor(getNonStrokingColor());
                setClip();
//                graphics.fill(glyph);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPath(path, paint);
//                canvas.clipPath(path);
//                canvas.drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), paint);
            }

            if (renderingMode.isStroke())
            {
//                graphics.setComposite(state.getStrokingJavaComposite());
//                graphics.setPaint(getStrokingPaint());
                paint.setColor(getStrokingColor());
//                graphics.setStroke(getStroke());
                setClip();
//                graphics.draw(glyph);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(path, paint);
//                canvas.clipPath(path);
//                canvas.drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), paint);
            }

            if (renderingMode.isClip())
            {
//                textClippingArea.add(new Area(glyph));
            }
        }
    }

    private int getStrokingColor() throws IOException
    {
        return getColor(getGraphicsState().getStrokingColor());
    }

    private int getNonStrokingColor() throws IOException
    {
        return getColor(getGraphicsState().getNonStrokingColor());
    }


    // returns an integer for color that Android understands from the PDColor
    // TODO: alpha?
    private int getColor( PDColor color ) throws IOException
    {
        PDColorSpace colorSpace = color.getColorSpace();
        float[] floats = colorSpace.toRGB(color.getComponents());
        int r = Math.round(floats[0] * 255);
        int g = Math.round(floats[1] * 255);
        int b = Math.round(floats[2] * 255);
        return Color.rgb(r, g, b);
    }

    /**
     * Provide a Glyph2D for the given font.
     *
     * @param font the font
     * @return the implementation of the Glyph2D interface for the given font
     * @throws IOException if something went wrong
     */
    private Glyph2D createGlyph2D( PDFont font ) throws IOException
    {
        Glyph2D glyph2D = fontGlyph2D.get(font);
        // Is there already a Glyph2D for the given font?
        if (glyph2D != null)
        {
            return glyph2D;
        }

        if (font instanceof PDTrueTypeFont)
        {
            PDTrueTypeFont ttfFont = (PDTrueTypeFont) font;
            glyph2D = new TTFGlyph2D(ttfFont);  // TTF is never null
        } else if (font instanceof PDType1Font)
        {
            PDType1Font pdType1Font = (PDType1Font) font;
            glyph2D = new Type1Glyph2D(pdType1Font); // T1 is never null
        } else if (font instanceof PDType1CFont)
        {
            PDType1CFont type1CFont = (PDType1CFont) font;
            glyph2D = new Type1Glyph2D(type1CFont);
        } else if (font instanceof PDType0Font)
        {
            PDType0Font type0Font = (PDType0Font) font;
            if (type0Font.getDescendantFont() instanceof PDCIDFontType2)
            {
                glyph2D = new TTFGlyph2D(type0Font); // TTF is never null
            } else if (type0Font.getDescendantFont() instanceof PDCIDFontType0)
            {
                // a Type0 CIDFont contains CFF font
                PDCIDFontType0 cidType0Font = (PDCIDFontType0) type0Font.getDescendantFont();
                glyph2D = new CIDType0Glyph2D(cidType0Font); // todo: could be null (need incorporate fallback)
            }
        } else
        {
            throw new IllegalStateException("Bad font type: " + font.getClass().getSimpleName());
        }

        // cache the Glyph2D instance
        if (glyph2D != null)
        {
            fontGlyph2D.put(font, glyph2D);
        }

        if (glyph2D == null)
        {
            // todo: make sure this never happens
            throw new UnsupportedOperationException("No font for " + font.getName());
        }

        return glyph2D;
    }

    @Override
    public void appendRectangle( PointF p0, PointF p1, PointF p2, PointF p3 )
    {
        // to ensure that the path is created in the right direction, we have to create
        // it by combining single lines instead of creating a simple rectangle
        linePath.moveTo((float) p0.x, (float) p0.y);
        linePath.lineTo((float) p1.x, (float) p1.y);
        linePath.lineTo((float) p2.x, (float) p2.y);
        linePath.lineTo((float) p3.x, (float) p3.y);

        // close the subpath instead of adding the last line so that a possible set line
        // cap style isn't taken into account at the "beginning" of the rectangle
        linePath.close();
    }

/*
    //TODO: move soft mask apply to getPaint()?
    private Paint applySoftMaskToPaint(Paint parentPaint, PDSoftMask softMask) throws IOException
    {
        if (softMask == null || softMask.getGroup() == null)
        {
            return parentPaint;
        }
        PDColor backdropColor = null;
        if (COSName.LUMINOSITY.equals(softMask.getSubType()))
        {
            COSArray backdropColorArray = softMask.getBackdropColor();
            PDColorSpace colorSpace = softMask.getGroup().getGroup().getColorSpace();
            if (colorSpace != null && backdropColorArray != null)
            {
                backdropColor = new PDColor(backdropColorArray, colorSpace);
            }
        }
        TransparencyGroup transparencyGroup = new TransparencyGroup(softMask.getGroup(), true, 
                softMask.getInitialTransformationMatrix(), backdropColor);
        BufferedImage image = transparencyGroup.getImage();
        if (image == null)
        {
            // Adobe Reader ignores empty softmasks instead of using bc color
            // sample file: PDFJS-6967_reduced_outside_softmask.pdf
            return parentPaint;
        }
        BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        if (COSName.ALPHA.equals(softMask.getSubType()))
        {
            gray.setData(image.getAlphaRaster());
        }
        else if (COSName.LUMINOSITY.equals(softMask.getSubType()))
        {
            Graphics g = gray.getGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
        }
        else
        {
            throw new IOException("Invalid soft mask subtype.");
        }
        gray = getRotatedImage(gray);
        Rectangle2D tpgBounds = transparencyGroup.getBounds();
        adjustRectangle(tpgBounds);
        return new SoftMask(parentPaint, gray, tpgBounds, backdropColor, softMask.getTransferFunction());
    }
*/


    // set stroke based on the current CTM and the current stroke
    private void setStroke()
    {
        PDGraphicsState state = getGraphicsState();

        // apply the CTM
        float lineWidth = transformWidth(state.getLineWidth());

        // minimum line width as used by Adobe Reader
        if (lineWidth < 0.25)
        {
            lineWidth = 0.25f;
        }

//        PDLineDashPattern dashPattern = state.getLineDashPattern();
//        int phaseStart = dashPattern.getPhase();
//        float[] dashArray = dashPattern.getDashArray();
//        if (dashArray != null)
//        {
//        	// apply the CTM
//        	for (int i = 0; i < dashArray.length; ++i)
//        	{
//        		// minimum line dash width avoids JVM crash, see PDFBOX-2373
//        		dashArray[i] = Math.max(transformWidth(dashArray[i]), 0.016f);
//        	}
//        	phaseStart = (int)transformWidth(phaseStart);
//
//        	// empty dash array is illegal
//        	if (dashArray.length == 0)
//        	{
//        		dashArray = null;
//        	}
//        }
//        return new BasicStroke(lineWidth, state.getLineCap(), state.getLineJoin(),
//        		state.getMiterLimit(), dashArray, phaseStart);

        paint.setStrokeWidth(lineWidth);
        paint.setStrokeCap(state.getLineCap());
        paint.setStrokeJoin(state.getLineJoin());
    }

    @Override
    public void strokePath() throws IOException
    {
//        graphics.setComposite(getGraphicsState().getStrokingJavaComposite());

        setStroke();
        setClip();
        paint.setARGB(255, 0, 0, 0); // TODO set the correct color from graphics state.
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getStrokingColor());
        setClip();
        canvas.drawPath(linePath, paint);
        linePath.reset();
    }

    @Override
    public void fillPath( Path.FillType windingRule ) throws IOException
    {
//        graphics.setComposite(getGraphicsState().getNonStrokingJavaComposite());
        paint.setColor(getNonStrokingColor());
        setClip();
        linePath.setFillType(windingRule);

        // disable anti-aliasing for rectangular paths, this is a workaround to avoid small stripes
        // which occur when solid fills are used to simulate piecewise gradients, see PDFBOX-2302
//        boolean isRectangular = isRectangular(linePath);
//        if (isRectangular)
        {
//            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                                      RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(linePath, paint);
        linePath.reset();

//        if (isRectangular)
        {
            // JDK 1.7 has a bug where rendering hints are reset by the above call to
            // the setRenderingHint method, so we re-set all hints, see PDFBOX-2302
            setRenderingHints();
        }
    }


    /**
     * Fills and then strokes the path.
     *
     * @param windingRule The winding rule this path will use.
     * @throws IOException If there is an IO error while filling the path.
     */
    @Override
    public void fillAndStrokePath( Path.FillType windingRule ) throws IOException
    {
        // TODO can we avoid cloning the path?
        Path path = new Path(linePath);
        fillPath(windingRule);
        linePath = path;
        strokePath();
    }

    @Override
    public void clip( Path.FillType windingRule )
    {
        // the clipping path will not be updated until the succeeding painting operator is called
        clipWindingRule = windingRule;
    }

    @Override
    public void moveTo( float x, float y )
    {
        linePath.moveTo(x, y);
    }

    @Override
    public void lineTo( float x, float y )
    {
        linePath.lineTo(x, y);
    }

    @Override
    public void curveTo( float x1, float y1, float x2, float y2, float x3, float y3 )
    {
        linePath.cubicTo(x1, y1, x2, y2, x3, y3); // TODO: check if this should be relative
    }

    @Override
    public PointF getCurrentPoint()
    {
        return new PointF();
    }

    @Override
    public void closePath()
    {
        linePath.close();
    }

    @Override
    public void endPath()
    {
        linePath.reset();
    }

    @Override
    public void drawImage( PDImage pdImage ) throws IOException
    {
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        AffineTransform at = ctm.createAffineTransform();

        if (! pdImage.getInterpolate())
        {
            boolean isScaledUp = pdImage.getWidth() < Math.round(at.getScaleX()) ||
                    pdImage.getHeight() < Math.round(at.getScaleY());

            // if the image is scaled down, we use smooth interpolation, eg PDFBOX-2364
            // only when scaled up do we use nearest neighbour, eg PDFBOX-2302 / mori-cvpr01.pdf
            // stencils are excluded from this rule (see survey.pdf)
            if (isScaledUp || pdImage.isStencil())
            {
//                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
//                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            }
        }

        if (pdImage.isStencil())
        {
            if (getGraphicsState().getNonStrokingColor().getColorSpace() instanceof PDPattern)
            {
                // The earlier code for stencils (see "else") doesn't work with patterns because the
                // CTM is not taken into consideration.
                // this code is based on the fact that it is easily possible to draw the mask and 
                // the paint at the correct place with the existing code, but not in one step.
                // Thus what we do is to draw both in separate images, then combine the two and draw
                // the result. 
                // Note that the device scale is not used. In theory, some patterns can get better
                // at higher resolutions but the stencil would become more and more "blocky".
                // If anybody wants to do this, have a look at the code in showTransparencyGroup().


                // draw the image
                setClip();
            } else
            {
                // fill the image with stenciled paint

                // draw the image
                drawBufferedImage(pdImage.getImage(), at);
            }
        } else
        {
            // draw the image
            drawBufferedImage(pdImage.getImage(), at);
        }

        if (! pdImage.getInterpolate())
        {
            // JDK 1.7 has a bug where rendering hints are reset by the above call to
            // the setRenderingHint method, so we re-set all hints, see PDFBOX-2302
            setRenderingHints();
        }
    }

    private void drawBufferedImage( Bitmap image, AffineTransform at ) throws IOException
    {
        setClip();
        PDSoftMask softMask = getGraphicsState().getSoftMask();
        if (softMask != null)
        {
            AffineTransform imageTransform = new AffineTransform(at);
            imageTransform.scale(1, - 1);
            imageTransform.translate(0, - 1);
//            Paint awtPaint = new TexturePaint(image,
//                    new Rectangle2D.Double(imageTransform.getTranslateX(), imageTransform.getTranslateY(),
//                            imageTransform.getScaleX(), imageTransform.getScaleY()));
//            awtPaint = applySoftMaskToPaint(awtPaint, softMask);
//            graphics.setPaint(awtPaint);
            RectF unitRect = new RectF(0, 0, 1, 1);
//            graphics.fill(at.createTransformedShape(unitRect));
        } else
        {
            int width = image.getWidth();
            int height = image.getHeight();
            AffineTransform imageTransform = new AffineTransform(at);
            imageTransform.scale(( 1.0f / width ), ( - 1.0f / height ));
            imageTransform.translate(0, - height);
            canvas.drawBitmap(image, imageTransform.toMatrix(), paint);
        }
    }

    @Override
    public void shadingFill( COSName shadingName ) throws IOException
    {
        PDShading shading = getResources().getShading(shadingName);
        if (shading == null)
        {
            LOG.error("shading " + shadingName + " does not exist in resources dictionary");
            return;
        }
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
    }

    @Override
    public void showAnnotation( PDAnnotation annotation ) throws IOException
    {
//        lastClip = null;
        //TODO support more annotation flags (Invisible, NoZoom, NoRotate)
//    	int deviceType = graphics.getDeviceConfiguration().getDevice().getType();
//    	if (deviceType == GraphicsDevice.TYPE_PRINTER && !annotation.isPrinted())
//    	{
//    		return;
//    	} Shouldn't be needed
        if (/*deviceType == GraphicsDevice.TYPE_RASTER_SCREEN && */annotation.isNoView())
        {
            return;
        }
        if (annotation.isHidden())
        {
            return;
        }
        super.showAnnotation(annotation);
    }

    private static class AnnotationBorder
    {
        private float[] dashArray = null;
        private boolean underline = false;
        private float width = 0;
        private PDColor color;
    }

    // return border info. BorderStyle must be provided as parameter because
    // method is not available in the base class
    private AnnotationBorder getAnnotationBorder( PDAnnotation annotation,
                                                  PDBorderStyleDictionary borderStyle )
    {
        AnnotationBorder ab = new AnnotationBorder();
        COSArray border = annotation.getBorder();
        if (borderStyle == null)
        {
            if (border.getObject(2) instanceof COSNumber)
            {
                ab.width = ( (COSNumber) border.getObject(2) ).floatValue();
            }
            if (border.size() > 3)
            {
                COSBase base3 = border.getObject(3);
                if (base3 instanceof COSArray)
                {
                    ab.dashArray = ( (COSArray) base3 ).toFloatArray();
                }
            }
        } else
        {
            ab.width = borderStyle.getWidth();
            if (borderStyle.getStyle().equals(PDBorderStyleDictionary.STYLE_DASHED))
            {
                ab.dashArray = borderStyle.getDashStyle().getDashArray();
            }
            if (borderStyle.getStyle().equals(PDBorderStyleDictionary.STYLE_UNDERLINE))
            {
                ab.underline = true;
            }
        }
        ab.color = annotation.getColor();
        if (ab.color == null)
        {
            // spec is unclear, but black seems to be the right thing to do
            ab.color = new PDColor(new float[] { 0 }, PDDeviceGray.INSTANCE);
        }
        if (ab.dashArray != null)
        {
            boolean allZero = true;
            for (float f : ab.dashArray)
            {
                if (f != 0)
                {
                    allZero = false;
                    break;
                }
            }
            if (allZero)
            {
                ab.dashArray = null;
            }
        }
        return ab;
    }

    @Override
    public void showTransparencyGroup( PDTransparencyGroup form ) throws IOException
    {
        TransparencyGroup group = new TransparencyGroup(form, false);

//        graphics.setComposite(getGraphicsState().getNonStrokingJavaComposite());
        setClip();

        // both the DPI xform and the CTM were already applied to the group, so all we do
        // here is draw it directly onto the Graphics2D device at the appropriate position
//        PDRectangle bbox = group.getBBox();
//        AffineTransform prev = graphics.getTransform();
//        float x = bbox.getLowerLeftX();
//        float y = pageSize.getHeight() - bbox.getLowerLeftY() - bbox.getHeight();
//        graphics.setTransform(AffineTransform.getTranslateInstance(x * xform.getScaleX(),
//                                                                   y * xform.getScaleY()));

        PDSoftMask softMask = getGraphicsState().getSoftMask();
        if (softMask != null)
        {
//            Bitmap image = group.getImage();
//            Paint awtPaint = new TexturePaint(image,
//                    new Rectangle2D.Float(0, 0, image.getWidth(), image.getHeight()));
//            awtPaint = applySoftMaskToPaint(awtPaint, softMask); // todo: PDFBOX-994 problem here?
//            graphics.setPaint(awtPaint);
//            graphics.fill(new Rectangle2D.Float(0, 0, bbox.getWidth() * (float)xform.getScaleX(),
//                                                bbox.getHeight() * (float)xform.getScaleY()));
        } else
        {
//            graphics.drawImage(group.getImage(), null, null);
        }

//        graphics.setTransform(prev);
    }

    /**
     * Transparency group.
     **/
    private final class TransparencyGroup
    {
//        private final Bitmap image;
//        private final PDRectangle bbox;

//        private final int minX;
//        private final int minY;
//        private final int width;
//        private final int height;

        /**
         * Creates a buffered image for a transparency group result.
         */
        private TransparencyGroup( PDFormXObject form, boolean isSoftMask) throws IOException
        {
//            Graphics2D g2dOriginal = graphics;
//            Area lastClipOriginal = lastClip;

            // get the CTM x Form Matrix transform
//            Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
//            Matrix transform = Matrix.concatenate(ctm, form.getMatrix());

            // transform the bbox
//            Path transformedBox = form.getBBox().transform(transform);

            // clip the bbox to prevent giant bboxes from consuming all memory
//            Area clip = (Area)getGraphicsState().getCurrentClippingPath().clone();
//            clip.intersect(new Area(transformedBox));
//            Rectangle2D clipRect = clip.getBounds2D();
//            this.bbox = new PDRectangle((float)clipRect.getX(), (float)clipRect.getY(),
//                                        (float)clipRect.getWidth(), (float)clipRect.getHeight());

            // apply the underlying Graphics2D device's DPI transform
//            Shape deviceClip = xform.createTransformedShape(clip);
//            Rectangle2D bounds = deviceClip.getBounds2D();

//            minX = (int) Math.floor(bounds.getMinX());
//            minY = (int) Math.floor(bounds.getMinY());
//            int maxX = (int) Math.floor(bounds.getMaxX()) + 1;
//            int maxY = (int) Math.floor(bounds.getMaxY()) + 1;

//            width = maxX - minX;
//            height = maxY - minY;

//            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); // FIXME - color space
//            Graphics2D g = image.createGraphics();

            // flip y-axis
//            g.translate(0, height);
//            g.scale(1, -1);

            // apply device transform (DPI)
//            g.transform(xform);

            // adjust the origin
//            g.translate(-clipRect.getX(), -clipRect.getY());

//            graphics = g;
            try
            {
                if (isSoftMask)
                {
//                    processSoftMask(form);
                }
                else
                {
                    processTransparencyGroup(form);
                }
            }
            finally
            {
//                lastClip = lastClipOriginal;
//                graphics.dispose();
//                graphics = g2dOriginal;
            }
        }

//        public Bitmap getImage()
//        {
//            return image;
//        }

//        public PDRectangle getBBox()
//        {
//            return bbox;
//        }

//        public Raster getAlphaRaster()
//        {
//            return image.getAlphaRaster();
//        }

//        public Raster getLuminosityRaster()
//        {
//            BufferedImage gray = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
//            Graphics g = gray.getGraphics();
//            g.drawImage(image, 0, 0, null);
//            g.dispose();
//
//            return gray.getRaster();
//        }
    }
}