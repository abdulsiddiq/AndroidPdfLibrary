package pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.fontbox.util.autodetect;


public class AndroidFontDirFinder extends NativeFontDirFinder
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] getSearchableDirectories() {
		return new String[] {
				"/system/fonts"
				// Shouldn't be any other directories, but they can be added here
		};
	}

}