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

import android.util.Log;

import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.cff.CFFFont;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.cff.CFFParser;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.NamingTable;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.TTFParser;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.TrueTypeFont;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.type1.Type1Font;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.util.autodetect.FontFileFinder;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A FontProvider which searches for fonts on the local filesystem.
 *
 * @author John Hewson
 */
final class FileSystemFontProvider extends FontProvider
{
    // cache of font files on the system (populated in constructor)
    private final Map<String, File> ttfFontFiles = new HashMap<String, File>();
    private final Map<String, File> cffFontFiles = new HashMap<String, File>();
    private final Map<String, File> type1FontFiles =  new HashMap<String, File>();

    // cache of loaded fonts which are in use (populated on-the-fly)
    private final Map<String, TrueTypeFont> ttfFonts = new HashMap<String, TrueTypeFont>();
    private final Map<String, CFFFont> cffFonts = new HashMap<String, CFFFont>();
    private final Map<String, Type1Font> type1Fonts = new HashMap<String, Type1Font>();

    /**
     * Constructor.
     */
    FileSystemFontProvider()
    {
        Log.v("PdfBoxAndroid", "Will search the local system for fonts");

        int count = 0;
        FontFileFinder fontFileFinder = new FontFileFinder();
        List<URI> fonts = fontFileFinder.find();
        for (URI font : fonts)
        {
            count++;
            File fontFile = new File(font);
            try
            {
                if (fontFile.getPath().toLowerCase().endsWith(".ttf") ||
                        fontFile.getPath().toLowerCase().endsWith(".otf"))
                {
                    addOpenTypeFont(fontFile);
                }
                else if (fontFile.getPath().toLowerCase().endsWith(".pfb"))
                {
                    addType1Font(fontFile);
                }
            }
            catch (IOException e)
            {
                Log.e("PdfBoxAndroid", "Error parsing font " + fontFile.getPath(), e);
            }
        }

        Log.v("PdfBoxAndroid", "Found " + count + " fonts on the local system");
    }

    /**
     * Adds an OTF or TTF font to the file cache. To reduce memory, the parsed font is not cached.
     */
    private void addOpenTypeFont(File otfFile) throws IOException
    {
        TTFParser ttfParser = new TTFParser(false, true);
        TrueTypeFont ttf = null;
        try
        {
            ttf = ttfParser.parse(otfFile);
        }
        catch (NullPointerException e) // TTF parser is buggy
        {
            Log.e("PdfBoxAndroid", "Could not load font file: " + otfFile, e);
        }
        catch (IOException e)
        {
            Log.e("PdfBoxAndroid", "Could not load font file: " + otfFile, e);
        }

        try
        {
            // check for 'name' table
            NamingTable nameTable = null;

            // ttf could still be null
            if (ttf != null)
            {
                nameTable = ttf.getNaming();
            }
            if (nameTable == null)
            {
                Log.w("PdfBoxAndroid", "Missing 'name' table in font " + otfFile);
            }
            else
            {
                // read PostScript name, if any
                if (nameTable.getPostScriptName() != null)
                {
                    String psName = nameTable.getPostScriptName();

                    String format;
                    if (ttf.getTableMap().get("CFF ") != null)
                    {
                        format = "OTF";
                        cffFontFiles.putAll(toMap(getNames(ttf), otfFile));
                    }
                    else
                    {
                        format = "TTF";
                        ttfFontFiles.putAll(toMap(getNames(ttf), otfFile));
                    }

                    Log.v("PdfBoxAndroid", format +": '" + psName + "' / '" + nameTable.getFontFamily() +
                            "' / '" + nameTable.getFontSubFamily() + "'");
                }
                else
                {
                    Log.w("PdfBoxAndroid", "Missing 'name' entry for PostScript name in font " + otfFile);
                }
            }
        }
        finally
        {
            if (ttf != null)
            {
                ttf.close();
            }
        }
    }

