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
	final Playlist playlist = new Playlist(url.trim());
	app.getPlayer().play(playlist, 0, 0, EnumSet.of(Player.Flags.STREAMING), new Properties());
	return true;
    }
}
