
package org.luwrain.app.player;

import java.nio.file.*;

import org.luwrain.core.*;

class RegistryPlaylists
{
    static private final String TYPE_VALUE = "type";

    static private final String PLAYLISTS_PATH = "/org/luwrain/player/playlists";

private final Registry registry;

    RegistryPlaylists(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	this.registry = registry;
    }

Playlist[] loadRegistryPlaylists()
    {
	return null;
    }

    Playlist loadPlaylist(String path)
    {
	NullCheck.notEmpty(path, "path");
	if (registry.getTypeOf(Registry.join(path, TYPE_VALUE)) != Registry.STRING)
	    return null;
	final String type = registry.getString(Registry.join(path, TYPE_VALUE));
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
	final String[] files = loadFilesList(dirPath);
	return new Playlist(title, files);
    }


    private Playlist loadStreamingPlaylist(String path)
    {
	/*
	NullCheck.notEmpty(path, "path");
	final Settings.DirectoryPlaylist sett = Settings.createDirectoryPlaylist(registry, path);
	final String title = sett.getTitle("");
	final String dirPath = sett.getPath("");
	if (title.isEmpty() || dirPath.isEmpty())
	    return null;
	final String[] files = loadFilesList(dirPath);
	*/
	return null;
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

    static private String[] loadFilesList(String path)
    {
	return null;
    }

    /*
    @Override public Playlist[] loadRegistryPlaylists()
    {
	final String dir = "/org/luwrain/player/playlists";//FIXME:
	final String[] dirs = registry.getDirectories(dir); 
	final LinkedList<Playlist> res = new LinkedList<Playlist>();
	for(String s: dirs)
	{
	    final String path = Registry.join(dir, s);
	    final RegistryPlaylist playlist = new RegistryPlaylist(registry);
	    if (playlist.init(path))
		res.add(playlist);
	}
	return res.toArray(new Playlist[res.size()]);
    }
    */



}
