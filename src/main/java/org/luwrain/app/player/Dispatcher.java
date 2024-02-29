/*
   Copyright 2012-2024 Michael Pozhidaev <msp@luwrain.org>

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

import org.luwrain.core.*;
import org.luwrain.core.MediaResourcePlayer.*;

final class Dispatcher implements org.luwrain.player.Player, MediaResourcePlayer.Listener
{
    static final String
	LOG_COMPONENT = "player";

    private interface ListenerNotification
    {
	void notify(org.luwrain.player.Listener listener);
    }

    private final Luwrain luwrain;
    private final Random rand = new Random();
    private MediaResourcePlayer[] mediaResourcePlayers;
    private final List<org.luwrain.player.Listener> listeners = new ArrayList<>();

    private State state = State.STOPPED;
    private int volume = MAX_VOLUME;
    private Instance player = null;
    private org.luwrain.player.Playlist playlist = null;
    private Set<Flags> flags = null;
    private int trackNum = 0;
    private long posMsec = 0;

    Dispatcher(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
	this.mediaResourcePlayers = luwrain.getMediaResourcePlayers();
	for(MediaResourcePlayer p: mediaResourcePlayers)
	    Log.debug(LOG_COMPONENT, "'" + p.getExtObjName() + "' is a known media resource player");
    }

    @Override public synchronized Result play(org.luwrain.player.Playlist playlist, int startingTrackNum, long startingPosMsec, Set<Flags> flags)
    {
	NullCheck.notNull(playlist, "playlist");
	NullCheck.notNull(flags, "flags");
	if (startingTrackNum < 0 || startingTrackNum >= playlist.getTrackCount())
	    throw new IllegalArgumentException("Illegal starting track num: " + String.valueOf(startingTrackNum));
	if (startingPosMsec < 0)
	    throw new IllegalArgumentException("Illegal starting position: " + String.valueOf(startingPosMsec));
	stop();
	this.playlist = playlist;
	this.volume = playlist.getVolume();
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
	playlist.onProgress(trackNum, startingPosMsec);
	return Result.OK;
    }

    @Override public synchronized boolean stop()
    {
	if (state == State.STOPPED)
	    return false;
	//If the current player is null, we are paused
	if (this.player != null)
	    player.stop();
	player = null;
	//The playlist must be keeped, we don't touch it
	this.trackNum = 0;
	this.posMsec = 0;
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
	    player.stop();
	    player = null;
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
	if (this.player != null)
	{
	    this.player.stop();
	    this.player = null;
	}
	posMsec += offsetMsec;
	if (posMsec < 0)
	    posMsec = 0;
	runPlayer();
	state = State.PLAYING;
	notifyListeners((listener)->listener.onTrackTime(playlist, trackNum, posMsec));
	this.playlist.onProgress(trackNum, posMsec);
	return true;
    }

    @Override public synchronized boolean nextTrack()
    {
	if (state == State.STOPPED || flags.contains(Flags.STREAMING))
	    return false;
	if (trackNum + 1 >= playlist.getTrackCount())
	    return false;
	final State prevState = state;
	if (player != null)
	{
	    this.player.stop();
	    this.player = null;
	}
	++this.trackNum;
	this.posMsec = 0;
	runPlayer();
	this.state = State.PLAYING;
	if (prevState != state)
	    	notifyListeners((listener)->listener.onNewState(playlist, State.PLAYING));
	notifyListeners((listener)->listener.onNewTrack(playlist, trackNum));
	notifyListeners((listener)->listener.onTrackTime(playlist, trackNum, 0));
	this.playlist.onProgress(trackNum, 0);
	return true;
    }

    @Override public synchronized boolean prevTrack()
    {
	if (state == State.STOPPED || flags.contains(Flags.STREAMING))
	    return false;
	if (trackNum == 0)
	    return false;
	final State prevState = state;
	if (player != null)
	{
	    this.player.stop();
	    this.player = null;
	}
	--this.trackNum;
this.posMsec = 0;
	runPlayer();
	state = State.PLAYING;
	if (prevState != state)
	    	notifyListeners((listener)->listener.onNewState(playlist, State.PLAYING));
		    	notifyListeners((listener)->listener.onNewTrack(playlist, trackNum));
	notifyListeners((listener)->listener.onTrackTime(playlist, trackNum, 0));
		this.playlist.onProgress(trackNum, 0);
	return true;
    }

        @Override public synchronized boolean playTrack(int trackIndex)
    {
	if (state == State.STOPPED || flags.contains(Flags.STREAMING))
	    return false;
	if (trackIndex < 0 || trackIndex >= playlist.getTrackCount())
	    return false;
	final State prevState = state;
	if (player != null)
	{
	    this.player.stop();
	    this.player = null;
	}
	this.trackNum = trackIndex;
this.posMsec = 0;
	runPlayer();
	state = State.PLAYING;
	if (prevState != state)
	    	notifyListeners((listener)->listener.onNewState(playlist, State.PLAYING));
		    	notifyListeners((listener)->listener.onNewTrack(playlist, trackNum));
	notifyListeners((listener)->listener.onTrackTime(playlist, trackNum, 0));
	this.playlist.onProgress(this.trackNum, 0);
	return true;
    }

    @Override public synchronized void onPlayerTime(Instance sourcePlayer, long msec)
    {
	if (this.player == null || sourcePlayer == null || this.player != sourcePlayer)
	    return;
	if (this.state != State.PLAYING || flags.contains(Flags.STREAMING))
	    return;
	if (this.posMsec <= msec && msec < this.posMsec + 50)
	    return;
	this.posMsec = msec;
	notifyListeners((listener)->listener.onTrackTime(playlist, trackNum, posMsec));
	this.playlist.onProgress(this.trackNum, this.posMsec);
    }

    @Override public synchronized void onPlayerFinish(Instance sourcePlayer)
    {
	if (player == null || sourcePlayer == null || player != sourcePlayer)
	    return;
	if (this.state != State.PLAYING)
	    return;
	if (flags.contains(Flags.STREAMING))
	{
	    stop();
	    return;
	}
	if (!flags.contains(Flags.CYCLED) && !flags.contains(Flags.RANDOM))
	{
	    if (trackNum + 1 < playlist.getTrackCount())
		this.nextTrack(); else
		stop();
	    return;
	}
	if (this.player != null)
	{
	    this.player.stop();
	    player = null;
	}
	if (!flags.contains(Flags.RANDOM))
	{
	    if (trackNum + 1 < playlist.getTrackCount())
		++this.trackNum; else
		this.trackNum = 0;
	} else
	    this.trackNum = rand.nextInt(playlist.getTrackCount());
	this.posMsec = 0;
	runPlayer();
	this.state = State.PLAYING;
	notifyListeners((listener)->listener.onNewTrack(playlist, trackNum));
	notifyListeners((listener)->listener.onTrackTime(playlist, trackNum, 0));
	this.playlist.onProgress(this.trackNum, 0);
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
	return this.volume;
    }

    @Override public void setVolume(int value)
    {
	final int newVolume = Math.min(Math.max(value, MIN_VOLUME), MAX_VOLUME);
	if (this.volume == newVolume)
	    return;
	this.volume = newVolume;
	if (player != null)
	    player.setVolume(this.volume);
	if (playlist != null)
	    playlist.onNewVolume(this.volume);
    }

    @Override public Set<Flags> getFlags()
    {
	return this.flags;
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
	for(org.luwrain.player.Listener l: listeners)
	    try {
		notification.notify(l);
	    }
	    catch(Throwable e)
	    {
		Log.warning(LOG_COMPONENT, "a player listener has thrown an exception: " + e.getClass().getName() + ": " + e.getMessage());
	    }
    }

    private Task createTask()
    {
	if (trackNum >= playlist.getTrackCount())
	    return null;
	final String url = playlist.getTrackUrl(trackNum);
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
	for(MediaResourcePlayer p: mediaResourcePlayers)
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
	    Log.error(LOG_COMPONENT, "unable to choose a player for " + task.url.toString());
	    return Result.UNSUPPORTED_FORMAT_STARTING_TRACK;
	}
	final MediaResourcePlayer.Instance instance = p.newMediaResourcePlayer(this);
	final MediaResourcePlayer.Params params = new MediaResourcePlayer.Params();
	params.playFromMsec = task.startPosMsec;
	params.volume = volume;
	params.flags = EnumSet.noneOf(MediaResourcePlayer.Flags.class);
	final MediaResourcePlayer.Result result = instance.play(task.url, params);
	if (result == null)
	    		return Result.GENERAL_PLAYER_ERROR;
	if (!result.isOk())
	    switch(result.getType())
	    {
	    case INACCESSIBLE_SOURCE:
		return Result.INACCESSIBLE_SOURCE;
	    default:
		return Result.GENERAL_PLAYER_ERROR;
	    }
	player = instance;
	return Result.OK;
    }

}
