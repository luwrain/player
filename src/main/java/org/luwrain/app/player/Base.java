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
    final Player player;
    private Listener listener;

    private org.luwrain.player.Playlist currentPlaylist = null;
    private String[] currentPlaylistItems = new String[0];
    private HashMap<String, TrackInfo> trackInfoMap = new HashMap<String, TrackInfo>();
    private int currentTrackNum = -1;

    private final RegistryPlaylists playlists;
    final PlaylistsModel playlistsModel;

    private Playlist playlistInEdit = null;

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

    void setNewCurrentPlaylist(Area area, org.luwrain.player.Playlist playlist)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(playlist, "playlist");
	if (currentPlaylist == playlist)
	    return;
	currentPlaylist = playlist;
	currentPlaylistItems = playlist.getPlaylistUrls();
	if (currentPlaylistItems == null)
	    currentPlaylistItems = new String[0];
	if (currentPlaylistItems.length > 0)
	{
	    currentTrackNum = player.getCurrentTrackNum();
	    if (currentTrackNum < 0 || currentTrackNum >= currentPlaylistItems.length)
		currentTrackNum = -1;
	}
	fillTrackInfoMap(area);
    }

    private void fillTrackInfoMap(Area area)
    {
	NullCheck.notNull(area, "area");
	final HashMap<String, TrackInfo> map = new HashMap<String, TrackInfo>();
	new Thread(()->{
		for(String s: currentPlaylistItems)
		{
		    try {
			map.put(s, new TrackInfo(new URL(s)));
			luwrain.runInMainThread(()->luwrain.onAreaNewContent(area));
		    }
		    catch(IOException e)
		    {
			Log.warning("player", "unable to read tags for " + s + ":" + e.getClass().getName() + ":" + e.getMessage());
		    }
		}
	}).start();
	trackInfoMap = map;
    }

    String getTrackTextAppearance(String trackUrl)
    {
	NullCheck.notNull(trackUrl, "trackUrl");
	return getTrackTextAppearanceWithMap(trackUrl, trackInfoMap);
    }

    static String getTrackTextAppearanceWithMap(String trackUrl, Map<String, TrackInfo> map)
    {
	NullCheck.notNull(trackUrl, "trackUrl");
	NullCheck.notNull(map, "map");
	final String tagText = getTrackTagText(trackUrl, map);
	if (tagText != null)
	    return tagText;
String name = "";
	try {
	    final File f = Urls.toFile(new URL(trackUrl));
	    if (f == null)
		name = trackUrl; else
		name = f.getName();
	}
	catch(MalformedURLException e)
	{
	    name = trackUrl;
    }
	return name;
    }

    static private String getTrackTagText(String trackUrl, Map<String, TrackInfo> map)
    {
	NullCheck.notNull(trackUrl, "trackUrl");
	NullCheck.notNull(map, "map");
	if (!map.containsKey(trackUrl))
	    return null;
	final StringBuilder b = new StringBuilder();
	final TrackInfo info = map.get(trackUrl);
	if (info == null)
	    return null;
	if (info.artist.trim().isEmpty() && info.title.trim().isEmpty())
	    return null;
	if (!info.artist.trim().isEmpty())
	    b.append(info.artist.trim());
	if (!info.artist.trim().isEmpty() && !info.title.trim().isEmpty())
	    b.append(" - ");
	    if (!info.title.trim().isEmpty())
	b.append(info.title);
	return new String(b);
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

    boolean prevTrack()
    {
	player.prevTrack();
	currentTrackNum = player.getCurrentTrackNum();
	return true;
    }

    boolean nextTrack()
    {
	player.nextTrack();
	currentTrackNum = player.getCurrentTrackNum();
	return true;
    }

    void fillPlaylistProperties(Playlist playlist, FormArea area)
    {
	NullCheck.notNull(playlist, "playlist");
	NullCheck.notNull(area, "area");
	/*
	area.clear();
	area.addEdit("title", strings.playlistPropertiesAreaTitle(), playlist.getPlaylistTitle());
	area.addEdit("url", strings.playlistPropertiesAreaUrl(), playlist.getPlaylistUrl());
	playlistInEdit = playlist;
	*/
    }

    void savePlaylistProperties(FormArea area)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(playlistInEdit, "playlistInEdit");
	/*
	playlistInEdit.setPlaylistTitle(area.getEnteredText("title"));
	playlistInEdit.setPlaylistUrl(area.getEnteredText("url"));
	playlistInEdit = null;
	playlistsModel.setPlaylists(player.loadRegistryPlaylists());
	*/
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
	if (currentPlaylistItems.length < 1 || currentTrackNum < 0)
	    return "";
	final String res = currentPlaylistItems[currentTrackNum];
	return res != null?res:"";
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
	    if (currentPlaylistItems == null || index >= currentPlaylistItems.length)
		return "";
	    return new PlaylistItem(currentPlaylistItems[index], getTrackTextAppearance(currentPlaylistItems[index]));
	}

	@Override public int getItemCount()
	{
	    return currentPlaylistItems != null?currentPlaylistItems.length:0;
	}
    }
}
