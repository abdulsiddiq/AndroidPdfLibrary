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
package pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf;

import android.graphics.Path;

import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.encoding.Encoding;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.util.BoundingBox;

import java.io.IOException;

/**
 * A Type 1-equivalent font, i.e. a font which can access glyphs by their PostScript name.
 * This is currently a minimal interface and could be expanded if needed.
 *
 * @author John Hewson
 */
public interface Type1Equivalent
{
    /**
     * The PostScript name of the font.
     */
    String getName() throws IOException;

    /**
     * Returns the Type 1 CharString for the character with the given name.
     *
     * @return glyph path
     * @throws IOException if the path could not be read
     */
    Path getPath( String name ) throws IOException;

    /**
     * Returns the advance width for the character with the given name.
     *
     * @return glyph advance width
     * @throws IOException if the path could not be read
     */
    float getWidth( String name ) throws IOException;

    /**
     * Returns true if the font contains the given glyph.
     * @param name PostScript glyph name
     */
    boolean hasGlyph( String name ) throws IOException;

    /**
     * Returns the PostScript Encoding vector for the font.
     */
    Encoding getEncoding() throws IOException;

    /**
     * Returns the font's bounding box in PostScript units.
     */
    abstract BoundingBox getFontBBox() throws IOException;
}
