/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>

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

import org.luwrain.core.*;
import org.luwrain.player.*;

final class Listener  implements org.luwrain.player.Listener
    {
	//	private final ListArea playlistArea;
	//	private final ControlArea controlArea;
	
	Listener(App app)
	{
	}

	@Override public void onNewPlaylist(org.luwrain.player.Playlist playlist)
	{
	    NullCheck.notNull(playlist, "playlist");
	    /*
	    luwrain.runUiSafely(()->{
		    setNewCurrentPlaylist(playlistArea, playlist);
		    if (Utils.isStreamingPlaylist(playlist))
			controlArea.setMode(ControlArea.Mode.PLAYING_STREAMING); else
			controlArea.setMode(ControlArea.Mode.PLAYING);
		    //FIXME:controlArea.setPlaylistTitle(playlist.getPlaylistTitle());
		    controlArea.setTrackTitle("");
		    controlArea.setTrackTime(0);
		});
	    */
	}
	
	@Override public void onNewTrack(org.luwrain.player.Playlist playlist, int trackNum)
	{
	    /*
	    luwrain.runUiSafely(()->{
		    controlArea.setTrackTitle("fixme1");
		    controlArea.setTrackTime(0);
		});
	    */
	}

	@Override public void onTrackTime(org.luwrain.player.Playlist playlist, int trackNum, long msec)
	{
	    /*
	    luwrain.runUiSafely(()->controlArea.setTrackTime(msec));
	    */
	}
	
	@Override public void onNewState(org.luwrain.player.Playlist playlist, Player.State state)
	{
	    NullCheck.notNull(playlist, "playlist");
	    NullCheck.notNull(state, "state");
	    /*
	    luwrain.runUiSafely(()->{
		    switch(state)
		    {
		    case STOPPED:
			controlArea.setMode(ControlArea.Mode.STOPPED);
			break;
		    case PAUSED:
			if (Utils.isStreamingPlaylist(playlist))
			    controlArea.setMode(ControlArea.Mode.PLAYING_STREAMING); else
			    controlArea.setMode(ControlArea.Mode.PAUSED);
			break;
		    case PLAYING:
			if (Utils.isStreamingPlaylist(playlist))
			    controlArea.setMode(ControlArea.Mode.PLAYING_STREAMING); else
			    controlArea.setMode(ControlArea.Mode.PLAYING);
			break;
		    }
		});
	    */
	}
	
	@Override public void onPlayingError(org.luwrain.player.Playlist playlist, Exception e)
	{
	}
    }
