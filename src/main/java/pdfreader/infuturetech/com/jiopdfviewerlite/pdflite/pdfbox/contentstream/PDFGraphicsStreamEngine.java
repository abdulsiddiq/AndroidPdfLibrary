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
package pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream;

import android.graphics.Path;
import android.graphics.PointF;

import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.color.SetNonStrokingColor;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.color.SetNonStrokingColorN;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.color.SetNonStrokingColorSpace;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.color.SetNonStrokingDeviceGrayColor;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.color.SetNonStrokingDeviceRGBColor;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.color.SetStrokingColor;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.color.SetStrokingColorN;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.color.SetStrokingColorSpace;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.color.SetStrokingDeviceGrayColor;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.color.SetStrokingDeviceRGBColor;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.AppendRectangleToPath;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.ClipEvenOddRule;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.ClipNonZeroRule;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.CloseAndStrokePath;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.CloseFillEvenOddAndStrokePath;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.CloseFillNonZeroAndStrokePath;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.ClosePath;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.CurveTo;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.CurveToReplicateFinalPoint;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.CurveToReplicateInitialPoint;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.DrawObject;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.EndPath;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.FillEvenOddAndStrokePath;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.FillEvenOddRule;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.FillNonZeroAndStrokePath;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.FillNonZeroRule;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.LegacyFillNonZeroRule;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.LineTo;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.MoveTo;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.ShadingFill;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.graphics.StrokePath;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.state.Concatenate;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.state.Restore;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.state.Save;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.state.SetFlatness;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.state.SetLineCapStyle;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.state.SetLineDashPattern;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.state.SetLineJoinStyle;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.state.SetLineMiterLimit;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.state.SetLineWidth;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.state.SetMatrix;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.state.SetRenderingIntent;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.BeginText;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.EndText;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.MoveText;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.MoveTextSetLeading;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.NextLine;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.SetCharSpacing;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.SetFontAndSize;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.SetTextHorizontalScaling;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.SetTextLeading;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.SetTextRenderingMode;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.SetTextRise;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.SetWordSpacing;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.ShowText;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.ShowTextAdjusted;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.ShowTextLine;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.text.ShowTextLineAndSpace;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSName;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.PDPage;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.image.PDImage;

import java.io.IOException;

/**
 * PDFStreamEngine subclass for advanced processing of graphics.
 * This class should be subclasses by end users looking to hook into graphics operations.
 *
 * @author John Hewson
 */
public abstract class PDFGraphicsStreamEngine extends PDFStreamEngine
{
    // may be null, for example if the stream is a tiling pattern
    private final PDPage page;

    /**
     * Constructor.
     */
    protected PDFGraphicsStreamEngine(PDPage page)
    {
        this.page = page;

        addOperator(new CloseFillNonZeroAndStrokePath());
        addOperator(new FillNonZeroAndStrokePath());
        addOperator(new CloseFillEvenOddAndStrokePath());
        addOperator(new FillEvenOddAndStrokePath());
//        addOperator(new BeginInlineImage());
        addOperator(new BeginText());
        addOperator(new CurveTo());
        addOperator(new Concatenate());
        addOperator(new SetStrokingColorSpace());
        addOperator(new SetNonStrokingColorSpace());
        addOperator(new SetLineDashPattern());
        addOperator(new DrawObject()); // special graphics version
        addOperator(new EndText());
        addOperator(new FillNonZeroRule());
        addOperator(new LegacyFillNonZeroRule());
        addOperator(new FillEvenOddRule());
        addOperator(new SetStrokingDeviceGrayColor());
        addOperator(new SetNonStrokingDeviceGrayColor());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new ClosePath());
        addOperator(new SetFlatness());
        addOperator(new SetLineJoinStyle());
        addOperator(new SetLineCapStyle());
//        addOperator(new SetStrokingDeviceCMYKColor());
//        addOperator(new SetNonStrokingDeviceCMYKColor());
        addOperator(new LineTo());
        addOperator(new MoveTo());
        addOperator(new SetLineMiterLimit());
        addOperator(new EndPath());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new AppendRectangleToPath());
        addOperator(new SetStrokingDeviceRGBColor());
        addOperator(new SetNonStrokingDeviceRGBColor());
        addOperator(new SetRenderingIntent());
        addOperator(new CloseAndStrokePath());
        addOperator(new StrokePath());
        addOperator(new SetStrokingColor());
        addOperator(new SetNonStrokingColor());
        addOperator(new SetStrokingColorN());
        addOperator(new SetNonStrokingColorN());
        addOperator(new ShadingFill());
        addOperator(new NextLine());
        addOperator(new SetCharSpacing());
        addOperator(new MoveText());
        addOperator(new MoveTextSetLeading());
        addOperator(new SetFontAndSize());
        addOperator(new ShowText());
        addOperator(new ShowTextAdjusted());
        addOperator(new SetTextLeading());
        addOperator(new SetMatrix());
        addOperator(new SetTextRenderingMode());
        addOperator(new SetTextRise());
        addOperator(new SetWordSpacing());
        addOperator(new SetTextHorizontalScaling());
        addOperator(new CurveToReplicateInitialPoint());
        addOperator(new SetLineWidth());
        addOperator(new ClipNonZeroRule());
        addOperator(new ClipEvenOddRule());
        addOperator(new CurveToReplicateFinalPoint());
        addOperator(new ShowTextLine());
        addOperator(new ShowTextLineAndSpace());
    }

    /**
     * Returns the page.
     */
    protected final PDPage getPage()
    {
        return page;
    }

    /**
     * Append a rectangle to the current path.
     */
    public abstract void appendRectangle(PointF p0, PointF p1,
                                         PointF p2, PointF p3) throws IOException;

    /**
     * Draw the image.
     *
     * @param pdImage The image to draw.
     */
    public abstract void drawImage(PDImage pdImage) throws IOException;

    /**
     * Modify the current clipping path by intersecting it with the current path.
     * The clipping path will not be updated until the succeeding painting operator is called.
     *
     * @param windingRule The winding rule which will be used for clipping.
     */
    public abstract void clip(Path.FillType windingRule) throws IOException;

    /**
     * Starts a new path at (x,y).
     */
    public abstract void moveTo(float x, float y) throws IOException;

    /**
     * Draws a line from the current point to (x,y).
     */
    public abstract void lineTo(float x, float y) throws IOException;

    /**
     * Draws a curve from the current point to (x3,y3) using (x1,y1) and (x2,y2) as control points.
     */
    public abstract void curveTo(float x1, float y1,
                                 float x2, float y2,
                                 float x3, float y3) throws IOException;

    /**
     * Returns the current point of the current path.
     */
    public abstract PointF getCurrentPoint() throws IOException;

    /**
     * Closes the current path.
     */
    public abstract void closePath() throws IOException;

    /**
     * Ends the current path without filling or stroking it. The clipping path is updated here.
     */
    public abstract void endPath() throws IOException;

    /**
     * Stroke the path.
     *
     * @throws IOException If there is an IO error while stroking the path.
     */
    public abstract void strokePath() throws IOException;

    /**
     * Fill the path.
     *
     * @param windingRule The winding rule this path will use.
     */
    public abstract void fillPath(Path.FillType windingRule) throws IOException;

    /**
     * Fills and then strokes the path.
     *
     * @param windingRule The winding rule this path will use.
     */
    public abstract void fillAndStrokePath(Path.FillType windingRule) throws IOException;

    /**
     * Fill with Shading.
     *
     * @param shadingName The name of the Shading Dictionary to use for this fill instruction.
     */
    public abstract void shadingFill(COSName shadingName) throws IOException;
}
