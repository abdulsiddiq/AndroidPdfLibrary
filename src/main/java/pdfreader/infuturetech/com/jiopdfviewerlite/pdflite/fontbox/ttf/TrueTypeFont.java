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
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.awt.AffineTransform;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A TrueType font file.
 * 
 * @author Ben Litchfield
 */
public class TrueTypeFont implements Type1Equivalent
{
	private float version;
	private int numberOfGlyphs = -1;
	private int unitsPerEm = -1;
	protected Map<String, pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.TTFTable> tables = new HashMap<String, pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.TTFTable>();
	private pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.TTFDataStream data;
	private Map<String, Integer> postScriptNames;

	/**
	 * Constructor.  Clients should use the TTFParser to create a new TrueTypeFont object.
	 * 
	 * @param fontData The font data.
	 */
	TrueTypeFont( pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.TTFDataStream fontData)
	{
		data = fontData;
	}

	/**
	 * Close the underlying resources.
	 * 
	 * @throws IOException If there is an error closing the resources.
	 */
	public void close() throws IOException
	{
		data.close();
	}

	/**
	 * @return Returns the version.
	 */
	public float getVersion() 
	{
		return version;
	}

	/**
	 * Set the version. Package-private, used by TTFParser only.
	 * @param versionValue The version to set.
	 */
	void setVersion(float versionValue)
	{
		version = versionValue;
	}

	/**
	 * Add a table definition. Package-private, used by TTFParser only.
	 * 
	 * @param table The table to add.
	 */
	void addTable( pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.TTFTable table )
	{
		tables.put( table.getTag(), table );
	}

	/**
	 * Get all of the tables.
	 * 
	 * @return All of the tables.
	 */
	public Collection<pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.TTFTable> getTables()
	{
		return tables.values();
	}

	/**
	 * Get all of the tables.
	 *
	 * @return All of the tables.
	 */
	public Map<String, pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.TTFTable> getTableMap()
	{
		return tables;
	}

	/**
	 * Returns the war bytes of the given table.
	 */
	public synchronized byte[] getTableBytes( pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.TTFTable table) throws IOException
	{
		// save current position
		long currentPosition = data.getCurrentPosition();
		data.seek(table.getOffset());

		// read all data
		byte[] bytes = data.read((int)table.getLength());

		// restore current position
		data.seek(currentPosition);
		return bytes;
	}

	/**
	 * This will get the naming table for the true type font.
	 * 
	 * @return The naming table.
	 */
	public synchronized pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.NamingTable getNaming() throws IOException
	{
		pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.NamingTable naming = (pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.NamingTable)tables.get( pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.NamingTable.TAG );
		if (naming != null && !naming.getInitialized())
		{
			readTable(naming);
		}
		return naming;
	}

	/**
	 * Get the postscript table for this TTF.
	 * 
	 * @return The postscript table.
	 */
	public synchronized pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.PostScriptTable getPostScript() throws IOException
	{
		pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.PostScriptTable postscript = (pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.PostScriptTable)tables.get( pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.PostScriptTable.TAG );
		if (postscript != null && !postscript.getInitialized())
		{
			readTable(postscript);
		}
		return postscript;
	}

	/**
	 * Get the OS/2 table for this TTF.
	 * 
	 * @return The OS/2 table.
	 */
	public synchronized pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.OS2WindowsMetricsTable getOS2Windows() throws IOException
	{
		pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.OS2WindowsMetricsTable os2WindowsMetrics = (pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.OS2WindowsMetricsTable)tables.get( pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.OS2WindowsMetricsTable.TAG );
		if (os2WindowsMetrics != null && !os2WindowsMetrics.getInitialized())
		{
			readTable(os2WindowsMetrics);
		}
		return os2WindowsMetrics;
	}

	/**
	 * Get the maxp table for this TTF.
	 * 
	 * @return The maxp table.
	 */
	public synchronized pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.MaximumProfileTable getMaximumProfile() throws IOException
	{
		pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.MaximumProfileTable maximumProfile = (pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.MaximumProfileTable)tables.get( pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.MaximumProfileTable.TAG );
		if (maximumProfile != null && !maximumProfile.getInitialized())
		{
			readTable(maximumProfile);
		}
		return maximumProfile;
	}

	/**
	 * Get the head table for this TTF.
	 * 
	 * @return The head table.
	 */
	public synchronized pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.HeaderTable getHeader() throws IOException
	{
		pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.HeaderTable header = (pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.HeaderTable)tables.get( pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.HeaderTable.TAG );
		if (header != null && !header.getInitialized())
		{
			readTable(header);
		}
		return header;
	}

	/**
	 * Get the hhea table for this TTF.
	 * 
	 * @return The hhea table.
	 */
	public synchronized pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.HorizontalHeaderTable getHorizontalHeader() throws IOException
	{
		pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.HorizontalHeaderTable horizontalHeader = (pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.HorizontalHeaderTable)tables.get( pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.HorizontalHeaderTable.TAG );
		if (horizontalHeader != null && !horizontalHeader.getInitialized())
		{
			readTable(horizontalHeader);
		}
		return horizontalHeader;
	}

