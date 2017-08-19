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
    private final Luwrain luwrain;
    private final Strings strings;
    private Listener listener;
    final Player player;

    private org.luwrain.player.Playlist currentPlaylist = null;
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
	currentPlaylist = player.getCurrentPlaylist();
    }

    org.luwrain.player.Playlist getCurrentPlaylist()
    {
	return currentPlaylist;
    }

    void setNewCurrentPlaylist(ListArea area, org.luwrain.player.Playlist playlist)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(playlist, "playlist");
	if (currentPlaylist == playlist)
	    return;
	currentPlaylist = playlist;
	fillTrackInfoMap(area);
    }

    private void fillTrackInfoMap(ListArea area)
    {
	NullCheck.notNull(area, "area");
	final org.luwrain.player.Playlist playlist = currentPlaylist;
	if (playlist == null)
	    return;
	final HashMap<String, TrackInfo> map = new HashMap<String, TrackInfo>();
	new Thread(()->{
		for(String s: currentPlaylist.getPlaylistUrls())
		{
		    try {
			map.put(s, new TrackInfo(new URL(s)));
			luwrain.runInMainThread(()->area.redraw());
		    }
		    catch(IOException e)
		    {
			luwrain.crash(e);
		    }
		}
	}).start();
	trackInfoMap = map;
    }

    String getTrackTextAppearance(String trackUrl)
    {
	NullCheck.notNull(trackUrl, "trackUrl");
	return Utils.getTrackTextAppearanceWithMap(trackUrl, trackInfoMap);
    }

    void setListener(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	this.listener = listener;
	player.addListener(listener);
    }

    void removeListener()
    {
	player.removeListener(listener);
    }

    boolean playPlaylistItem(int index)
    {
	if (currentPlaylist == null)
	    return false;
	if (index < 0 || index >= getPlaylistLen())
	    return false;
	player.play(currentPlaylist, index, 0);
	return true;
    }

boolean onAddPlaylistWithBookmark()
    {
	return addPlaylist(true);
    }

boolean onAddPlaylistWithoutBookmark()
    {
	return addPlaylist(false);
    }

boolean onAddStreamingPlaylist()
    {
	return false;
    }

    private boolean addPlaylist(boolean hasBookmark)
    {
	/*
	final String title = Popups.simple(luwrain, strings.addPlaylistPopupName(), strings.addPlaylistPopupPrefix(), ""); 
	if (title == null)
	    return false;
	if (title.trim().isEmpty())
	{
	    luwrain.message(strings.playlistTitleMayNotBeEmpty(), Luwrain.MESSAGE_ERROR);
	    return false;
	}
	final Path path = Popups.path(luwrain, strings.choosePlaylistFilePopupName(), strings.choosePlaylistFilePopupPrefix(),
				      luwrain.getPathProperty("luwrain.dir.userhome"),
				      (pathToCheck)->{
					  if (Files.isDirectory(pathToCheck))
					  {
					      luwrain.message(strings.playlistFileMayNotBeDir(pathToCheck.toString()), Luwrain.MESSAGE_ERROR);
					      return false;
					  }
					  return true;
				      });
	RegistryPlaylist.add(luwrain.getRegistry(), title.trim(), path.toString(), false, hasBookmark);
	playlistsModel.setPlaylists(player.loadRegistryPlaylists());
	*/
		return true;
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
	if (isEmptyPlaylist())
	    return "";
	final String res = getPlaylistUrls()[player.getCurrentTrackNum()];
	return res != null?res:"";
    }

    boolean isEmptyPlaylist()
    {
	return currentPlaylist == null || currentPlaylist.getPlaylistUrls().length < 1;
    }

    String[] getPlaylistUrls()
    {
	if (currentPlaylist == null)
	    return new String[0];
	return currentPlaylist.getPlaylistUrls();
    }

    int getPlaylistLen()
    {
	if (currentPlaylist == null)
return 0;
return currentPlaylist.getPlaylistUrls().length;
    }

    PlaylistModel newPlaylistModel()
    {
	return new PlaylistModel();
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
}
