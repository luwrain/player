package org.luwrain.player.backends;

//import javazoom.jl.player.advanced.AdvancedPlayer;
//import javazoom.jl.player.advanced.PlaybackEvent;
//import javazoom.jl.player.advanced.PlaybackListener;

//import javax.sound.sampled.AudioFormat;
//import javax.sound.sampled.AudioInputStream;
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.UnsupportedAudioFileException;

//import javazoom.jl.decoder.JavaLayerException;
//import javazoom.jl.player.AudioDevice;
//import javazoom.jl.player.FactoryRegistry;


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
    //    private AudioFormat bitFormat;
    //    private Thread playerThread;
    private FutureTask futureTask = null; 

    //    private int framesFrom = 0;
    //    private int framesTo = Integer.MAX_VALUE;
    //    private int offsetStartMsec;
    //    private int offsetEndMsec;
    // false when called resume(), and true for play()
    //    private boolean isNewPlaying=true;

    //    private long startTime;

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
        try {
	    if (task.isPath())
		stream = AudioSystem.getAudioInputStream(Files.newInputStream(task.path()));else
stream = AudioSystem.getAudioInputStream(task.url());
	    bitFormat = stream.getFormat();
	    /*
	        if(isNewPlaying)
	        {
		    framesFrom=Math.round(task.startPosMsec() * bitFormat.getFrameRate() / 1000);
framesTo=(offsetEnd==Integer.MAX_VALUE?Integer.MAX_VALUE:Math.round(offsetEnd*bitFmt.getFrameRate()/1000));
	        }
	    */
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


	futureTask = new FutureTask(()->{
		try {
		    player.play(0, Integer.MAX_VALUE);
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
}
