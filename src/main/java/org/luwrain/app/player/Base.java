/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

package org.luwrain.app.player;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.nio.channels.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.player.*;

class Base
{
    private Luwrain luwrain;
    private CachedTreeModel treeModel;
    private final TreeModelSource treeModelSource = new TreeModelSource();
    private Player player;
    private Listener listener;

    boolean init(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
	player = luwrain.getPlayer();
	if (player == null)
	    return false;
	treeModelSource.setPlaylists(player.loadRegistryPlaylists());
	treeModel = new CachedTreeModel(treeModelSource);
	return true;
    }

    TreeModel getTreeModel()
    {
	return treeModel;
    }

    void onPlaylistClick(Playlist playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	player.play(playlist);
    }

    void setListener(Area area)
    {
	NullCheck.notNull(area, "area");
	listener = new Listener(luwrain, area);
	player.addListener(listener);
    }

    void removeListener()
    {
	player.removeListener(listener);
    }
}
