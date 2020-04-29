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

import java.util.*;
import java.io.*;
import java.nio.charset.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.player.*;
import org.luwrain.template.*;

class App extends AppBase<Strings> implements Application, MonoApp, org.luwrain.player.Listener
{
    static final String DATA_DIR_NAME = "luwrain.player";
    static final String LOG_COMPONENT = "player";

    private final String[] args;
    private org.luwrain.player.Player player = null;
    private Conversations conv = null;
    private MainLayout layout = null;
    private Hooks hooks = null;
    final HashMap<String, TrackInfo> trackInfoMap = new HashMap<String, TrackInfo>();
    private Albums albums = null;

    App()
    {
	this(null);
    }

    App(String[] args)
    {
	super(Strings.NAME, Strings.class);
	this.args = args.clone();
    }

    @Override public boolean onAppInit() throws IOException
    {
	this.conv = new Conversations(this);
	this.albums = new Albums(getLuwrain());
	this.player = getLuwrain().getPlayer();
	if (player == null)
	    return false;
	this.player.addListener(this);
	this.hooks = new Hooks(getLuwrain());
	this.layout = new MainLayout(this, this.player);
	setAppName(getStrings().appName());
	return true;
    }

    Albums getAlbums()
    {
	return this.albums;
    }

    Hooks getHooks()
    {
	return this.hooks;
    }

    Conversations getConv()
    {
	return conv;
    }

    @Override public AreaLayout getDefaultAreaLayout()
    {
	return this.layout.getLayout();
    }

    @Override public void closeApp()
    {
	this.player.removeListener(this);
	super.closeApp();
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }

    @Override public void onNewPlaylist(org.luwrain.player.Playlist playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	getLuwrain().runUiSafely(()->{
		if (layout != null)
		    layout.onNewPlaylist(playlist);
	    });
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
