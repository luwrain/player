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
    private Strings strings;
    private CachedTreeModel treeModel;
    private final TreeModelSource treeModelSource = new TreeModelSource();
    private Player player;
    private final FixedListModel playlistModel = new FixedListModel();
    private Listener listener;

    private Playlist currentPlaylist = null;
    private String[] currentPlaylistItems = new String[0];
    private int currentTrackNum = -1;

    boolean init(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	player = (Player)luwrain.getSharedObject(Player.SHARED_OBJECT_NAME);
	if (player == null)
	{
	    Log.error("player", "unable to obtain a reference to the player needed for PlayerApp");
	    return false;
	}
	treeModelSource.setPlaylists(player.loadRegistryPlaylists());
	treeModel = new CachedTreeModel(treeModelSource);
	currentPlaylist = player.getCurrentPlaylist();
	if (currentPlaylist != null)
	    onNewPlaylist(currentPlaylist);
	return true;
    }

    void playPlaylist(Playlist playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	player.play(playlist, 0, 0);
	onNewPlaylist(playlist);
    }

    void pauseResume()
    {
	player.pauseResume();
    }

    boolean playPlaylistItem(int index)
    {
	if (currentPlaylist == null)
	    return false;
	if (index < 0 || index >= currentPlaylistItems.length)
	    return false;
	player.play(currentPlaylist, index, 0);
	currentTrackNum = index;
	return true;
    }

    void stop()
    {
	player.stop();
    }

    void onJump(long offsetMsec)
    {
	player.jump(offsetMsec);
    }

    void onNewPlaylist(Playlist playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	currentPlaylist = playlist;
	currentPlaylistItems = playlist.getPlaylistItems();
	if (currentPlaylistItems == null)
	    currentPlaylistItems = new String[0];
	if (currentPlaylistItems.length > 0)
	{
	    playlistModel.setItems(currentPlaylistItems);
	    currentTrackNum = player.getCurrentTrackNum();
	    if (currentTrackNum < 0 || currentTrackNum >= currentPlaylistItems.length)
		currentTrackNum = -1;
	}
    }

void onNewTrack(int trackNum)
{
    if (trackNum < 0 || trackNum >= currentPlaylistItems.length)
	currentTrackNum = -1; else
	currentTrackNum = trackNum;
}

    void onStop()
    {
	currentTrackNum = -1;
    }

    Playlist getCurrentPlaylist()
    {
	return player.getCurrentPlaylist();
    }

    int getCurrentTrackNum()
    {
	return player.getCurrentTrackNum();
    }

    void fillPlaylistProperties(Playlist playlist, FormArea area)
    {
	NullCheck.notNull(playlist, "playlist");
	NullCheck.notNull(area, "area");
	area.addEdit("title", strings.playlistPropertiesAreaTitle(), playlist.getPlaylistTitle());
	if (playlist.isStreaming())
	{
	    final String[] items = playlist.getPlaylistItems();
	    if (items != null && items.length >= 1 && items[0] != null)
		area.addEdit("url", strings.playlistPropertiesAreaUrl(), items[0]); else
		area.addEdit("url", strings.playlistPropertiesAreaUrl(), "");
	}
    }

    void setListener(ControlArea area)
    {
	NullCheck.notNull(area, "area");
	listener = new Listener(luwrain, area);
	player.addListener(listener);
    }

    void removeListener()
    {
	player.removeListener(listener);
    }

    String getCurrentPlaylistTitle()
    {
	if (currentPlaylist == null)
	    return "";
	final String res = currentPlaylist.getPlaylistTitle();
	return res != null?res:"";
    }

    String getCurrentTrackTitle()
    {
	if (currentPlaylistItems.length < 1 || currentTrackNum < 0)
	    return "";
	final String res = currentPlaylistItems[currentTrackNum];
	return res != null?res:"";
    }

    ListArea.Model getPlaylistModel()
    {
	return playlistModel;
    }

    TreeArea.Model getTreeModel()
    {
	return treeModel;
    }

    static String getTimeStr(long sec)
    {
	final StringBuilder b = new StringBuilder();
	final long min = sec / 60;
	final long seconds = sec % 60;
	if (min < 10)
	    b.append("0" + min); else
	    b.append("" + min);
	b.append(":");
	if (seconds < 10)
	    b.append("0" + seconds); else
	    b.append("" + seconds);
	return new String(b);
    }
}
