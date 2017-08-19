/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.player;

import java.util.*;
import java.io.*;
import java.net.*;

import org.luwrain.core.*;

class RegistryPlaylists
{
    private final Base base;
    private final Registry registry;

    RegistryPlaylists(Base base, Registry registry)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(registry, "registry");
	this.base = base;
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
	return new Playlist(title, ()->{
		final LinkedList<String> filesList = new LinkedList<String>();
		loadFilesList(new File(dirPath), new String[0], filesList);
		final String[] items = filesList.toArray(new String[filesList.size()]);
		final HashMap<String, TrackInfo> trackInfoMap = new HashMap<String, TrackInfo>();
		for(String s: items)
		    try {
			trackInfoMap.put(s, new TrackInfo(new URL(s)));
		    }
		    catch(IOException e)
		    {
			Log.warning("player", "unable to read tags for " + s + ":" + e.getClass().getName() + ":" + e.getMessage());
		    }
		Log.debug("comparing", "" + trackInfoMap.size());
		Arrays.sort(items, new PlaylistComparator(base, trackInfoMap));
		return items;
	});
    }

    private Playlist loadStreamingPlaylist(String path)
    {
	NullCheck.notEmpty(path, "path");
	final Settings.StreamingPlaylist sett = Settings.createStreamingPlaylist(registry, path);
	final String title = sett.getTitle("");
	final String url = sett.getUrl("");
	if (title.isEmpty() || url.isEmpty())
	    return null;
	return new Playlist(title, ()->{
		return new String[]{url};
	}, EnumSet.of(Playlist.Flags.STREAMING));
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

    static private class PlaylistComparator implements Comparator
    {
	private final Base base;
	private final HashMap<String, TrackInfo> trackInfoMap;

	PlaylistComparator(Base base, HashMap<String, TrackInfo> trackInfoMap)
	{
	    NullCheck.notNull(base, "base");
	    NullCheck.notNull(trackInfoMap, "trackInfoMap");
	    this.base = base;
	    this.trackInfoMap = trackInfoMap;
	}

	@Override public int compare(Object o1, Object o2)
	{
	    NullCheck.notNull(o1, "oo1");
	    NullCheck.notNull(o2, "oo2");
	    if (!(o1 instanceof String) || !(o2 instanceof String))
		return o1.toString().compareTo(o2.toString());
	    final String title1 = Utils.getTrackTextAppearanceWithMap((String)o1, trackInfoMap);
	    final String title2 = Utils.getTrackTextAppearanceWithMap((String)o2, trackInfoMap);
	    return title1.compareTo(title2);
	}
    }
}
