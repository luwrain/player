
package org.luwrain.player.backends;

import java.nio.file.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sound.sampled.*;

import org.luwrain.core.NullCheck;

import javazoom.jl.player.advanced.*;
import javazoom.jl.decoder.*;
import javazoom.jl.player.*;

class JLayer2 implements BackEnd
{
    private Listener listener;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private AudioInputStream stream = null;
    private AdvancedPlayer player;
    private FutureTask futureTask = null; 

    JLayer2(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	this.listener = listener;
    }

    @Override public boolean play(Task task)
    {
	NullCheck.notNull(task, "task");
    	AudioDevice device = null;
	AudioFormat bitFormat = null;
	int framesFrom = 0;
        try {
	    if (task.isPath())
		stream = AudioSystem.getAudioInputStream(Files.newInputStream(task.path()));else
		stream = AudioSystem.getAudioInputStream(task.url());
	    bitFormat = stream.getFormat();
	    if (task.startPosMsec() > 0)
		framesFrom=Math.round(task.startPosMsec() * bitFormat.getFrameRate() / 1000);
	    device = FactoryRegistry.systemRegistry().createAudioDevice();
	    player = new AdvancedPlayer(stream, device);
	    player.setPlayBackListener(new PlaybackListener(){
		    @Override public void playbackStarted(PlaybackEvent playbackEvent)
		    {
		    }
		    @Override public void playbackFinished(PlaybackEvent playbackEvent)
		    {
		    }
		});
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	final int finalFramesFrom = framesFrom;
	futureTask = new FutureTask(()->{
		try {
		    player.play(finalFramesFrom, Integer.MAX_VALUE);
		}
		catch (JavaLayerException e)
		{
		    e.printStackTrace();
		}
	    }, null);
	executor.execute(futureTask);
	return true;
    }

    @Override public void stop()
    {
	if(player != null)
	    player.stop();
	player = null;
	stream = null;
    }
}

    /*
    public void playbackStarted(PlaybackEvent playbackEvent)
	{
    	startTime=new Date().getTime();
	}

	public void playbackFinished(PlaybackEvent playbackEvent)
	{
    	long endTime=new Date().getTime();
    	if(startTime!=0)
    	{ // detect double called finished
    		framesFrom+=Math.round((endTime-startTime)*bitFmt.getFrameRate()/1000);
    		startTime=0;
    	}
	}    
    */

