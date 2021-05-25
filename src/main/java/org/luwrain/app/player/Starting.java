/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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
import org.luwrain.player.*;
import org.luwrain.util.*;

final class Starting
{
    private final App app;

    Starting(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
    }

    boolean play(Album album)
    {
	NullCheck.notNull(album, "album");
	if (album.getType() == null)
	    return false;
	switch(album.getType())
	{
	case STREAMING:
	    return onStreaming(album);
	case DIR:
	    return onDir(album);
	default:
	    return false;
	}
    }

    private boolean onStreaming(Album album)
    {
	NullCheck.notNull(album, "album");
	final String url = album.getProps().getProperty("url");
	if (url == null || url.trim().isEmpty())
	    return false;
	final Playlist playlist = new FixedPlaylist(new String[]{url.trim()}, (value)->{
		album.getProps().setProperty("volume", String.valueOf(value));
		app.getAlbums().save();
	    }, album.getVolume());
	app.getPlayer().play(playlist, 0, 0, EnumSet.of(Player.Flags.STREAMING), new Properties());
	return true;
    }

    private boolean onDir(Album album)
    {
	NullCheck.notNull(album, "album");
	final String path = album.getProps().getProperty("path");
	if (path == null || path.trim().isEmpty())
	    return false;
	final List<String> urls = new ArrayList();
	final App.TaskId taskId = app.newTaskId();
	return app.runTask(taskId, ()->{
		collectMusicFiles(new File(path), urls);
		app.finishedTask(taskId, ()->{
			final Playlist playlist = new FixedPlaylist(urls.toArray(new String[urls.size()]), (value)->{
				album.getProps().setProperty("volume", String.valueOf(value));
				app.getAlbums().save();
			    }, album.getVolume());
			app.getPlayer().play(playlist, 0, 0, EnumSet.noneOf(Player.Flags.class), new Properties());
		    });
	    });
    }

    private void collectMusicFiles(File file, List<String> res)
    {
	NullCheck.notNull(file, "file");
	NullCheck.notNull(res, "res");
	if (!file.exists())
	    return;
	if (file.isFile())
	{
	    final String name = file.getName().toLowerCase();
	    if (name.endsWith(".mp3"))
		res.add(UrlUtils.fileToUrl(file));
	    return;
	}
	if (!file.isDirectory())
	    return;
	final File[] files = file.listFiles();
	if (files != null)
	{
	    Arrays.sort(files);
	    for(File f: files)
		collectMusicFiles(f, res);
	}
    }
}
