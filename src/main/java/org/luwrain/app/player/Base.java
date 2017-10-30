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
import java.io.*;
import java.nio.*;
import java.net.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.nio.channels.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.player.*;
import org.luwrain.popups.Popups;
import org.luwrain.util.*;

class Base
{
    static final String LOG_COMPONENT = "player";

    private final Luwrain luwrain;
    private final Strings strings;
    private Listener listener;
    final Player player;

    private HashMap<String, TrackInfo> trackInfoMap = new HashMap<String, TrackInfo>();
    private final RegistryPlaylists playlists;
    final PlaylistsModel playlistsModel;

    Base(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	playlists = new RegistryPlaylists(this, luwrain.getRegistry());
	playlistsModel = new PlaylistsModel(strings);
	player = (Player)luwrain.getSharedObject(Player.SHARED_OBJECT_NAME);
	if (player == null)
	    return;
	playlistsModel.setPlaylists(playlists.loadRegistryPlaylists());
    }

    String getTrackTextAppearance(String trackUrl)
    {
	NullCheck.notNull(trackUrl, "trackUrl");
	return Utils.getTrackTextAppearanceWithMap(trackUrl, trackInfoMap);
    }

    void setListener(ListArea playlistArea, ControlArea controlArea)
    {
	NullCheck.notNull(playlistArea, "playlistArea");
	NullCheck.notNull(controlArea, "controlArea");
	this.listener = new Listener(playlistArea, controlArea);
	player.addListener(listener);
    }

    void removeListener()
    {
	player.removeListener(listener);
    }

    boolean playPlaylistItem(int index)
    {
	if (!player.hasPlaylist())
	    return false;
	if (index < 0 || index >= getPlaylistLen())
	    return false;
	player.play(player.getPlaylist(), index, 0, org.luwrain.player.Player.DEFAULT_FLAGS);
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
	return player.getPlaylist().getPlaylistTitle();
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
	return !player.hasPlaylist() || player.getPlaylist().getPlaylistUrls().length < 1;
    }

    String[] getPlaylistUrls()
    {
	if (!player.hasPlaylist())
	    return new String[0];
	return player.getPlaylist().getPlaylistUrls();
    }

    int getPlaylistLen()
    {
	if (!player.hasPlaylist())
	    return 0;
	return player.getPlaylist().getPlaylistUrls().length;
    }

    PlaylistModel newPlaylistModel()
    {
	return new PlaylistModel();
    }

    private void setNewCurrentPlaylist(ListArea area, org.luwrain.player.Playlist playlist)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(playlist, "playlist");
	/*
	if (player.getCurrentPlaylist() == playlist)
	    return;
	*/
	fillTrackInfoMap(area);
    }

    private void fillTrackInfoMap(ListArea area)
    {
	NullCheck.notNull(area, "area");
	final org.luwrain.player.Playlist playlist = player.getPlaylist();
	if (playlist == null)
	    return;
	final HashMap<String, TrackInfo> map = new HashMap<String, TrackInfo>();
	new Thread(()->{
		for(String s: playlist.getPlaylistUrls())
		{
		    try {
			map.put(s, new TrackInfo(new URL(s)));
			luwrain.runInMainThread(()->area.redraw());
		    }
		    catch(IOException e)
		    {
			//Silently doing nothing here
		    }
		}
	}).start();
	trackInfoMap = map;
    }

    class PlaylistModel implements EditableListArea.EditableModel
    {
	@Override public boolean clearList()
	{
	    return false;
	}

	@Override public boolean addToList(int pos,Clipboard clipboard)
	{
	    return false;
	}

	@Override public boolean removeFromList(int index)
	{
	    return false;
	}

	@Override public void refresh()
	{
	} 

	@Override public Object getItem(int index)
	{
	    if (index < 0 || index >= getPlaylistLen())
		return "";
	    return new PlaylistItem(getPlaylistUrls()[index], getTrackTextAppearance(getPlaylistUrls()[index]));
	}

	@Override public int getItemCount()
	{
	    return getPlaylistLen();
	}
    }

    class Listener  implements org.luwrain.player.Listener
    {
	private final ListArea playlistArea;
	private final ControlArea controlArea;

	Listener(ListArea playlistArea, ControlArea controlArea)
	{
	    NullCheck.notNull(playlistArea, "playlistArea");
	    NullCheck.notNull(controlArea, "controlArea");
	    this.playlistArea = playlistArea;
	    this.controlArea = controlArea;;
	}

	@Override public void onNewPlaylist(org.luwrain.player.Playlist playlist)
	{
	    NullCheck.notNull(playlist, "playlist");
	    luwrain.runInMainThread(()->{
		    final org.luwrain.player.Playlist.ExtInfo extInfo = playlist.getExtInfo();
		    		    		    setNewCurrentPlaylist(playlistArea, playlist);
						    if (extInfo != null && extInfo.getProp("streaming").equals("yes"))
													    		    controlArea.setMode(ControlArea.Mode.PLAYING_STREAMING); else
						    						    		    controlArea.setMode(ControlArea.Mode.PLAYING);
		    controlArea.setPlaylistTitle(playlist.getPlaylistTitle());
		    controlArea.setTrackTitle("");
		    controlArea.setTrackTime(0);
		});
	}

	@Override public void onNewTrack(org.luwrain.player.Playlist playlist, int trackNum)
	{
	    luwrain.runInMainThread(()->{
		    controlArea.setTrackTitle("fixme1");
		    controlArea.setTrackTime(0);
				    });
	}

	@Override public void onTrackTime(org.luwrain.player.Playlist playlist, int trackNum, long msec)
	{
	    luwrain.runInMainThread(()->controlArea.setTrackTime(msec));
	}

	@Override public void onNewState(org.luwrain.player.Playlist playlist, Player.State state)
	{
	    NullCheck.notNull(playlist, "playlist");
	    NullCheck.notNull(state, "state");
	    luwrain.runInMainThread(()->{
		    switch(state)
		    {
		    case STOPPED:
			controlArea.setMode(ControlArea.Mode.STOPPED);
			break;
		    case PAUSED:
			controlArea.setMode(ControlArea.Mode.PAUSED);
		    case PLAYING:
			//FIXME:streaming
			controlArea.setMode(ControlArea.Mode.PLAYING);
		    }
		});
	}

	@Override public void onPlayingError(org.luwrain.player.Playlist playlist, Exception e)
	{
	}
    }
}
