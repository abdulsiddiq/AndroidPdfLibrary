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
package pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.color;

import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.MissingOperandException;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.Operator;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.OperatorProcessor;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSArray;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSBase;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSNumber;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.color.PDColor;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.color.PDColorSpace;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.graphics.color.PDPattern;

import java.io.IOException;
import java.util.List;

/**
 * sc,scn,SC,SCN: Sets the color to use for stroking or non-stroking operations.
 *
 * @author John Hewson
 */
public abstract class SetColor extends OperatorProcessor
{
    @Override
    public void process(Operator operator, List<COSBase> arguments) throws IOException
    {
        PDColorSpace colorSpace = getColorSpace();
        if (!(colorSpace instanceof PDPattern))
        {
            if (arguments.size() < colorSpace.getNumberOfComponents())
            {
                throw new MissingOperandException(operator, arguments);
            }
            if (!checkArrayTypesClass(arguments, COSNumber.class))
            {
                return;
            }
        }
        COSArray array = new COSArray();
        array.addAll(arguments);
        setColor(new PDColor(array, colorSpace));
    }

    /**
     * Returns either the stroking or non-stroking color value.
     * @return The stroking or non-stroking color value.
     */
    protected abstract PDColor getColor();

    /**
     * Sets either the stroking or non-stroking color value.
     * @param color The stroking or non-stroking color value.
     */
    protected abstract void setColor(PDColor color);

    /**
     * Returns either the stroking or non-stroking color space.
     * @return The stroking or non-stroking color space.
     */
    protected abstract PDColorSpace getColorSpace();
}
