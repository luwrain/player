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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;

final class Actions
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
	RegistryAlbums.addPlaylist(luwrain.getRegistry(), params);
	listArea.refresh();
	return true;
    }

    boolean onDeletePlaylist(ListArea listArea)
    {
	NullCheck.notNull(listArea, "listArea");
	final Object obj = listArea.selected();
	if (obj== null || !(obj instanceof Album))
	    return false;
	final Album album = (Album)obj;
	if (!conversations.confirmPlaylistDeleting(album.getPlaylistTitle()))
	    return true;
	RegistryAlbums.deletePlaylist(luwrain.getRegistry(), album.registryPath);
	return true;
    }

    boolean onAlbumClick(Area playlistArea, Object obj)
    {
	NullCheck.notNull(playlistArea, "playlistArea");
	if (obj == null || !(obj instanceof Album))
	    return false;
	final Album album = (Album)obj;
	try {
	    return luwrain.xRunHooks("luwrain.player.album.play", new Object[]{album}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY);
	}
	    catch(Exception e)
	    {
		luwrain.message(luwrain.i18n().getExceptionDescr(e), Luwrain.MessageType.ERROR);
		return true;
	    }
    }

    boolean pauseResume()
    {
	return base.player.pauseResume();
    }

    boolean stop()
    {
	return base.player.stop();
    }

    boolean prevTrack()
    {
	return base.player.prevTrack();
    }

    boolean nextTrack()
    {
	return base.player.nextTrack();
    }

    boolean jump(long offsetMsec)
    {
	return base.player.jump(offsetMsec);
    }
}
