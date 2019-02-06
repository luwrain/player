/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

final class RegistryAlbums
{
    private final Base base;
    private final Registry registry;

    RegistryAlbums(Base base, Registry registry)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(registry, "registry");
	this.base = base;
	this.registry = registry;
    }

    Album[] loadRegistryAlbums()
    {
	final List<Album> res = new LinkedList();
	registry.addDirectory(Settings.PLAYLISTS_PATH);
	for(String s: registry.getDirectories(Settings.PLAYLISTS_PATH))
	{
	    final String path = Registry.join(Settings.PLAYLISTS_PATH, s);
	    final Album album = loadAlbum(path);
	    if (album != null)
		res.add(album);
	}
	return res.toArray(new Album[res.size()]);
    }

    private Album loadAlbum(String path)
    {
	NullCheck.notEmpty(path, "path");
	if (registry.getTypeOf(Registry.join(path, Settings.TYPE_VALUE)) != Registry.STRING)
	    return null;
	final String type = registry.getString(Registry.join(path, Settings.TYPE_VALUE));
	switch(type)
	{
	case Settings.TYPE_DIRECTORY:
	    return loadDirectoryAlbum(path);
	case "streaming":
	    return loadStreamingAlbum(path);
	default:
	    return null;
	}
    }

    private Album loadDirectoryAlbum(String path)
    {
	NullCheck.notEmpty(path, "path");
	final Settings.DirectoryPlaylist sett = Settings.createDirectoryPlaylist(registry, path);
	final String title = sett.getTitle("");
	final String dirPath = sett.getPath("");
	if (title.isEmpty() || dirPath.isEmpty())
	    return null;
	final Map<String, String> props = new HashMap();
	props.put("title", title);
	props.put("path", dirPath);
	return new Album(Album.Type.DIR, props, path);
    }

    private Album loadStreamingAlbum(String path)
    {
	NullCheck.notEmpty(path, "path");
	final Settings.StreamingPlaylist sett = Settings.createStreamingPlaylist(registry, path);
	final String title = sett.getTitle("");
	final String url = sett.getUrl("");
	if (title.isEmpty() || url.isEmpty())
	    return null;
	final Map<String, String> props = new HashMap();
	props.put("title", title);
	props.put("url", url);
	return new Album(Album.Type.STREAMING, props, path);
    }

    static boolean addPlaylist(Registry registry, Conversations.NewPlaylistParams params)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(params, "params");
	final int num = Registry.nextFreeNum(registry, Settings.PLAYLISTS_PATH);
	final String path = Registry.join(Settings.PLAYLISTS_PATH, "" + num);
	registry.addDirectory(path);
	switch(params.type)
	{
	case DIRECTORY:
	    {
		final Settings.DirectoryPlaylist sett = Settings.createDirectoryPlaylist(registry, path);
		sett.setType(Settings.TYPE_DIRECTORY);
		sett.setTitle(params.name);
		sett.setPath(params.arg);
		sett.setWithBookmark(false);
		return true;
	    }
	case M3U:
	    {
		final Settings.M3uPlaylist sett = Settings.createM3uPlaylist(registry, path);
		sett.setType(Settings.TYPE_M3U);
		sett.setTitle(params.name);
		sett.setM3uUrl(params.arg);
		sett.setWithBookmark(false);
		return true;
	    }
	case WITH_BOOKMARK_DIR:
	    {
		final Settings.DirectoryPlaylist sett = Settings.createDirectoryPlaylist(registry, path);
		sett.setType(Settings.TYPE_DIRECTORY);
		sett.setTitle(params.name);
		sett.setPath(params.arg);
		sett.setWithBookmark(true);
		return true;
	    }
	case WITH_BOOKMARK_M3U:
	    {
		final Settings.M3uPlaylist sett = Settings.createM3uPlaylist(registry, path);
		sett.setType(Settings.TYPE_M3U);
		sett.setTitle(params.name);
		sett.setM3uUrl(params.arg);
		sett.setWithBookmark(true);
		return true;
	    }
	case STREAMING:
	    {
		final Settings.StreamingPlaylist sett = Settings.createStreamingPlaylist(registry, path);
		sett.setType(Settings.TYPE_M3U);
		sett.setTitle(params.name);
		sett.setUrl(params.arg);
		return true;
	    }
	default:
	    return false;
	}
    }

    static void deletePlaylist(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
	registry.deleteDirectory(path);
    }
}
