package pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.font;

import android.util.Log;

import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.cff.CFFCIDFont;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.cff.CFFFont;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.cff.CFFParser;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.cff.CFFType1Font;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.TTFParser;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.TrueTypeFont;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.ttf.Type1Equivalent;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.type1.Type1Font;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.io.IOUtils;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.PDFResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * External font service, locates non-embedded fonts via a pluggable FontProvider.
 *
 * @author John Hewson
 */
public final class ExternalFonts
{
	private ExternalFonts() {}

	// lazy thread safe singleton
	private static class DefaultFontProvider
	{
		private static final FontProvider INSTANCE = new FileSystemFontProvider();
	}

	private static FontProvider fontProvider;

	/** fallback fonts, used as as a last resort */
	private static final TrueTypeFont ttfFallbackFont;
	private static final CFFCIDFont cidFallbackFont;
	static
	{
		try
		{
			// ttf
			String ttfName = "com.example.demo.pdfbox/resources/ttf/Helvetica_45_light.ttf";
			InputStream ttfStream;
			if(PDFResourceLoader.isReady()) {
				ttfStream = PDFResourceLoader.getStream(ttfName);
				if (ttfStream == null)
				{
					throw new IOException("Error loading resource: " + ttfStream);
				}
			} else {
				// Fallback
				URL url = ExternalFonts.class.getClassLoader().getResource(ttfName);
				if (url == null)
				{
					throw new IOException("Error loading resource: " + ttfName);
				}
				ttfStream = url.openStream();
			}
			
			
			TTFParser ttfParser = new TTFParser();
			ttfFallbackFont = ttfParser.parse(ttfStream);

			// cff
			String cffName = "com.example.demo.pdfbox/resources/otf/Helvetica_45_light.ttf";
			InputStream cffStream;
			if(PDFResourceLoader.isReady()) {
				cffStream = PDFResourceLoader.getStream(cffName);
				if (cffStream == null)
				{
					throw new IOException("Error loading resource: " + cffName);
				} 
			} else {
				// Fallback
				URL url = ExternalFonts.class.getClassLoader().getResource(cffName);
				if (url == null)
				{
					throw new IOException("Error loading resource: " + ttfName);
				}
				cffStream = url.openStream();
			}
			
			byte[] bytes = IOUtils.toByteArray(cffStream);
			CFFParser cffParser = new CFFParser();
			cidFallbackFont = (CFFCIDFont)cffParser.parse(bytes).get(0);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sets the font service provider.
	 */
	public static void setProvider(FontProvider fontProvider)
	{
		ExternalFonts.fontProvider = fontProvider;
	}

	/**
	 * Returns the font service provider. Defaults to using FileSystemFontProvider.
	 */
	public static FontProvider getProvider()
	{
		if (fontProvider == null)
		{
			fontProvider = DefaultFontProvider.INSTANCE;
		}
		return fontProvider;
	}

	/** Map of PostScript name substitutes, in priority order. */
	private static final Map<String, List<String>> substitutes = new HashMap<String, List<String>>();
	static
	{
		// substitutes for standard 14 fonts
		substitutes.put("Courier",
				Arrays.asList("CourierNew", "CourierNewPSMT", "LiberationMono", "NimbusMonL-Regu","DroidSansMono"));
		substitutes.put("Courier-Bold",
				Arrays.asList("CourierNewPS-BoldMT", "CourierNew-Bold", "LiberationMono-Bold",
						"NimbusMonL-Bold","DroidSansMono"));
		substitutes.put("Courier-Oblique",
				Arrays.asList("CourierNewPS-ItalicMT","CourierNew-Italic",
						"LiberationMono-Italic", "NimbusMonL-ReguObli","DroidSansMono"));
		substitutes.put("Courier-BoldOblique",
				Arrays.asList("CourierNewPS-BoldItalicMT","CourierNew-BoldItalic",
						"LiberationMono-BoldItalic", "NimbusMonL-BoldObli","DroidSansMono"));
		substitutes.put("Helvetica",
				Arrays.asList("ArialMT", "Arial", "LiberationSans", "NimbusSanL-Regu","Roboto-Regular"));
		substitutes.put("Helvetica-Bold",
				Arrays.asList("Arial-BoldMT", "Arial-Bold", "LiberationSans-Bold",
						"NimbusSanL-Bold","Roboto-Bold"));
		substitutes.put("Helvetica-Oblique",
				Arrays.asList("Arial-ItalicMT", "Arial-Italic", "Helvetica-Italic",
						"LiberationSans-Italic", "NimbusSanL-ReguItal", "Roboto-Italic"));
		substitutes.put("Helvetica-BoldOblique",
				Arrays.asList("Arial-BoldItalicMT", "Helvetica-BoldItalic",
						"LiberationSans-BoldItalic", "NimbusSanL-BoldItal","Roboto-BoldItalic"));
		substitutes.put("Times-Roman",
				Arrays.asList("TimesNewRomanPSMT", "TimesNewRoman", "TimesNewRomanPS",
						"LiberationSerif", "NimbusRomNo9L-Regu","DroidSerif-Regular", "Roboto-Regular"));
		substitutes.put("Times-Bold",
				Arrays.asList("TimesNewRomanPS-BoldMT", "TimesNewRomanPS-Bold",
						"TimesNewRoman-Bold", "LiberationSerif-Bold",
						"NimbusRomNo9L-Medi", "DroidSerif-Bold", "Roboto-Bold"));
		substitutes.put("Times-Italic",
				Arrays.asList("TimesNewRomanPS-ItalicMT", "TimesNewRomanPS-Italic",
						"TimesNewRoman-Italic", "LiberationSerif-Italic",
						"NimbusRomNo9L-ReguItal","DroidSerif-Italic", "Roboto-Italic"));
		substitutes.put("Times-BoldItalic",
				Arrays.asList("TimesNewRomanPS-BoldItalicMT", "TimesNewRomanPS-BoldItalic",
						"TimesNewRoman-BoldItalic", "LiberationSerif-BoldItalic",
						"NimbusRomNo9L-MediItal","DroidSerif-BoldItalic", "Roboto-BoldItalic"));
		substitutes.put("Symbol", Arrays.asList("SymbolMT", "StandardSymL"));
		substitutes.put("ZapfDingbats", Arrays.asList("ZapfDingbatsITC", "Dingbats"));
		// TODO: load extra fonts? (DroidSerif for times and a symbol font)

		// extra substitute mechanism for CJK CIDFonts when all we know is the ROS
		substitutes.put("$Adobe-CNS1", Arrays.asList("AdobeMingStd-Light"));
		substitutes.put("$Adobe-Japan1", Arrays.asList("KozMinPr6N-Regular"));
		substitutes.put("$Adobe-Korea1", Arrays.asList("AdobeGothicStd-Bold"));
		substitutes.put("$Adobe-GB1", Arrays.asList("AdobeHeitiStd-Regular"));

		// Acrobat also uses alternative names for Standard 14 fonts, which we map to those above
		// these include names such as "Arial" and "TimesNewRoman"
		for (String baseName : Standard14Fonts.getNames())
		{
			if (!substitutes.containsKey(baseName))
			{
				String mappedName = Standard14Fonts.getMappedFontName(baseName);
				substitutes.put(baseName, copySubstitutes(mappedName));
			}
		}
	}

	/**
	 * Copies a list of font substitutes, adding the original font at the start of the list.
	 */
	private static List<String> copySubstitutes(String postScriptName)
	{
		return new ArrayList<String>(substitutes.get(postScriptName));
	}

	/**
	 * Adds a top-priority substitute for the given font.
	 *
	 * @param match PostScript name of the font to match
	 * @param replace PostScript name of the font to use as a replacement
	 */
	public static void addSubstitute(String match, String replace)
	{
		if (!substitutes.containsKey(match))
		{
			substitutes.put(match, new ArrayList<String>());
		}
		substitutes.get(match).add(replace);
	}

	/**
	 * Returns the substitutes for a given font.
	 */
	private static List<String> getSubstitutes(String postScriptName)
	{
		List<String> subs = substitutes.get(postScriptName.replaceAll(" ", ""));
		if (subs != null)
		{
			return subs;
		}
		else
		{
			return Collections.emptyList();
		}
	}

	/**
	 * Windows name (ArialNarrow,Bold) to PostScript name (ArialNarrow-Bold)
	 */
	private static String windowsToPs(String windowsName)
	{
		return windowsName.replaceAll(",", "-");
	}

	/**
	 * Finds a CFF CID-Keyed font with the given PostScript name, or a suitable substitute, or null.
	 *
	 * @param registryOrdering the CID system registry and ordering e.g. "Adobe-Japan1", if any
	 * @param fontDescriptor the font descriptor, if any
	 */
	public static CFFCIDFont getCFFCIDFontFallback(String registryOrdering,
			PDFontDescriptor fontDescriptor)
	{
		// try ROS substitutes
		// todo: this is a fairly primitive mechanism and could be improved
		if (registryOrdering != null)
		{
			for (String substituteName : getSubstitutes("$" + registryOrdering))
			{
				CFFFont cff = getProvider().getCFFFont(substituteName);
				if (cff instanceof CFFCIDFont)
				{
					return (CFFCIDFont)cff;
				}
			}
		}
		return cidFallbackFont;
	}

	/**
	 * Returns the fallback font, used for rendering when no other fonts are available,
	 * we attempt to find a good fallback based on the font descriptor.
	 */
	public static Type1Equivalent getType1FallbackFont( PDFontDescriptor fontDescriptor)
	{
		String fontName = getFallbackFontName(fontDescriptor);
		Type1Equivalent type1Equivalent = getType1EquivalentFont(fontName);
		if (type1Equivalent == null)
		{
			// only systems with no fonts should reach this point, so we return a basic fallback
			Log.e("PdfBoxAndroid", "No fallback font for '" + fontName + "'");
			return ttfFallbackFont;
		}
		return type1Equivalent;
	}

	/**
	 * Returns the fallback font, used for rendering when no other fonts are available,
	 * we attempt to find a good fallback based on the font descriptor.
	 */
	public static TrueTypeFont getTrueTypeFallbackFont(PDFontDescriptor fontDescriptor)
	{
		String fontName = getFallbackFontName(fontDescriptor);
		TrueTypeFont ttf = getTrueTypeFont(fontName);
		if (ttf == null)
		{
			// we have to return something here as TTFs aren't strictly required on the system
			Log.e("PdfBoxAndroid", "No TTF fallback font for '" + fontName + "'");
			return ttfFallbackFont;
		}
		return ttf;
	}

	/**
	 * Attempts to find a good fallback based on the font descriptor.
	 */
	private static String getFallbackFontName(PDFontDescriptor fontDescriptor)
	{
		String fontName;
		if (fontDescriptor != null)
		{
			// heuristic detection of bold
			boolean isBold = false;
			String name = fontDescriptor.getFontName();
			if (name != null)
			{
				String lower = fontDescriptor.getFontName().toLowerCase();
				isBold = lower.contains("bold") ||
						lower.contains("black") ||
						lower.contains("heavy");
			}

			// font descriptor flags should describe the style
			if (fontDescriptor.isFixedPitch())
			{
				fontName = "Courier";
				if (isBold && fontDescriptor.isItalic())
				{
					fontName += "-BoldOblique";
				}
				else if (isBold)
				{
					fontName += "-Bold";
				}
				else if (fontDescriptor.isItalic())
				{
					fontName += "-Oblique";
				}
			}
			else if (fontDescriptor.isSerif())
			{
				fontName = "Times";
				if (isBold && fontDescriptor.isItalic())
				{
					fontName += "-BoldItalic";
				}
				else if (isBold)
				{
					fontName += "-Bold";
				}
				else if (fontDescriptor.isItalic())
				{
					fontName += "-Italic";
				}
				else
				{
					fontName += "-Roman";
				}
			}
			else
			{
				fontName = "Helvetica";
				if (isBold && fontDescriptor.isItalic())
				{
					fontName += "-BoldOblique";
				}
				else if (isBold)
				{
					fontName += "-Bold";
				}
				else if (fontDescriptor.isItalic())
				{
					fontName += "-Oblique";
				}
			}
		}
		else
		{
			// if there is no FontDescriptor then we just fall back to Times Roman
			fontName = "Times-Roman";
		}
		return fontName;
	}

	/**
	 * Finds a TrueType font with the given PostScript name, or a suitable substitute, or null.
	 *
	 * @param postScriptName PostScript font name
	 */
	public static TrueTypeFont getTrueTypeFont(String postScriptName)
	{
		// first ask the font provider for the font
		TrueTypeFont ttf = getProvider().getTrueTypeFont(postScriptName);
		if (ttf == null)
		{
			// then try substitutes
			for (String substituteName : getSubstitutes(postScriptName))
			{
				ttf = getProvider().getTrueTypeFont(substituteName);
				if (ttf != null)
				{
					return ttf;
				}
			}
			// then Windows name
			ttf = getProvider().getTrueTypeFont(windowsToPs(postScriptName));
		}
		return ttf;
	}

	/**
	 * Finds a TrueType font with the given PostScript name, or a suitable substitute, or null.
	 *
	 * @param postScriptName PostScript font name
	 */
	public static Type1Font getType1Font( String postScriptName)
	{
		// first ask the font provider for the font
		Type1Font t1 = getProvider().getType1Font(postScriptName);
		if (t1 == null)
		{
			// then try substitutes
			for (String substituteName : getSubstitutes(postScriptName))
			{
				t1 = getProvider().getType1Font(substituteName);
				if (t1 != null)
				{
					return t1;
				}
			}
			// then Windows name
			t1 = getProvider().getType1Font(windowsToPs(postScriptName));
		}
		return t1;
	}

	/**
	 * Finds a CFF Type 1 font with the given PostScript name, or a suitable substitute, or null.
	 *
	 * @param postScriptName PostScript font name
	 */
	public static CFFType1Font getCFFType1Font( String postScriptName)
	{
		CFFFont cff = getCFFFont(postScriptName);
		if (cff instanceof CFFType1Font)
		{
			return (CFFType1Font)cff;
		}
		return null;
	}

	/**
	 * Finds a CFF CID-Keyed font with the given PostScript name, or a suitable substitute, or null.
	 *
	 * @param postScriptName PostScript font name
	 */
	public static CFFCIDFont getCFFCIDFont(String postScriptName)
	{
		CFFFont cff = getCFFFont(postScriptName);
		if (cff instanceof CFFCIDFont)
		{
			return (CFFCIDFont)cff;
		}
		return null;
	}

	/**
	 * Finds a CFF font with the given PostScript name, or a suitable substitute, or null.
	 *
	 * @param postScriptName PostScript font name
	 */
	private static CFFFont getCFFFont(String postScriptName)
	{
		// first ask the font provider for the font
		CFFFont cff = getProvider().getCFFFont(postScriptName);
		if (cff == null)
		{
			// then try substitutes
			for (String substituteName : getSubstitutes(postScriptName))
			{
				cff = getProvider().getCFFFont(substituteName);
				if (cff != null)
				{
					return cff;
				}
			}

			// then Windows name
			cff = getProvider().getCFFFont(windowsToPs(postScriptName));
		}
		return cff;
	}

	/**
	 * Finds a Type 1-equivalent font with the given PostScript name, or a suitable substitute,
	 * or null. This allows a Type 1 font to be substituted with a PFB, TTF or OTF.
	 *
	 * @param postScriptName PostScript font name
	 */
	public static Type1Equivalent getType1EquivalentFont(String postScriptName)
	{
		Type1Font t1 = getType1Font(postScriptName);
		if (t1 != null)
		{
			return t1;
		}

		CFFType1Font cff = getCFFType1Font(postScriptName);
		if (cff != null)
		{
			return cff;
		}

		TrueTypeFont ttf = getTrueTypeFont(postScriptName);
		if (ttf != null)
		{
			return ttf;
		}

		return null;
	}
}
