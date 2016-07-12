
package org.luwrain.player;

import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.player.backends.*;

class PlayerThread
{
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(1024); 
    private FutureTask futureTask;

    private final Vector<Listener> listeners = new Vector<Listener>();
    private org.luwrain.player.backends.Listener backendListener = null;
    private Playlist currentPlaylist;
    private BackEnd currentPlayer = null;
    private int currentTrackNum = 0;
    private long currentPos = 0;

    synchronized void play(Playlist playlist,
			   int startingTrackNum, long startingPosMsec)
    {
	NullCheck.notNull(playlist, "playlist");
	if (playlist.getPlaylistItems() == null || playlist.getPlaylistItems().length < 1)
	    return;
	stop();
	this.currentPlaylist = playlist;
	currentTrackNum = startingTrackNum;
	currentPos = startingPosMsec;
	final Task task = createTask();
	if (task == null)
	    return;
	task.setStartPosMsec(currentPos);
	currentPlayer = BackEnd.createBackEnd(createBackEndListener(), "mp3", false);
	for(Listener l: listeners)
	{
	    l.onNewPlaylist(playlist);
	    l.onNewTrack(playlist, currentTrackNum);
	}
	currentPlayer.play(task);
    }

	synchronized void stop()
    {
	Log.debug("player", "stopping...");
	if (currentPlayer == null)
	    return;
	currentPlayer.stop();
	for(Listener l: listeners)
	    l.onPlayerStop();
	currentPlayer = null;
currentPlaylist = null;
    }

    synchronized void jump(long offsetMsec)
    {
	if (currentPlayer == null)
	    return;
	currentPlayer.stop();
	final Task task = createTask();
	if (task == null)
	{
	    currentPlayer = null;
	    return;
	}
	currentPos += offsetMsec;
	task.setStartPosMsec(currentPos);
	currentPlayer.play(task); 
    }


	synchronized Playlist getCurrentPlaylist()
    {
	return currentPlaylist;
    }

    synchronized int getCurrentTrackNum()
    {
	return currentTrackNum;
    } 

    synchronized void onBackEndTime(long msec)
    {
	if (currentPos + 50 > msec)
	    return;
currentPos = msec;
	for(Listener l: listeners)
	    l.onTrackTime(currentPlaylist, currentTrackNum, currentPos);
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
	if (backendListener == null)
	    backendListener = new org.luwrain.player.backends.Listener(){
	    @Override public void onPlayerBackEndTime(long msec)
	    {
		run(()->onBackEndTime(msec));
	    }
	    @Override public void onPlayerBackEndFinish()
	    {
		run(()->onBackEndFinish());
	    }
	};
	return backendListener;
    }

    private Task createTask()
    {
	try {
	    Task task = new Task(new URL(currentPlaylist.getPlaylistItems()[currentTrackNum]));
	    if (task.url().getProtocol().equals("file"))
		task = new Task(Paths.get(task.url().toURI()));
	    return task;
	}
	catch (Exception e)
	{
	    Log.error("player", "unable to create a task:" + e.getMessage());
	    e.printStackTrace();
	    return null;
	}
    }
}
