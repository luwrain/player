/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import java.net.*;
import java.nio.file.*;

import org.luwrain.base.*;
import org.luwrain.core.*;

final class Dispatcher implements org.luwrain.player.Player, MediaResourcePlayer.Listener
{
    static final String LOG_COMPONENT = "player";

    private final Luwrain luwrain;
    private final Settings sett;
    private final Random rand = new Random();
    private final List<org.luwrain.player.Listener> listeners = new Vector();

    private State state = State.STOPPED;
    private int volume = 100;
    private MediaResourcePlayer.Instance currentPlayer = null;
    private org.luwrain.player.Playlist playlist = null;
    private Set<Flags> flags = null;
    private int trackNum = 0;
    private long posMsec = 0;

    Dispatcher(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
	this.sett = Settings.create(luwrain.getRegistry());
	this.volume = this.sett.getVolume(100);
	if (this.volume < 0)
	    this.volume = 0;
	if (this.volume > 100)
	    this.volume = 100;
    }

    @Override public synchronized Result play(org.luwrain.player.Playlist playlist, int startingTrackNum, long startingPosMsec, Set<Flags> flags)
    {
	NullCheck.notNull(playlist, "playlist");
	NullCheck.notNull(flags, "flags");
	if (playlist.getPlaylistUrls() == null)
	    return Result.INVALID_PLAYLIST;
	if (startingTrackNum < 0 || startingTrackNum >= playlist.getPlaylistUrls().length)
	    return Result.INVALID_PLAYLIST;
	stop();
	this.playlist = playlist;
	this.flags = flags;
	this.trackNum = startingTrackNum;
	this.posMsec = startingPosMsec;
	final Result res = runPlayer();
	if (res != Result.OK)
	{
	    this.playlist = null;
	    this.trackNum = 0;
	    this.posMsec = 0;
	    this.flags = null;
	    this.state = State.STOPPED;
	    return res;
	}
	state = State.PLAYING;
	notifyListeners((l)->l.onNewState(playlist, State.PLAYING));
	notifyListeners((l)->l.onNewPlaylist(playlist));
	notifyListeners((l)->l.onNewTrack(playlist, trackNum));
	return Result.OK;
    }

    @Override public synchronized boolean stop()
    {
	if (state == State.STOPPED)
	    return false;
	//Current player may be null, this means we are paused
	if (currentPlayer != null)
	    currentPlayer.stop();
	currentPlayer = null;
	//Playlist must be saved
	trackNum = 0;
	posMsec = 0;
	state = State.STOPPED;
	notifyListeners((listener)->listener.onNewState(playlist, State.STOPPED));
	return true;
    }

    @Override public synchronized boolean pauseResume()
    {
	if (state == State.STOPPED)
	    return false;
	//FIXME:streaming
	if (state == State.PLAYING)
	{
	    //pausing
	    currentPlayer.stop();
	    currentPlayer = null;
	    state = State.PAUSED;
	    notifyListeners((listener)->listener.onNewState(playlist, State.PAUSED));
	} else
	{
	    //resuming
	    if (runPlayer() != Result.OK)
	    {
		//FIXME:
		return true;
	    }
	    state = State.PLAYING;
	    notifyListeners((listener)->listener.onNewState(playlist, State.PLAYING));
	    notifyListeners((listener)->listener.onTrackTime(playlist, trackNum, posMsec));
	}
	return true;
    }

    @Override public synchronized boolean jump(long offsetMsec)
    {
	if (state == State.STOPPED || flags.contains(Flags.STREAMING))
	    return false;
	if (currentPlayer != null)
	{
	    currentPlayer.stop();
	    currentPlayer = null;
	}
	posMsec += offsetMsec;
	if (posMsec < 0)
	    posMsec = 0;
	runPlayer();
	state = State.PLAYING;
	notifyListeners((listener)->listener.onTrackTime(playlist, trackNum, posMsec));
	return true;
    }

    @Override public synchronized boolean nextTrack()
    {
	if (state == State.STOPPED || flags.contains(Flags.STREAMING))
	    return false;
	final String[] items = playlist.getPlaylistUrls();
	if (items == null || trackNum + 1 >= items.length)
	    return false;
	final State prevState = state;
	if (currentPlayer != null)
	{
	    currentPlayer.stop();
	    currentPlayer = null;
	}
	++trackNum;
	posMsec = 0;
	runPlayer();
	state = State.PLAYING;
	if (prevState != state)
	    	notifyListeners((listener)->listener.onNewState(playlist, State.PLAYING));
	notifyListeners((listener)->listener.onNewTrack(playlist, trackNum));
	notifyListeners((listener)->listener.onTrackTime(playlist, trackNum, 0));
	return true;
    }

    @Override public synchronized boolean prevTrack()
    {
	if (state == State.STOPPED || flags.contains(Flags.STREAMING))
	    return false;
	final String[] items = playlist.getPlaylistUrls();
	if (items == null || trackNum == 0)
	    return false;
	final State prevState = state;
	if (currentPlayer != null)
	{
	    currentPlayer.stop();
	    currentPlayer = null;
	}
	--trackNum;
posMsec = 0;
	runPlayer();
	state = State.PLAYING;
	if (prevState != state)
	    	notifyListeners((listener)->listener.onNewState(playlist, State.PLAYING));
		    	notifyListeners((listener)->listener.onNewTrack(playlist, trackNum));
	notifyListeners((listener)->listener.onTrackTime(playlist, trackNum, 0));
	return true;
    }

