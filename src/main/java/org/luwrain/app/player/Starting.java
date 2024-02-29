/*
   Copyright 2012-2024 Michael Pozhidaev <msp@luwrain.org>

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
import java.util.concurrent.atomic.*;
import java.io.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.player.*;
import org.luwrain.util.*;

final class Starting
{
    static private final long
	SAVE_POS_STEP = 1000;

    private final App app;
    Starting(App app) { this.app = app; }

    private boolean onDir(Album album)
    {
	final String path = album.getPath();
	if (path.isEmpty())
	    return false;
	final List<String> urls = new ArrayList<>();
	final App.TaskId taskId = app.newTaskId();
	return app.runTask(taskId, ()->{
		collectMusicFiles(new File(path), urls);
		app.finishedTask(taskId, ()->{
			final AtomicLong prevPosMsec = new AtomicLong(album.getPosMsec());
			final Playlist playlist = new FixedPlaylist(urls.toArray(new String[urls.size()]),
								    (trackNum, posMsec)->{
									if (!album.isSavePosition())
									    return;
									if (Math.abs(posMsec - prevPosMsec.get()) < SAVE_POS_STEP)
									    return;
									prevPosMsec.set(posMsec);
									album.setTrackNum(trackNum);
									album.setPosMsec(posMsec);
									app.getAlbums().save();
								    },
								    (value)->{
									album.setVolume(value);
									app.getAlbums().save();
								    }, album.getVolume());
			app.getPlayer().play(playlist,
					     album.isSavePosition()?album.getTrackNum():0,
					     album.isSavePosition()?album.getPosMsec():0,
					     EnumSet.noneOf(Player.Flags.class));
		    });
	    });
    }


    private boolean onStreaming(Album album)
    {
	final String url = album.getUrl();
	if (url == null || url.trim().isEmpty())
	    return false;
	final Playlist playlist = new FixedPlaylist(new String[]{url.trim()}, (value)->{
		album.setVolume(value);
		app.getAlbums().save();
	    }, album.getVolume());
	app.getPlayer().play(playlist, 0, 0, EnumSet.of(Player.Flags.STREAMING));
	return true;
    }


    private void collectMusicFiles(File file, List<String> res)
    {
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

        boolean play(Album album)
    {
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

}