	/**
	 * Get the hmtx table for this TTF.
	 * 
	 * @return The hmtx table.
	 */
	public synchronized pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.HorizontalMetricsTable getHorizontalMetrics() throws IOException
	{
		pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.HorizontalMetricsTable horizontalMetrics = (pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.HorizontalMetricsTable)tables.get( pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.HorizontalMetricsTable.TAG );
		if (horizontalMetrics != null && !horizontalMetrics.getInitialized())
		{
			readTable(horizontalMetrics);
		}
		return horizontalMetrics;
	}

	/**
	 * Get the loca table for this TTF.
	 * 
	 * @return The loca table.
	 */
	public synchronized pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.IndexToLocationTable getIndexToLocation() throws IOException
	{
		pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.IndexToLocationTable indexToLocation = (pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.IndexToLocationTable)tables.get( pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.IndexToLocationTable.TAG );
		if (indexToLocation != null && !indexToLocation.getInitialized())
		{
			readTable(indexToLocation);
		}
		return indexToLocation;
	}

	/**
	 * Get the glyf table for this TTF.
	 * 
	 * @return The glyf table.
	 */
	public synchronized pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.GlyphTable getGlyph() throws IOException
	{
		pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.GlyphTable glyph = (pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.GlyphTable)tables.get( pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.GlyphTable.TAG );
		if (glyph != null && !glyph.getInitialized())
		{
			readTable(glyph);
		}
		return glyph;
	}

	/**
	 * Get the "cmap" table for this TTF.
	 * 
	 * @return The "cmap" table.
	 */
	public synchronized pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapTable getCmap() throws IOException
	{
		pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapTable cmap = (pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapTable)tables.get( pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapTable.TAG );
		if (cmap != null && !cmap.getInitialized())
		{
			readTable(cmap);
		}
		return cmap;
	}

	/**
	 * This permit to get the data of the True Type Font
	 * program representing the stream used to build this 
	 * object (normally from the TTFParser object).
	 * 
	 * @return COSStream True type font program stream
	 * 
	 * @throws IOException If there is an error getting the font data.
	 */
	public InputStream getOriginalData() throws IOException 
	{
		return data.getOriginalData(); 
	}

	/**
	 * Read the given table if necessary. Package-private, used by TTFParser only.
	 * 
	 * @param table the table to be initialized
	 */
	void readTable( pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.TTFTable table) throws IOException
	{
		// save current position
		long currentPosition = data.getCurrentPosition();
		data.seek(table.getOffset());
		table.read(this, data);
		// restore current position
		data.seek(currentPosition);
	}

	/**
	 * Returns the number of glyphs (MaximuProfile.numGlyphs).
	 * 
	 * @return the number of glyphs
	 */
	public int getNumberOfGlyphs() throws IOException
	{
		if (numberOfGlyphs == -1)
		{
			pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.MaximumProfileTable maximumProfile = getMaximumProfile();
			if (maximumProfile != null)
			{
				numberOfGlyphs = maximumProfile.getNumGlyphs();
			}
			else
			{
				// this should never happen
				numberOfGlyphs = 0;
			}
		}
		return numberOfGlyphs;
	}

	/**
	 * Returns the units per EM (Header.unitsPerEm).
	 * 
	 * @return units per EM
	 */
	public int getUnitsPerEm() throws IOException
	{
		if (unitsPerEm == -1)
		{
			pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.HeaderTable header = getHeader();
			if (header != null)
			{
				unitsPerEm = header.getUnitsPerEm();
			}
			else
			{
				// this should never happen
				unitsPerEm = 0;
			}
		}
		return unitsPerEm;
	}

	/**
	 * Returns the width for the given GID.
	 * 
	 * @param gid the GID
	 * @return the width
	 */
	public int getAdvanceWidth(int gid) throws IOException
	{
		pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.HorizontalMetricsTable hmtx = getHorizontalMetrics();
		if (hmtx != null)
		{
			return hmtx.getAdvanceWidth(gid);
		}
		else
		{
			// this should never happen
			return 250;
		}
	}

	@Override
	public String getName() throws IOException
	{
		if (getNaming() != null)
		{
			return getNaming().getPostScriptName();
		}
		else
		{
			return null;
		}
	}

	private synchronized void readPostScriptNames() throws IOException
	{
		if (postScriptNames == null)
		{
			postScriptNames = new HashMap<String, Integer>();
			if (getPostScript() != null)
			{
				String[] names = getPostScript().getGlyphNames();
				if (names != null)
				{
					for (int i = 0; i < names.length; i++)
					{
						postScriptNames.put(names[i], i);
					}
				}
			}
		}
	}
	
	/**
	 * Returns the best Unicode from the font (the most general). The PDF spec says that "The means
	 * by which this is accomplished are implementation-dependent."
	 * 
	 * @throws IOException if the font could not be read
	 */
	public pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapSubtable getUnicodeCmap() throws IOException
	{
		return getUnicodeCmap(true);
	}

