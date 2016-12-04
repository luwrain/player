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

package org.luwrain.player;

import java.util.*;
import java.net.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.player.backends.*;

class PlayerImpl implements Player, org.luwrain.player.backends.Listener
{
    private final Registry registry;
    private final Vector<Listener> listeners = new Vector<Listener>();

    private Playlist currentPlaylist = null;
    private BackEnd currentPlayer = null;
    private int currentTrackNum = -1;
    private long currentPos = 0;

    PlayerImpl(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	this.registry = registry;
    }



    @Override public synchronized void play(Playlist playlist,
			   int startingTrackNum, long startingPosMsec)
    {
	NullCheck.notNull(playlist, "playlist");
	if (playlist.getPlaylistItems() == null || playlist.getPlaylistItems().length < 1)
	    return;
	if (currentPlaylist != null)
	    stop();
	currentPlaylist = playlist;
	currentTrackNum = startingTrackNum;
	currentPos = startingPosMsec;
	final Task task = createTask();
	if (task == null)
	    return;
	Log.debug("player", "starting pos is " + currentPos);
	task.setStartPosMsec(currentPos);
	currentPlayer = BackEnd.createBackEnd(this, "jlayer");
	//currentPlayer = BackEnd.createBackEnd(this, "SoundPlayer");
	//	currentPlayer = BackEnd.createBackEnd(this, "OggPlayer");
	for(Listener l: listeners)
	{
	    l.onNewPlaylist(playlist);
	    l.onNewTrack(playlist, currentTrackNum);
	}
	currentPlayer.play(task);
    }

    @Override public synchronized void stop()
    {
	if (currentPlayer == null)
	    return;
	currentPlayer.stop();
	notifyListeners((listener)->listener.onPlayerStop());
	currentPlayer = null;
	currentPlaylist = null;
    }

    @Override public synchronized void pauseResume()
    {
	if (currentPlayer != null)
	{
	    //pausing
	    currentPlayer.stop();
	    currentPlayer = null;
	    return;
	}
	if (currentPlaylist == null)
	    return;
	//resuming
	final Task task = createTask();
	if (task == null)
	    return;
	task.setStartPosMsec(currentPos);
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, currentPos));
	currentPlayer = BackEnd.createBackEnd(this, "jlayer");
	currentPlayer.play(task);
    }

    @Override public synchronized void jump(long offsetMsec)
    {
	if (currentPlaylist == null || currentPlaylist.isStreaming())
	    return;
	if (currentPlayer != null)
	    {
			currentPlayer.stop();
	currentPlayer = null;
	    }
	final Task task = createTask();
	if (task == null)
	    return;
	currentPos += offsetMsec;
	if (currentPos < 0)
	    currentPos = 0;
	task.setStartPosMsec(currentPos);
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, currentPos));
		currentPlayer = BackEnd.createBackEnd(this, "jlayer");
	currentPlayer.play(task); 
    }

	@Override public synchronized void nextTrack()
			  {
	if (currentPlaylist == null || currentPlaylist.isStreaming())
	    return;
	if (currentTrackNum + 1 >= currentPlaylist.getPlaylistItems().length)
	    return;
	++currentTrackNum;
	if (currentPlayer != null)
	currentPlayer.stop();
	final Task task = createTask();
	if (task == null)
	    return;
	currentPlayer = BackEnd.createBackEnd(this, "jlayer");
	currentPos = 0;
	notifyListeners((listener)->listener.onNewTrack(currentPlaylist, currentTrackNum));
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, 0));
	currentPlayer.play(task); 
			  }

    @Override public synchronized void prevTrack()
    {
	if (currentPlaylist == null || currentPlaylist.isStreaming())
	    return;
	if (currentTrackNum <= 0)
	    return;
	--currentTrackNum;
	if (currentPlayer != null)
	currentPlayer.stop();
	final Task task = createTask();
	if (task == null)
	    return;
	currentPlayer = BackEnd.createBackEnd(this, "jlayer");
	currentPos = 0;
	notifyListeners((listener)->listener.onNewTrack(currentPlaylist, currentTrackNum));
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, 0));
	currentPlayer.play(task); 
    }

    @Override public synchronized Playlist getCurrentPlaylist()
    {
	return currentPlaylist;
    }

    @Override public synchronized int getCurrentTrackNum()
    {
	return currentTrackNum;
    } 

    @Override public synchronized void onPlayerBackEndTime(long msec)
    {
	if (currentPlaylist == null || currentPlayer == null)
	    return;
	if (currentPos <= msec && msec < currentPos + 50)
	    return;
	currentPos = msec;
	notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, currentPos));
	currentPlaylist.updateStartingPos(currentTrackNum, currentPos);
    }

    @Override public synchronized void onPlayerBackEndFinish()
    {
	if (currentPlaylist == null)
	    return;
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
	for(Listener l: listeners)
	    notification.notify(l);
    }

    private Task createTask()
    {
	    final String url = currentPlaylist.getPlaylistItems()[currentTrackNum];
	try {
	    Log.debug("player", "creating task for " + url);
	    Task task = new Task(new URL(url));
	    if (task.url().getProtocol().equals("file"))
	    {
		task = new Task(Paths.get(task.url().getFile()));
	    }
	    return task;
	}
	catch (Exception e)
	{
	    Log.error("player", "unable to create a task for " + url + ":" + e.getClass().getName() + ":" + e.getMessage());
	    e.printStackTrace();
	    return null;
	}
    }

    private interface ListenerNotification
    {
	void notify(Listener listener);
    }

    @Override public Playlist[] loadRegistryPlaylists()
    {
	final String dir = "/org/luwrain/player/playlists";//FIXME:
	final String[] dirs = registry.getDirectories(dir); 
	final LinkedList<Playlist> res = new LinkedList<Playlist>();
	for(String s: dirs)
	{
	    final String path = Registry.join(dir, s);
	    final RegistryPlaylist playlist = new RegistryPlaylist(registry);
	    if (playlist.init(path))
		res.add(playlist);
	}
	return res.toArray(new Playlist[res.size()]);
    }


}
