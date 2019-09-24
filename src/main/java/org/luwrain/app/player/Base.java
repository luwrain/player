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
    final org.luwrain.player.Player player;

    private HashMap<String, TrackInfo> trackInfoMap = new HashMap<String, TrackInfo>();
    private final Albums albums;
    private final ListUtils.FixedModel albumsModel;

    Base(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	this.albums = new Albums(this, luwrain.getRegistry());
	this.albumsModel = new ListUtils.FixedModel(albums.loadRegistryAlbums());
	player = luwrain.getPlayer();
	if (player == null)
	    return;
    }

    void updateAlbums()
    {
	this.albumsModel.setItems(albums.loadRegistryAlbums());
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
		for(String s: playlist.getTracks())
		{
		    try {
			map.put(s, new TrackInfo(new URL(s)));
			luwrain.runUiSafely(()->area.redraw());
		    }
		    catch(IOException e)
		    {
			//Silently doing nothing here
		    }
		}
	}).start();
	trackInfoMap = map;
    }

    ListArea.Params createAlbumsListParams(ListArea.ClickHandler clickHandler)
    {
	NullCheck.notNull(clickHandler, "clickHandler");
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.model = albumsModel;
	params.name = strings.treeAreaName();
	params.clickHandler = clickHandler;
	params.appearance = new ListUtils.DoubleLevelAppearance(params.context){
		@Override public boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    return (item instanceof String);
		}
	    };
		params.transition = new ListUtils.DoubleLevelTransition(params.model){
		@Override public boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    return (item instanceof String);
		}
	    };
	return params;
    }

    ListArea.Params createPlaylistParams(ListArea.ClickHandler clickHandler)
    {
	NullCheck.notNull(clickHandler, "clickHandler");
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.model = new PlaylistModel();
	params.appearance = new ListUtils.DefaultAppearance(params.context);//new PlaylistAppearance(luwrain, base);
	params.clickHandler = clickHandler;
	params.name = strings.playlistAreaName();
	return params;
    }


    private class Listener  implements org.luwrain.player.Listener
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
	    luwrain.runUiSafely(()->{
		    setNewCurrentPlaylist(playlistArea, playlist);
		    if (Utils.isStreamingPlaylist(playlist))
			controlArea.setMode(ControlArea.Mode.PLAYING_STREAMING); else
			controlArea.setMode(ControlArea.Mode.PLAYING);
		    //FIXME:controlArea.setPlaylistTitle(playlist.getPlaylistTitle());
		    controlArea.setTrackTitle("");
		    controlArea.setTrackTime(0);
		});
	}
	@Override public void onNewTrack(org.luwrain.player.Playlist playlist, int trackNum)
	{
	    luwrain.runUiSafely(()->{
		    controlArea.setTrackTitle("fixme1");
		    controlArea.setTrackTime(0);
		});
	}
	@Override public void onTrackTime(org.luwrain.player.Playlist playlist, int trackNum, long msec)
	{
	    luwrain.runUiSafely(()->controlArea.setTrackTime(msec));
	}
	@Override public void onNewState(org.luwrain.player.Playlist playlist, Player.State state)
	{
	    NullCheck.notNull(playlist, "playlist");
	    NullCheck.notNull(state, "state");
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
	}
	@Override public void onPlayingError(org.luwrain.player.Playlist playlist, Exception e)
	{
	}
    }

    private class PlaylistModel implements EditableListArea.EditableModel
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
	    return new Item(getPlaylistUrls()[index], getTrackTextAppearance(getPlaylistUrls()[index]));
	}
	@Override public int getItemCount()
	{
	    return getPlaylistLen();
	}
    }

    static private final class Item
    {
	final String url;
	final String title;
	Item(String url, String title)
	{
	    NullCheck.notNull(url, "url");
	    NullCheck.notNull(title, "title");
	    this.url = url;
	    this.title = title;
	}
	@Override public String toString()
	{
	    return title;
	}
	@Override public boolean equals(Object o)
	{
	    if (o == null || !(o instanceof Item))
		return false;
	    return url.equals(((Item)o).url);
	}
    }
}
