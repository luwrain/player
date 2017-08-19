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
	final List<Playlist> res = new LinkedList<Playlist>();
	registry.addDirectory(Settings.PLAYLISTS_PATH);
	for(String s: registry.getDirectories(Settings.PLAYLISTS_PATH))
	{
	    final String path = Registry.join(Settings.PLAYLISTS_PATH, s);
	    final Playlist playlist = loadPlaylist(path);
	    if (playlist != null)
		res.add(playlist);
	}
	return res.toArray(new Playlist[res.size()]);
    }

    private Playlist loadPlaylist(String path)
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
	return new Playlist(sett, TracksLoaders.newDirectoryLoader(base, dirPath));
    }

    private Playlist loadStreamingPlaylist(String path)
    {
	NullCheck.notEmpty(path, "path");
	final Settings.StreamingPlaylist sett = Settings.createStreamingPlaylist(registry, path);
	final String title = sett.getTitle("");
	final String url = sett.getUrl("");
	if (title.isEmpty() || url.isEmpty())
	    return null;
	return new Playlist(sett, ()->{
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
}
