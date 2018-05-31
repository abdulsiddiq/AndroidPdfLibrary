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
package pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.state;

import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.LogFactory;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.LogFactory.Log;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.MissingOperandException;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.Operator;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.contentstream.operator.OperatorProcessor;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSArray;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSBase;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.cos.COSNumber;

import java.util.List;

/**
 * d: Set the line dash pattern.
 *
 * @author Ben Litchfield
 */
public class SetLineDashPattern extends OperatorProcessor
{
    private static final Log LOG = LogFactory.getLog(SetLineDashPattern.class);
    
    @Override
    public void process(Operator operator, List<COSBase> arguments) throws MissingOperandException
    {
        if (arguments.size() < 2)
        {
            throw new MissingOperandException(operator, arguments);
        }
        COSBase base0 = arguments.get(0);
        if (!(base0 instanceof COSArray))
        {
            return;
        }
        COSBase base1 = arguments.get(1);
        if (!(base1 instanceof COSNumber))
        {
            return;
        }
        COSArray dashArray = (COSArray) base0;
        int dashPhase = ((COSNumber) base1).intValue();
        
        boolean allZero = true;
        for (COSBase base : dashArray)
        {
            if (base instanceof COSNumber)
            {
                COSNumber num = (COSNumber) base;
                if (num.floatValue() != 0)
                {
                    allZero = false;
                    break;
                }
            }
            else
            {
                LOG.warn("dash array has non number element " + base + ", ignored");
                dashArray = new COSArray();
                break;
            }
        }
        if (dashArray.size() > 0 && allZero)
        {
            LOG.warn("dash lengths all zero, ignored");
            dashArray = new COSArray();
        }
        context.setLineDashPattern(dashArray, dashPhase);
    }

    @Override
    public String getName()
    {
        return "d";
    }
}
