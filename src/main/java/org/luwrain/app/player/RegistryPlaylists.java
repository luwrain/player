
package org.luwrain.app.player;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;

class RegistryPlaylists
{
    private final Registry registry;

    RegistryPlaylists(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	this.registry = registry;
    }

    Playlist[] loadRegistryPlaylists()
    {
	final LinkedList<Playlist> res = new LinkedList<Playlist>();
	for(String s: registry.getDirectories(Settings.PLAYLISTS_PATH))
	{
	    final String path = Registry.join(Settings.PLAYLISTS_PATH, s);
	    final Playlist playlist = loadPlaylist(path);
	    if (playlist != null)
		res.add(playlist);
	}
	return res.toArray(new Playlist[res.size()]);
    }

    Playlist loadPlaylist(String path)
    {
	NullCheck.notEmpty(path, "path");
	if (registry.getTypeOf(Registry.join(path, Settings.TYPE_VALUE)) != Registry.STRING)
	    return null;
	final String type = registry.getString(Registry.join(path, Settings.TYPE_VALUE));
	switch(type)
	{
	case "directory":
	    return loadDirectoryPlaylist(path);
	case "streaming":
	    return loadStreamingPlaylist(path);
	default:
	    Log.warning("player", "unknown playlist type in " + path + ":" + type);
	    return null;
	}
    }

    private Playlist loadDirectoryPlaylist(String path)
    {
	NullCheck.notEmpty(path, "path");
	final Settings.DirectoryPlaylist sett = Settings.createDirectoryPlaylist(registry, path);
	final String title = sett.getTitle("");
	final String dirPath = sett.getPath("");
	if (title.isEmpty() || dirPath.isEmpty())
	    return null;
	final LinkedList<String> filesList = new LinkedList<String>();
	loadFilesList(new File(dirPath), new String[0], filesList);
	final String[] files = filesList.toArray(new String[filesList.size()]);
	return new Playlist(title, files);
    }

    private Playlist loadStreamingPlaylist(String path)
    {
	NullCheck.notEmpty(path, "path");
	final Settings.StreamingPlaylist sett = Settings.createStreamingPlaylist(registry, path);
	final String title = sett.getTitle("");
	final String url = sett.getUrl("");
	if (title.isEmpty() || url.isEmpty())
	    return null;
	return new Playlist(title, new String[]{url}, EnumSet.of(Playlist.Flags.STREAMING));
    }


    /*
    static public void add(Registry registry,
String title, String url,
			   boolean streaming, boolean hasBookmark)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(title, "title");
	NullCheck.notNull(url, "url");
	final int num = Registry.nextFreeNum(registry, PLAYLISTS_PATH);
	final String path = Registry.join(PLAYLISTS_PATH, "" + num);
	registry.addDirectory(path);
	final Settings settings = createSettings(registry, path);
	settings.setTitle(title);
	settings.setUrl(url);
	settings.setStreaming(streaming);
	settings.setHasBookmark(hasBookmark);
    }
    */

    static private void loadFilesList(File dir, String[] endings, List<String> res)
    {
	NullCheck.notNull(dir, "dir");
	NullCheck.notNullItems(endings, "endings");
	NullCheck.notNull(res, "res");
	Log.debug("player", "loading " + dir);
	final File[] files = dir.listFiles();
	if (files == null)
	    return;
	for(File f: files)
	{
	    if (f.isDirectory())
	    {
		loadFilesList(f, endings, res);
		continue;
	    }
	    try {
		res.add(f.getAbsoluteFile().toURI().toURL().toString());
	    }
	    catch(java.net.MalformedURLException e)
	    {
		Log.warning("player", "unable to get URL of " + f.getAbsolutePath() + ":" + e.getMessage());
	    }
	}
    }
}