    /**
     * Adds a Type 1 font to the file cache. To reduce memory, the parsed font is not cached.
     */
    private void addType1Font(File pfbFile) throws IOException
    {
        InputStream input = new FileInputStream(pfbFile);
        try
        {
            Type1Font type1 = Type1Font.createWithPFB(input);

            String psName = type1.getFontName();
            type1FontFiles.putAll(toMap(getNames(type1), pfbFile));

            Log.v("PdfBoxAndroid", "PFB: '" + psName + "' / '" + type1.getFamilyName() + "' / '" +
                    type1.getWeight() + "'");
        }
        finally
        {
            input.close();
        }
    }


    /**
     * Returns a map containing the given file for each string key.
     */
    private Map<String, File> toMap(Set<String> names, File file)
    {
        Map<String, File> map = new HashMap<String, File>();
        for (String name : names)
        {
            map.put(name, file);
        }
        return map;
    }

    @Override
    public synchronized TrueTypeFont getTrueTypeFont( String postScriptName )
    {
        TrueTypeFont ttf = ttfFonts.get(postScriptName);
        if (ttf != null)
        {
            return ttf;
        }

        File file = ttfFontFiles.get(postScriptName);
        if (file != null)
        {
            TTFParser ttfParser = new TTFParser(false, true);
            try
            {
                ttf = ttfParser.parse(file);

                for (String name : getNames(ttf))
                {
                    ttfFonts.put(name, ttf);
                }
                Log.d("PdfBoxAndroid", "Loaded " + postScriptName + " from " + file);
                return ttf;
            }
            catch (NullPointerException e) // TTF parser is buggy
            {
                Log.e("PdfBoxAndroid", "Could not load font file: " + file, e);
            }
            catch (IOException e)
            {
                Log.e("PdfBoxAndroid", "Could not load font file: " + file, e);
            }
        }
        return null;
    }

    @Override
    public synchronized CFFFont getCFFFont( String postScriptName )
    {
        CFFFont cff = cffFonts.get(postScriptName);
        if (cff != null)
        {
            return cff;
        }

        File file = cffFontFiles.get(postScriptName);
        if (file != null)
        {
            InputStream input = null;
            try
            {
                input = new FileInputStream(file);
                byte[] bytes = IOUtils.toByteArray(input);
                CFFParser cffParser = new CFFParser();
                cff = cffParser.parse(bytes).get(0);
                for (String name : getNames(cff))
                {
                    cffFonts.put(name, cff);
                }
                Log.d("PdfBoxAndroid", "Loaded " + postScriptName + " from " + file);
                return cff;
            }
            catch (IOException e)
            {
                Log.e("PdfBoxAndroid", "Could not load font file: " + file, e);
            }
            finally
            {
                IOUtils.closeQuietly(input);
            }
        }
        return null;
    }

    @Override
    public synchronized Type1Font getType1Font( String postScriptName )
    {
        Type1Font type1 = type1Fonts.get(postScriptName);
        if (type1 != null)
        {
            return type1;
        }

        File file = type1FontFiles.get(postScriptName);
        if (file != null)
        {
            InputStream input = null;
            try
            {
                input = new FileInputStream(file);
                type1 = Type1Font.createWithPFB(input);
                for (String name : getNames(type1))
                {
                    type1Fonts.put(name, type1);
                }
                Log.d("PdfBoxAndroid", "Loaded " + postScriptName + " from " + file);
                return type1;
            }
            catch (IOException e)
            {
                Log.e("PdfBoxAndroid", "Could not load font file: " + file, e);
            }
            finally
            {
                IOUtils.closeQuietly(input);
            }
        }
        return null;
    }

    @Override
    public String toDebugString()
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, File> entry : ttfFontFiles.entrySet())
        {
            sb.append("TTF: ");
            sb.append(entry.getKey());
            sb.append(": ");
            sb.append(entry.getValue().getPath());
            sb.append('\n');
        }
        for (Map.Entry<String, File> entry : cffFontFiles.entrySet())
        {
            sb.append("OTF: ");
            sb.append(entry.getKey());
            sb.append(": ");
            sb.append(entry.getValue().getPath());
            sb.append('\n');
        }
        for (Map.Entry<String, File> entry : type1FontFiles.entrySet())
        {
            sb.append("PFB: ");
            sb.append(entry.getKey());
            sb.append(": ");
            sb.append(entry.getValue().getPath());
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public List<? extends FontInfo> getFontInfo()
    {
        return null;
    }
}
