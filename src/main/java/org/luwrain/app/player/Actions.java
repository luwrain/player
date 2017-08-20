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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;

class Actions
{
    private final Luwrain luwrain;
    private final Base base;
    private final Strings strings;
    final Conversations conversations;

    Actions(Luwrain luwrain, Base base, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(base, "base");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	this.base = base;
	this.conversations = new Conversations(luwrain);
    }

    boolean onAddPlaylist(ListArea listArea)
    {
	NullCheck.notNull(listArea, "listArea");
	final Conversations.NewPlaylistParams params = conversations.addPlaylist();
	if (params == null)
	    return true;
	RegistryPlaylists.addPlaylist(luwrain.getRegistry(), params);
	listArea.refresh();
	return true;
    }

    boolean onPlaylistsClick(Area playlistArea, Object obj)
    {
	NullCheck.notNull(playlistArea, "playlistArea");
	if (obj == null || !(obj instanceof Playlist))
	    return false;
	final Playlist playlist = (Playlist)obj;
	base.player.play(playlist.toGeneralPlaylist(), 0, 0);
	if (!playlist.getFlags().contains(Playlist.Flags.STREAMING))
	    luwrain.setActiveArea(playlistArea);
	return true;
    }

    boolean commonKeys(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.isModified())
	    return false;
	if (event.isSpecial())
	    switch(event.getSpecial())
	    {
	    case F5:
		pauseResume();
		return true;
	    case ESCAPE:
	    case F6:
		stop();
		return true;
	    case F7:
		prevTrack();
		return true;
	    case F8:
		nextTrack();
		return true;
	    default:
		return false;
	    }
	switch(event.getChar())
	{
	case '-':
	    jump(-5000);
	    return true;
	case '=':
	    jump(5000);
	    return true;
	case '[':
	    jump(-60000);
	    return true;
	case ']':
	    jump(60000);
	    return true;
	default:
	    return false;
	}
    }

    void pauseResume()
    {
	base.player.pauseResume();
    }

    void stop()
    {
	base.player.stop();
    }

    void prevTrack()
    {
	base.player.prevTrack();
    }

    void nextTrack()
    {
	base.player.nextTrack();
    }

    void jump(long offsetMsec)
    {
	base.player.jump(offsetMsec);
    }
}
