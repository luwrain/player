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

package org.luwrain.player;

import java.util.*;
import java.net.*;
import java.nio.file.*;

import org.luwrain.base.*;
import org.luwrain.core.*;

class PlayerImpl implements Player, MediaResourcePlayer.Listener
{
    static final String LOG_COMPONENT = "player";

    private final Registry registry;
    private final Vector<Listener> listeners = new Vector<Listener>();

    private Playlist currentPlaylist = null;
    private final Manager manager = new Manager();
    private MediaResourcePlayer currentPlayer = null;
    private int currentTrackNum = 0;
    private long currentPos = 0;

    PlayerImpl(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	this.registry = registry;
    }

    @Override public synchronized Result play(Playlist playlist,
			   int startingTrackNum, long startingPosMsec)
    {
	NullCheck.notNull(playlist, "playlist");
	if (playlist.getPlaylistUrls() == null)
	    return Result.INVALID_PLAYLIST;
	if (startingTrackNum < 0 || startingTrackNum >= playlist.getPlaylistUrls().length)
	    return Result.INVALID_PLAYLIST;
	    stop();
	currentPlaylist = playlist;
	currentTrackNum = startingTrackNum;
	currentPos = startingPosMsec;
	final Result res = runPlayer();
	if (res != Result.OK)
	    return res;
	notifyListeners((l)->l.onNewPlaylist(playlist));
    notifyListeners((l)->l.onNewTrack(playlist, currentTrackNum));
    return Result.OK;
    }

    @Override public synchronized void stop()
    {
	if (currentPlaylist == null)
	    return;
	//Current player may be null, this means we are paused
	if (currentPlayer != null)
	currentPlayer.stop();
	notifyListeners((listener)->listener.onPlayerStop());
	currentPlayer = null;
	currentPlaylist = null;
	currentTrackNum = 0;
	currentPos = 0;
    }

    @Override public synchronized void pauseResume()
    {
	if (currentPlaylist == null)
	    return;
	if (currentPlayer != null)
	{
	    //pausing
	    currentPlayer.stop();
	    currentPlayer = null;
	} else
	{
	//resuming
	if (runPlayer() != Result.OK)
	    return;
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, currentPos));
	}
	}

    @Override public synchronized void jump(long offsetMsec)
    {
	if (currentPlaylist == null/* || currentPlaylist.isStreaming()*/)
	    return;
	if (currentPlayer != null)
	    {
			currentPlayer.stop();
	currentPlayer = null;
	    }
	currentPos += offsetMsec;
	if (currentPos < 0)
	    currentPos = 0;
	runPlayer();
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, currentPos));
    }

	@Override public synchronized void nextTrack()
			  {
			      if (currentPlaylist == null/* || currentPlaylist.isStreaming()*/)
	    return;
	final String[] items = currentPlaylist.getPlaylistUrls();
	if (items == null || currentTrackNum + 1 >= items.length)
	    return;
	if (currentPlayer != null)
	currentPlayer.stop();
	++currentTrackNum;
	currentPos = 0;
	runPlayer();
	notifyListeners((listener)->listener.onNewTrack(currentPlaylist, currentTrackNum));
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, 0));
			  }

    @Override public synchronized void prevTrack()
    {
	if (currentPlaylist == null/* || currentPlaylist.isStreaming()*/)
	    return;
	final String[] items = currentPlaylist.getPlaylistUrls();
	if (items == null || currentTrackNum + 1 >= items.length)
	    return;
	if (currentPlayer != null)
	currentPlayer.stop();
	++currentTrackNum;
	currentPos = 0;
	runPlayer();
	notifyListeners((listener)->listener.onNewTrack(currentPlaylist, currentTrackNum));
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, 0));
    }

    @Override public synchronized void onPlayerTime(long msec)
    {
	if (currentPlaylist == null || currentPlayer == null)
	    return;
	if (currentPos <= msec && msec < currentPos + 50)
	    return;
	currentPos = msec;
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, currentPos));
    }

    @Override public synchronized void onPlayerFinish()
    {
	if (currentPlaylist == null || currentPlayer == null)
	    return;
	final String[] items = currentPlaylist.getPlaylistUrls();
	if (items != null && currentTrackNum + 1 < items.length)
	    nextTrack(); else
	stop();
    }

    @Override public synchronized boolean hasPlaylist()
    {
	return currentPlaylist != null;
    }

    @Override public synchronized Playlist getCurrentPlaylist()
    {
	return currentPlaylist;
    }

    @Override public synchronized int getCurrentTrackNum()
    {
	return currentTrackNum;
    } 

    @Override public synchronized void addListener(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	for(Listener l: listeners)
	    if (l == listener)
		return;
	listeners.add(listener);
    }

    @Override public synchronized void removeListener(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	for(int i = 0;i < listeners.size();++i)
	    if (listeners.get(i) == listener)
	    {
		listeners.remove(i);
		break;
	    }
    }

    private void notifyListeners(ListenerNotification notification)
    {
	NullCheck.notNull(notification, "notification");
	for(Listener l: listeners)
	    notification.notify(l);
    }

    private Task createTask()
    {
	final String[] items = currentPlaylist.getPlaylistUrls();
	if (items == null || currentTrackNum < 0 || currentTrackNum >= items.length)
	    return null;
	for(int i = 0;i < items.length;++i)
	    if (items[i] == null)
		return null;
	    final String url = items[currentTrackNum];
	try {
	    return new Task(new URL(url), currentPos);
	}
	catch (Exception e)
	{
	    Log.error("player", "unable to create the URL object for " + url + ":" + e.getClass().getName() + ":" + e.getMessage());
	    return null;
	}
    }

    private Result runPlayer()
    {
	final Task task = createTask();
	if (task == null)
	    return Result.INVALID_PLAYLIST;
	Log.debug("player", "starting playing " + task.url.toString() + " from " + task.startPosMsec);
	final String fileName = task.url.getFile();
	final MediaResourcePlayer p = manager.play(this, task);
	if (p == null)
	{
	    Log.error("player", "unable to play due to unsupported format:" + task.url.toString());
	    return Result.UNSUPPORTED_FORMAT_STARTING_TRACK;
	}
	currentPlayer = p;
	return Result.OK;
    }

    private interface ListenerNotification
    {
	void notify(Listener listener);
    }
}
