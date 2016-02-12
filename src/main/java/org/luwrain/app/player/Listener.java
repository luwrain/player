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

package org.luwrain.app.player;

import org.luwrain.core.*;
import org.luwrain.player.Playlist;

class Listener  implements org.luwrain.player.Listener
{
    private Luwrain luwrain;
    private PlayerArea area;

    Listener(Luwrain luwrain, PlayerArea area)
    {
	this.luwrain = luwrain;
	this.area = area;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(area, "area");
    }

    @Override public void onNewPlaylist(final Playlist playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	luwrain.runInMainThread(()->area.onNewPlaylist(playlist));
    }

    @Override public void onNewTrack(int trackNum)
    {
	luwrain.runInMainThread(()->area.onNewTrack(trackNum));
    }

    @Override public void onTrackTime(int sec)
    {
	luwrain.runInMainThread(()->area.onTrackTime(sec));
    }

    @Override public void onPlayerStop()
    {
	luwrain.runInMainThread(()->area.onStop());
    }
}
