
package org.luwrain.player;

import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.player.backends.*;

class PlayerThread implements org.luwrain.player.backends.Listener
{
    //    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    //    private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(1024); 
    //    private FutureTask futureTask;

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
	if (currentPlaylist != null)
	stop();
	currentPlaylist = playlist;
	currentTrackNum = startingTrackNum;
	currentPos = startingPosMsec;
	final Task task = createTask();
	if (task == null)
	    return;
	task.setStartPosMsec(currentPos);
	currentPlayer = BackEnd.createBackEnd(this, "jlayer");
	for(Listener l: listeners)
	{
	    l.onNewPlaylist(playlist);
	    l.onNewTrack(playlist, currentTrackNum);
	}
	currentPlayer.play(task);
    }

	synchronized void stop()
    {
	if (currentPlayer == null)
	    return;
	currentPlayer.stop();
	notifyListeners((listener)->listener.onPlayerStop());
	currentPlayer = null;
currentPlaylist = null;
    }

    synchronized void pauseResume()
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

    @Override public synchronized void onPlayerBackEndTime(long msec)
    {
	if (currentPos + 50 > msec)
	    return;
currentPos = msec;
	    notifyListeners((listener)->listener.onTrackTime(currentPlaylist, currentTrackNum, currentPos));
    }

    @Override public synchronized void onPlayerBackEndFinish()
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
	NullCheck.notNull(listener, "listener");
	for(int i = 0;i < listeners.size();++i)
	    if (listeners.get(i) == listener)
	    {
		listeners.remove(i);
		break;
	    }
    }

    /*
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
    */

    /*
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
    */

    private void notifyListeners(ListenerNotification notification)
    {
	for(Listener l: listeners)
	    notification.notify(l);
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

private interface ListenerNotification
{
    void notify(Listener listener);
}
}
