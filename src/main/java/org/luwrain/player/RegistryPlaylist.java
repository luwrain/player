/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.player;

import java.nio.file.*;

import org.luwrain.core.*;

public class RegistryPlaylist extends PlaylistBase
{
    static public final String PLAYLISTS_PATH = "/org/luwrain/player/playlists";

    protected Registry registry = null;
    protected Settings settings = null;
    protected String path;
    //    protected String url;

    RegistryPlaylist(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	this.registry = registry;
    }

    boolean init(String path)
    {
	NullCheck.notNull(path, "path");
	this.path = path;
	settings = createSettings(registry, path);
	title = settings.getTitle("");
	final String url = settings.getUrl("");
	streaming = settings.getStreaming(false);
	hasBookmark = settings.getHasBookmark(false);
	if (!url.isEmpty())
	{
	    if (!streaming)
	    {
		final M3uPlaylist playlist = new M3uPlaylist();
		playlist.load(Paths.get(url));
		for(String s: playlist.getPlaylistItems())
		    items.add(s);
	    } else 
		items.add(url);
	}
	return true;
    }

    @Override public int getStartingTrackNum()
    {
	NullCheck.notNull(settings, "settings");
	return settings.getStartingTrackNum(0);
    }

    @Override public long getStartingPosMsec()
    {
	NullCheck.notNull(settings, "settings");
	return settings.getStartingPosMsec(0);
    }

    @Override public void updateStartingPos(int trackNum, long posMsec)
    {
	NullCheck.notNull(settings, "settings");
	if (!hasBookmark)
	    return;
	settings.setStartingTrackNum(trackNum);
	settings.setStartingPosMsec((int)posMsec);
    }

    public void setPlaylistTitle(String value)
    {
	NullCheck.notNull(value, "value");
	NullCheck.notNull(settings, "settings");
	this.title = value;
	settings.setTitle(value);
    }

    public String getPlaylistUrl()
    {
	NullCheck.notNull(settings, "settings");
	return settings.getUrl("");
    }

    public void setPlaylistUrl(String value)
    {
	NullCheck.notNull(value, "value");
	NullCheck.notNull(settings, "settings");
	settings.setUrl(value);
    }



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

    static protected Settings createSettings(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(path, "path");
return RegistryProxy.create(registry, path, Settings.class);
    }

    protected interface Settings
    {
	String getTitle(String defValue);
	void setTitle(String value);
	String getUrl(String defValue);
	void setUrl(String value);
	boolean getStreaming(boolean defValue);
	void setStreaming(boolean value);
	boolean getHasBookmark(boolean defValue);
	void setHasBookmark(boolean value);
	int getStartingTrackNum(int defValue);
	void setStartingTrackNum(int value);
	int getStartingPosMsec(int defValue);
	void setStartingPosMsec(int value);
    }
}