	/**
	 * Returns the best Unicode from the font (the most general). The PDF spec says that "The means
	 * by which this is accomplished are implementation-dependent."
	 *
	 * @param isStrict False if we allow falling back to any cmap, even if it's not Unicode.
	 * @throws IOException if the font could not be read, or there is no Unicode cmap
	 */
	public pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapSubtable getUnicodeCmap( boolean isStrict) throws IOException
	{
		pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapTable cmapTable = getCmap();
		if (cmapTable == null)
		{
			return null;
		}

		pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapSubtable cmap = cmapTable.getSubtable(pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapTable.PLATFORM_UNICODE,
				pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapTable.ENCODING_UNICODE_2_0_FULL);
		if (cmap == null)
		{
			cmap = cmapTable.getSubtable(pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapTable.PLATFORM_UNICODE,
					pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapTable.ENCODING_UNICODE_2_0_BMP);
		}
		if (cmap == null)
		{
			cmap = cmapTable.getSubtable(pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapTable.PLATFORM_WINDOWS,
					pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapTable.ENCODING_WIN_UNICODE_BMP);
		}
		if (cmap == null)
		{
			// Microsoft's "Recommendations for OpenType Fonts" says that "Symbol" encoding
			// actually means "Unicode, non-standard character set"
			cmap = cmapTable.getSubtable(pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapTable.PLATFORM_WINDOWS,
					pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapTable.ENCODING_WIN_SYMBOL);
		}
		if (cmap == null)
		{
			if (isStrict)
			{
				throw new IOException("The TrueType font does not contain a Unicode cmap");
			}
			else
			{
				// fallback to the first cmap (may not be Unicode, so may produce poor results)
				cmap = cmapTable.getCmaps()[0];
			}
		}
		return cmap;
	}

	/**
	 * Returns the GID for the given PostScript name, if the "post" table is present.
	 */
	public int nameToGID(String name) throws IOException
	{
		// look up in 'post' table
		readPostScriptNames();
		Integer gid = postScriptNames.get(name);
		if (gid != null && gid > 0 && gid < getMaximumProfile().getNumGlyphs())
		{
			return gid;
		}
		// look up in 'cmap'
		int uni = parseUniName(name);
		if (uni > -1)
		{
			pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.CmapSubtable cmap = getUnicodeCmap(false);
			return cmap.getGlyphId(uni);
		}
		return 0;
	}
	
	/**
	 * Parses a Unicode PostScript name in the format uniXXXX.
	 */
	private int parseUniName(String name) throws IOException
	{
		if (name.startsWith("uni") && name.length() == 7)
		{
			int nameLength = name.length();
			StringBuilder uniStr = new StringBuilder();
			try
			{
				for (int chPos = 3; chPos + 4 <= nameLength; chPos += 4)
				{
					int codePoint = Integer.parseInt(name.substring(chPos, chPos + 4), 16);
					if (codePoint <= 0xD7FF || codePoint >= 0xE000) // disallowed code area
					{
						uniStr.append((char) codePoint);
					}
				}
				String unicode = uniStr.toString();
				if (unicode.length() == 0)
				{
					return -1;
				}
				return unicode.codePointAt(0);
			}
			catch (NumberFormatException e)
			{
				return -1;
			}
		}
		return -1;
	}

	@Override
	public Path getPath(String name) throws IOException
	{
		readPostScriptNames();
		int gid = nameToGID(name);
		if (gid < 0 || gid >= getMaximumProfile().getNumGlyphs())
		{
			gid = 0;
		}

		// some glyphs have no outlines (e.g. space, table, newline)
		pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.GlyphData glyph = getGlyph().getGlyph(gid);
		if (glyph == null)
		{
			return new Path();
		}
		else
		{
			Path path = glyph.getPath();

			// scale to 1000upem, per PostScript convention
			float scale = 1000f / getUnitsPerEm();
			AffineTransform atScale = AffineTransform.getScaleInstance(scale, scale);
			path.transform(atScale.toMatrix());

			return path;
		}
	}

	//    @Override TODO
	public float getWidth(String name) throws IOException
	{
		Integer gid = nameToGID(name);

		int width = getAdvanceWidth(gid);
		int unitsPerEM = getUnitsPerEm();
		if (unitsPerEM != 1000)
		{
			width *= 1000f / unitsPerEM;
		}
		return width;
	}

	@Override
	public boolean hasGlyph(String name) throws IOException
	{
		return nameToGID(name) != 0;
	}

	@Override
	public Encoding getEncoding()
	{
		return null;
	}

	@Override
	public BoundingBox getFontBBox() throws IOException
	{
		short xMin = getHeader().getXMin();
		short xMax = getHeader().getXMax();
		short yMin = getHeader().getYMin();
		short yMax = getHeader().getYMax();
		float scale = 1000f / getUnitsPerEm();
		return new BoundingBox(xMin * scale, yMin * scale, xMax * scale, yMax * scale);
	}

	@Override
	public String toString()
	{
		try
		{
			if (getNaming() != null)
			{
				return getNaming().getPostScriptName();
			}
			else
			{
				return "(null)";
			}
		}
		catch (IOException e)
		{
			return "(null - " + e.getMessage() + ")";
		}
	}
}
