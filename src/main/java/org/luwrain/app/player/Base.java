
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
    final ListUtils.FixedModel playlistModel = new ListUtils.FixedModel();

    private Playlist playlistInEdit = null;

    Base(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	playlists = new RegistryPlaylists(luwrain.getRegistry());
	playlistsModel = new PlaylistsModel(strings);
	player = (Player)luwrain.getSharedObject(Player.SHARED_OBJECT_NAME);
	if (player == null)
	    return;
	playlistsModel.setPlaylists(playlists.loadRegistryPlaylists());
	currentPlaylist = player.getCurrentPlaylist();
	if (currentPlaylist != null)
	    onNewPlaylist(currentPlaylist);
    }

    void onNewPlaylist(org.luwrain.player.Playlist playlist)
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
	fillTrackInfoMap();
    }

    private void fillTrackInfoMap()
    {
	for(String s: currentPlaylistItems)
	{
	    try {
		new TrackInfo(new URL(s));
	    }
	    catch(IOException e)
	    {
		Log.error("player", "unable to read tags for " + s + ":" + e.getClass().getName() + ":" + e.getMessage());
	    }
	}
    }

    String getTrackTextAppearance(String trackUrl)
    {
	return "fixme";
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
}
