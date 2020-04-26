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

class App extends AppBase<Strings> implements Application, MonoApp
{
    static final String LOG_COMPONENT = "player";

    private final String[] args;
private org.luwrain.player.Player player = null;
        private Listener listener = null;
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

    @Override public boolean onAppInit()
    {
	this.albums = new Albums(getLuwrain().getRegistry());
	player = getLuwrain().getPlayer();
	if (player == null)
	    return false;

	setAppName(getStrings().appName());
	return true;
    }

    Albums getAlbums()
    {
	return this.albums;
    }





    @Override public AreaLayout getDefaultAreaLayout()
    {
	return null;//layout.getLayout();
    }

    @Override public void closeApp()
    {
	this.player.removeListener(this.listener);
	super.closeApp();
    }


    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }


    
}
    /*

    boolean playPlaylistItem(int index)
    {
	if (!player.hasPlaylist())
	    return false;
	if (index < 0 || index >= getPlaylistLen())
	    return false;
	player.play(player.getPlaylist(), index, 0, org.luwrain.player.Player.DEFAULT_FLAGS, null);
	return true;
    }

    boolean isStreamingPlaylist()
    {
	return false;
    }

    boolean onAddStreamingPlaylist()
    {
	return false;
    }

    String getCurrentPlaylistTitle()
    {
	if (!player.hasPlaylist())
	    return "";
	//	return player.getPlaylist().getPlaylistTitle();
	return "FIXME:Playlist title";
    }

    String getCurrentTrackTitle()
    {
	if (isEmptyPlaylist())
	    return "";
	final String res = getPlaylistUrls()[player.getTrackNum()];
	return res != null?res:"";
    }

    boolean isEmptyPlaylist()
    {
	return !player.hasPlaylist();
    }

    String[] getPlaylistUrls()
    {
	if (!player.hasPlaylist())
	    return new String[0];
	return player.getPlaylist().getTracks();
    }

    int getPlaylistLen()
    {
	if (!player.hasPlaylist())
	    return 0;
	return player.getPlaylist().getTrackCount();
    }

    private void setNewCurrentPlaylist(ListArea area, org.luwrain.player.Playlist playlist)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(playlist, "playlist");

    */
