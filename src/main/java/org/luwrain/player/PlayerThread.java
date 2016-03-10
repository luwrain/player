package org.luwrain.player;

import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.player.backends.*;
import org.luwrain.util.RegistryPath;

class PlayerThread
{
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(1024); 
    private FutureTask futureTask;

    private final Vector<Listener> listeners = new Vector<Listener>();
    private Playlist currentPlaylist;
    private int currentTrackNum = 0;
    private BackEnd currentPlayer = null;
    private long lastSec = 0;

synchronized void play(Playlist playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	if (playlist.getPlaylistItems() == null || playlist.getPlaylistItems().length < 1)
	    return;
	stop();
	this.currentPlaylist = playlist;
	currentTrackNum = 0;
	Task task = null;
	try {
task = new Task(new URL(currentPlaylist.getPlaylistItems()[currentTrackNum]));
	if (task.url().getProtocol().equals("file"))
	    task = new Task(Paths.get(task.url().toURI()));

    }
    catch (Exception e)
    {
	Log.error("player", "unable to start playing:" + e.getMessage());
	e.printStackTrace();
    }
	currentPlayer = BackEnd.createBackEnd(createBackEndListener(), "mp3", false);
	for(Listener l: listeners)
	{
	    l.onNewPlaylist(playlist);
	    l.onNewTrack(currentTrackNum);
	}
currentPlayer.play(task);
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

    synchronized void onBackEndTime(long sec)
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

    void startThread()
    {
	futureTask = new FutureTask(()->{
		while(!Thread.currentThread().interrupted())
		{
		    try {
		    final Runnable r = queue.take();
		    if (r != null)
			r.run();
		    }
		    catch (InterruptedException e)
		    {
			Thread.currentThread().interrupt();
		    }
		}
	    }, null);
	executor.execute(futureTask);
    }

    synchronized void run(Runnable runnable)
    {
	NullCheck.notNull(runnable, "runnable");
	try {
	    queue.put(runnable);
	}
	catch(InterruptedException e)
	{
	    Thread.currentThread().interrupt();
	}
    }

    private org.luwrain.player.backends.Listener createBackEndListener()
    {
	return new org.luwrain.player.backends.Listener(){
	    @Override public void onPlayerBackEndTime(long msecs)
	    {
		run(()->onBackEndTime(msecs));
	    }
	    @Override public void onPlayerBackEndFinish()
	    {
		run(()->onBackEndFinish());
	    }
	};
    }

}
