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

    private final Manager manager = new Manager();
    private final Vector<Listener> listeners = new Vector<Listener>();
    private State state = State.STOPPED;
    private MediaResourcePlayer currentPlayer = null;
    private Playlist playlist = null;
    private int currentTrackNum = 0;
    private long currentPos = 0;

    @Override public synchronized Result play(Playlist playlist, int startingTrackNum, long startingPosMsec, Set<Flags> flags)
    {
	NullCheck.notNull(playlist, "playlist");
	NullCheck.notNull(flags, "flags");
	if (playlist.getPlaylistUrls() == null)
	    return Result.INVALID_PLAYLIST;
	if (startingTrackNum < 0 || startingTrackNum >= playlist.getPlaylistUrls().length)
	    return Result.INVALID_PLAYLIST;
	stop();
	this.playlist = playlist;
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
	if (state == State.STOPPED)
	    return;
	//Current player may be null, this means we are paused
	if (currentPlayer != null)
	currentPlayer.stop();
	state = State.STOPPED;
	currentPlayer = null;
	playlist = null;
	currentTrackNum = 0;
	notifyListeners((listener)->listener.onPlayerStop());	currentPos = 0;
    }

    @Override public synchronized void pauseResume()
    {
	if (state == State.STOPPED)
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
	notifyListeners((listener)->listener.onTrackTime(playlist, currentTrackNum, currentPos));
	}
	}

    @Override public synchronized void jump(long offsetMsec)
    {
	if (state == State.STOPPED)
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
	notifyListeners((listener)->listener.onTrackTime(playlist, currentTrackNum, currentPos));
    }

	@Override public synchronized void nextTrack()
			  {
			      if (state == State.STOPPED)
	    return;
	final String[] items = playlist.getPlaylistUrls();
	if (items == null || currentTrackNum + 1 >= items.length)
	    return;
	if (currentPlayer != null)
	currentPlayer.stop();
	++currentTrackNum;
	currentPos = 0;
	runPlayer();
	notifyListeners((listener)->listener.onNewTrack(playlist, currentTrackNum));
	notifyListeners((listener)->listener.onTrackTime(playlist, currentTrackNum, 0));
			  }

    @Override public synchronized void prevTrack()
    {
	if (state == State.STOPPED)
	    return;
	final String[] items = playlist.getPlaylistUrls();
	if (items == null || currentTrackNum + 1 >= items.length)
	    return;
	if (currentPlayer != null)
	currentPlayer.stop();
	++currentTrackNum;
	currentPos = 0;
	runPlayer();
	notifyListeners((listener)->listener.onNewTrack(playlist, currentTrackNum));
	notifyListeners((listener)->listener.onTrackTime(playlist, currentTrackNum, 0));
    }

    @Override public synchronized void onPlayerTime(long msec)
    {
	if (state == State.STOPPED || currentPlayer == null)
	    return;
	if (currentPos <= msec && msec < currentPos + 50)
	    return;
	currentPos = msec;
	notifyListeners((listener)->listener.onTrackTime(playlist, currentTrackNum, currentPos));
    }

    @Override public synchronized void onPlayerFinish()
    {
	if (state == State.STOPPED || currentPlayer == null)
	    return;
	final String[] items = playlist.getPlaylistUrls();
	if (items != null && currentTrackNum + 1 < items.length)
	    nextTrack(); else
	stop();
    }

    @Override public synchronized boolean hasPlaylist()
    {
	return playlist != null;
    }

    @Override public synchronized Playlist getCurrentPlaylist()
    {
	return playlist;
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
	final String[] items = playlist.getPlaylistUrls();
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
