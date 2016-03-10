
package org.luwrain.player.backends;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import org.luwrain.core.NullCheck;
import org.luwrain.player.BackEndStatus;

public class JLayer implements org.luwrain.player.BackEnd
{                                                                                                
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Player player = null;
    private FutureTask task = null;
    private BackEndStatus status;

    public JLayer(BackEndStatus status)
    {
	this.status = status;
	NullCheck.notNull(status, "status");
    }

    @Override public boolean play(String uri)
    {
	NullCheck.notNull(uri, "uri");
	if (task != null && !task.isDone())
	    return false;
	task = new FutureTask(()->{
	try
	{
	    final URLConnection urlConnection = new URL(uri).openConnection();
	urlConnection.connect();
player = new Player(urlConnection.getInputStream());
	player.play();
	    status.onBackEndFinish();
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

