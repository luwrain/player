package test;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;

class MpegAudioImpl extends PlaybackListener implements Runnable
{
	private AudioInputStream audioStream=null;
    private AdvancedPlayer player;
    private AudioFormat bitFmt;
    private Thread playerThread;

	// offsets in frames
    private int framesFrom=0;
	private int framesTo=Integer.MAX_VALUE;
	// offset in milliseconds
	private int offsetStart;
	private int offsetEnd;
	// false when called resume(), and true for play()
	private boolean isNewPlaying=true;

	// only one of this can be not null
	private InputStream sourceStream=null;
	private File sourceFile=null;
	private URL sourceUrl=null;

	private long startTime;
	
	/** change current source for audio input stream, if used, must be called each play() and resume() methods with newly opened stream, each one close it */
	public void setSource(InputStream stream)
	{
		sourceStream=stream;
		sourceFile=null;
		sourceUrl=null;
	}
	/** change current source for audio stream to file */
	public void setSource(File file)
	{
		sourceStream=null;
		sourceFile=file;
		sourceUrl=null;
	}
	/** change current source for audio stream to url */
	public void setSource(URL url)
	{
		sourceStream=null;
		sourceFile=null;
		sourceUrl=url;
	}
	
	public void stop()
	{
	    // stop any playing
	    if(player!=null) player.stop();
	}

	/**
	 * command to play section audio stream (it starts thread which do any init and reopen stream) 
	 * @param from - start offset in milliseconds
	 * @param to - end offset in millisecond, we can use Integer.MAX_VALUE to end of audio
	 */
	public void play(int from,int to)
	{
		isNewPlaying=true;
		offsetStart=from;
		offsetEnd=to;
	    // stop any playing
	    if(player!=null) player.stop();
	    // and make new thread
		playerThread = new Thread(this, "AudioPlayerThread");
		playerThread.start();
	}
	
	/**
	 * command to play from last position (or begining), it starts thread which do any init and reopen stream
	 */
	public void resume()
	{
		isNewPlaying=false;
	    // stop any playing
		if(player!=null) this.player.stop();
	    // and make new thread
		this.playerThread = new Thread(this, "AudioPlayerThread");
		this.playerThread.start();
	}

    public void playbackStarted(PlaybackEvent playbackEvent)
	{
	    // check play start time for calculate duration
    	startTime=new Date().getTime();
		System.out.println("playing started "+framesFrom);
	}
	
	public void playbackFinished(PlaybackEvent playbackEvent)
	{
	    // check play end time for calculate duration
    	long endTime=new Date().getTime();
        // calculate finished frame for resume
    	if(startTime!=0)
    	{ // detect double called finished
    		framesFrom+=Math.round((endTime-startTime)*bitFmt.getFrameRate()/1000);
    		startTime=0;
    	}
		System.out.println("playing finished "+framesFrom);
	}    
	public void run()
    {
    	AudioDevice device=null;
        try
        {
        	// choose source stream
			if(sourceStream!=null) audioStream = AudioSystem.getAudioInputStream(sourceStream);else
			if(sourceFile!=null) audioStream = AudioSystem.getAudioInputStream(sourceFile);else
			if(sourceUrl!=null) audioStream = AudioSystem.getAudioInputStream(sourceUrl);
			// get current audio format information
	        bitFmt = audioStream.getFormat();
	        // resume or play
	        if(isNewPlaying)
	        {
	        	//System.out.println("AUDIO RUN: set new offset");
				// convert from and to from milliseconds to frames
				framesFrom=Math.round(offsetStart*bitFmt.getFrameRate()/1000);
				framesTo=(offsetEnd==Integer.MAX_VALUE?Integer.MAX_VALUE:Math.round(offsetEnd*bitFmt.getFrameRate()/1000));
	        }
    		// audio device
    		device=FactoryRegistry.systemRegistry().createAudioDevice();
    		player = new AdvancedPlayer(audioStream,device);
    	    player.setPlayBackListener(this);
    		// play current audio section
    	    //System.out.println("AUDIO RUN: play frames "+framesFrom+"-"+framesTo);
            player.play(framesFrom,framesTo);
        	System.out.println("AUDIO RUN: frames");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
    		// close all
    		player.close();
    		player=null;
    		if(device!=null) device.close();
		}
    }
}