
package org.luwrain.player.backends;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import org.luwrain.core.NullCheck;

class JLayerStreaming implements org.luwrain.player.backends.BackEnd
{                                                                                                
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Player player = null;
    private FutureTask task = null;
    private Listener listener;

JLayerStreaming(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	this.listener = listener;
    }

    @Override public boolean play(Task trackTask)
    {
	NullCheck.notNull(trackTask, "trackTask");
	if (task != null && !task.isDone())
	    return false;
	task = new FutureTask(()->{
	try {
	    final URLConnection urlConnection = trackTask.url.openConnection();
	urlConnection.connect();
player = new Player(urlConnection.getInputStream());
	player.play();
	    listener.onPlayerBackEndFinish();
	}                                                                                        
	catch (IOException e)
	{
	    e.printStackTrace();
	}
	catch (JavaLayerException e)
	{
	    e.printStackTrace();
	}
	    }, null);
	executor.execute(task);
	return true;
    }                                                                                            

    @Override public void stop()
    {
	if (task == null || task.isDone())
	    return;
	player.close();
	player = null;
	try {
	    task.get();
	}
	catch(InterruptedException e)
	{
	    Thread.currentThread().interrupt();
	}
	catch(ExecutionException e)
	{
	    e.printStackTrace();
	}
    }
}

