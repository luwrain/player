package org.luwrain.player;

import java.util.*;
import org.luwrain.core.*;
import org.luwrain.player.backends.*;
import org.luwrain.util.RegistryPath;

class Impl
{
    BackEnd regularBackEnd = null;
    BackEnd streamingBackEnd = null;

    private BackEnd currentPlayer = null;//null means the player is in idle state
    private final Vector<Listener> listeners = new Vector<Listener>();
    private Playlist currentPlaylist;
    private int currentTrackNum = 0;
    private int lastSec = 0;

synchronized void play(Playlist playlist)
    {
	try {
	NullCheck.notNull(playlist, "playlist");
	//	System.out.println("play1");
	if (playlist.getPlaylistItems() == null || playlist.getPlaylistItems().length < 1)
	    return;
	stop();
	this.currentPlaylist = playlist;
	currentTrackNum = 0;
	//	if (playlist.isStreaming())
	currentPlayer = streamingBackEnd;// else
	//	    currentPlayer = regularBackEnd;
	for(Listener l: listeners)
	{
	    l.onNewPlaylist(playlist);
	    l.onNewTrack(currentTrackNum);
	}
	currentPlayer.play(playlist.getPlaylistItems()[currentTrackNum]);
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }

synchronized void stop()
    {
	if (currentPlayer == null)
	    return;
	currentPlayer.stop();
	for(Listener l: listeners)
	    l.onPlayerStop();
	currentPlayer = null;
    }

synchronized Playlist getCurrentPlaylist()
    {
	return currentPlaylist;
    }

    synchronized int getCurrentTrackNum()
    {
	return currentTrackNum;
    } 

    synchronized void onBackEndTime(int sec)
    {
	if (lastSec == sec)
	    return;
	lastSec = sec;
	//	System.out.println("" + listeners.size() + " listeners");
	for(Listener l: listeners)
	    l.onTrackTime(lastSec);
    }

    synchronized void onBackEndFinish()
    {
    }

    synchronized void addListener(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	for(Listener l: listeners)
	    if (l == listener)
		return;
	listeners.add(listener);
    }

    synchronized void removeListener(Listener listener)
    {
    }
}
