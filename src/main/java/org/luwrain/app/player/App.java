/*
   Copyright 2012-2022 Michael Pozhidaev <msp@luwrain.org>

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
import java.util.concurrent.*;
import java.io.*;
import java.net.*;
//import java.nio.charset.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.player.*;
import org.luwrain.app.base.*;

class App extends AppBase<Strings> implements Application, MonoApp, org.luwrain.player.Listener
{
    static final String DATA_DIR_NAME = "luwrain.player";
    static final String LOG_COMPONENT = "player";

    private final String[] args;
    final Starting starting = new Starting(this);
    private org.luwrain.player.Player player = null;
    private Conversations conv = null;
    private MainLayout layout = null;
    private Albums albums = null;
    private Hooks hooks = null;
    final Map<String, TrackInfo> trackInfoMap = new ConcurrentHashMap<>();


    App()
    {
	this(null);
    }

    App(String[] args)
    {
	super(Strings.NAME, Strings.class, "luwrain.player");
	this.args = args.clone();
    }

    @Override public AreaLayout onAppInit() throws IOException
    {
	this.conv = new Conversations(this);
	this.albums = new Albums(getLuwrain());
	this.player = getLuwrain().getPlayer();
	if (player == null)
	    return null;
	this.player.addListener(this);
	this.hooks = new Hooks(getLuwrain());
	this.layout = new MainLayout(this, this.player);
	setAppName(getStrings().appName());
	return layout.getAreaLayout();
    }

    void fillTrackInfoMap(Playlist playlist, ListArea listArea)
    {
	NullCheck.notNull(playlist, "playlist");
	NullCheck.notNull(listArea, "listArea");
	final List<String> tracks = new ArrayList<>();
	int count = playlist.getTrackCount();
	for(int i = 0;i < count;i++)
	    tracks.add(playlist.getTrackUrl(i));
	getLuwrain().executeBkg(new FutureTask<>(()->{
		    for(String s: tracks)
		    {
			try {
			    trackInfoMap.put(s, new TrackInfo(new URL(s)));
			    getLuwrain().runUiSafely(()->listArea.refresh());
			}
			catch(IOException e)
			{
			    Log.warning(LOG_COMPONENT, "unable to get track info for " + s +  ":" + e.getClass().getName() + ":" + e.getMessage());
			}
		    }
	}, null));
    }

    Albums getAlbums()
    {
	return this.albums;
    }

    @Override public boolean onEscape()
    {
	closeApp();
	return true;
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

    Player getPlayer()
    {
	return this.player;
    }

        Hooks getHooks()
    {
	return this.hooks;
    }

    Conversations getConv()
    {
	return conv;
    }
}
