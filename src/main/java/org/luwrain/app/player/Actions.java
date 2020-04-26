/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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
    private final Luwrain luwrain = null;
    private final Strings strings = null;
    final Conversations conv = null;

    /*
    boolean onAddAlbum(ListArea listArea)
    {
	NullCheck.notNull(listArea, "listArea");
	final Album.Type type = conv.newAlbumType();
	if (type == null)
	    return true;
	final String path = Albums.addAlbum(luwrain.getRegistry(), type, strings.newAlbumTitle());
	base.updateAlbums();
	listArea.refresh();
	return true;
    }

    boolean onDeleteAlbum(ListArea albumsArea)
    {
	NullCheck.notNull(albumsArea, "albumsArea");
	final Object obj = albumsArea.selected();
	if (obj== null || !(obj instanceof Album))
	    return false;
	final Album album = (Album)obj;
	if (!conv.confirmAlbumDeleting(album.getTitle()))
	    return true;
	Albums.deleteAlbum(luwrain.getRegistry(), album.registryPath);
	base.updateAlbums();
	albumsArea.refresh();
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
    */
}
