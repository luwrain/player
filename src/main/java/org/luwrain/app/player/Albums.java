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

final class Albums
{
    static private final String LOG_COMPONENT = Base.LOG_COMPONENT;
    
    private final Base base;
    private final Registry registry;

    Albums(Base base, Registry registry)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(registry, "registry");
	this.base = base;
	this.registry = registry;
    }

    Album[] loadRegistryAlbums()
    {
	final List<Album> res = new LinkedList();
	registry.addDirectory(Settings.ALBUMS_PATH);
	for(String s: registry.getDirectories(Settings.ALBUMS_PATH))
	{
	    final String path = Registry.join(Settings.ALBUMS_PATH, s);
	    final Album album = loadAlbum(path);
	    if (album != null)
		res.add(album);
	}
	return res.toArray(new Album[res.size()]);
    }

    private Album loadAlbum(String path)
    {
	NullCheck.notEmpty(path, "path");
	Log.debug(LOG_COMPONENT, "loading album from " + path);
	final Settings.Album sett = Settings.createAlbum(registry, path);
	final String typeStr = sett.getType("");
	final Album.Type type;
	switch(typeStr)
	{
	case Settings.TYPE_DIRECTORY:
	    type = Album.Type.DIR;
	    break;
	case "streaming":
type = Album.Type.STREAMING;
break;
	default:
	    Log.warning(LOG_COMPONENT, "the album of the unknown type \'" + typeStr + "\' in " + path);
	    return null;
	}
	return new Album(type, sett.getTitle(""), Settings.decodeProperties(sett.getProperties("")), path);
    }

    static boolean addPlaylist(Registry registry, Conversations.NewPlaylistParams params)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(params, "params");
	final int num = Registry.nextFreeNum(registry, Settings.ALBUMS_PATH);
	final String path = Registry.join(Settings.ALBUMS_PATH, "" + num);
	registry.addDirectory(path);
	/*
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
	*/
	return false;
    }

    static void deletePlaylist(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
	registry.deleteDirectory(path);
    }
}