    @Override public synchronized void onPlayerTime(MediaResourcePlayer.Instance sourcePlayer, long msec)
    {
	if (currentPlayer == null || sourcePlayer == null || currentPlayer != sourcePlayer)
	    return;
	if (state != State.PLAYING || flags.contains(Flags.STREAMING))
	    return;
	if (posMsec <= msec && msec < posMsec + 50)
	    return;
	posMsec = msec;
	notifyListeners((listener)->listener.onTrackTime(playlist, trackNum, posMsec));
    }

    @Override public synchronized void onPlayerFinish(MediaResourcePlayer.Instance sourcePlayer)
    {
	if (currentPlayer == null || sourcePlayer == null || currentPlayer != sourcePlayer)
	    return;
	if (state != State.PLAYING)
	    return;
	final String[] items = playlist.getPlaylistUrls();
	if (flags.contains(Flags.STREAMING) || items == null || items.length == 0)
	{
	    stop();
	    return;
	}
	if (!flags.contains(Flags.CYCLED) && !flags.contains(Flags.RANDOM))
	{
	    if (trackNum + 1 < items.length)
		nextTrack(); else
		stop();
	    return;
	}
	if (currentPlayer != null)
	{
	    currentPlayer.stop();
	    currentPlayer = null;
	}
	if (!flags.contains(Flags.RANDOM))
	{
	    if (trackNum + 1 < items.length)
		++trackNum; else
		trackNum = 0;
	} else
	    trackNum = rand.nextInt(items.length);
	posMsec = 0;
	runPlayer();
	state = State.PLAYING;
	notifyListeners((listener)->listener.onNewTrack(playlist, trackNum));
	notifyListeners((listener)->listener.onTrackTime(playlist, trackNum, 0));
    }

    @Override public synchronized void onPlayerError(Exception e)
    {
    }

    @Override public synchronized boolean hasPlaylist()
    {
	return playlist != null;
    }

    @Override public synchronized org.luwrain.player.Playlist getPlaylist()
    {
	return playlist;
    }

    @Override public synchronized int getTrackNum()
    {
	return trackNum;
    }

    @Override public State getState()
    {
	return state != null?state:State.STOPPED;
    }

    @Override public int getVolume()
    {
	return volume;
    }

    @Override public void setVolume(int value)
    {
	Log.debug(LOG_COMPONENT, "changing volume to " + value);
	if (value < 0 || value > 100)
	    throw new IllegalArgumentException("value (" + value + ") must be between 0 and 100 (inclusively)");
	if (this.volume == value)
	    return;
	this.volume = value;
	if (currentPlayer != null)
	    currentPlayer.setVolume(this.volume);
	sett.setVolume(this.volume);
    }

    @Override public synchronized void addListener(org.luwrain.player.Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	for(org.luwrain.player.Listener l: listeners)
	    if (l == listener)
		return;
	listeners.add(listener);
    }

    @Override public synchronized void removeListener(org.luwrain.player.Listener listener)
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
	for(org.luwrain.player.Listener l: listeners)
	    notification.notify(l);
    }

    private Task createTask()
    {
	final String[] items = playlist.getPlaylistUrls();
	if (items == null || trackNum < 0 || trackNum >= items.length)
	    return null;
	for(int i = 0;i < items.length;++i)
	    if (items[i] == null)
		return null;
	final String url = items[trackNum];
	Log.debug(LOG_COMPONENT, "creating task for " + url);
	try {
	    return new Task(new URL(url), flags.contains(Flags.STREAMING)?0:posMsec);
	}
	catch (java.io.IOException e)
	{
	    Log.error(LOG_COMPONENT, "unable to create the URL object for " + url + ":" + e.getClass().getName() + ":" + e.getMessage());
	    return null;
	}
    }

    private MediaResourcePlayer findPlayer(Task task)
    {
	NullCheck.notNull(task, "task");
	final String contentType = luwrain.suggestContentType(task.url, ContentTypes.ExpectedType.AUDIO);
	Log.debug(LOG_COMPONENT, "suggested content type is \'" + contentType + "\'");
	for(MediaResourcePlayer p: luwrain.getMediaResourcePlayers())
	{
	    final String supportedTypes = p.getSupportedMimeType();
	    if (supportedTypes.trim().toLowerCase().equals(contentType.trim().toLowerCase()))
		return p;
	}
	return null;
    }

    private Result runPlayer()
    {
	final Task task = createTask();
	if (task == null)
	    return Result.INVALID_PLAYLIST;
	final MediaResourcePlayer p = findPlayer(task);
	if (p == null)
	{
	    Log.error(LOG_COMPONENT, "unable to choose the player for " + task.url.toString());
	    return Result.UNSUPPORTED_FORMAT_STARTING_TRACK;
	}
	Log.debug(LOG_COMPONENT, "the chosen player is \'" + p.getExtObjName() + "\'");
	final MediaResourcePlayer.Instance instance = p.newMediaResourcePlayer(this);
	final MediaResourcePlayer.Params params = new MediaResourcePlayer.Params();
	params.playFromMsec = task.startPosMsec;
	params.volume = volume;
	params.flags = EnumSet.noneOf(MediaResourcePlayer.Flags.class);
	final MediaResourcePlayer.Result result = instance.play(task.url, params);
	if (!result.isOk())
	    switch(result.getType())
	    {
	    case INACCESSIBLE_SOURCE:
		return Result.INACCESSIBLE_SOURCE;
	    default:
		return Result.GENERAL_PLAYER_ERROR;
	    }
	currentPlayer = instance;
	return Result.OK;
    }

    private interface ListenerNotification
    {
	void notify(org.luwrain.player.Listener listener);
    }
}
