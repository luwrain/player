/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

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

package org.luwrain.extensions.plogg;

import java.util.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.luwrain.base.*;
import org.luwrain.core.*;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

class OggPlayer implements org.luwrain.base.MediaResourcePlayer.Instance
{
    private static final int NOTIFY_MSEC_COUNT=500;

	private final MediaResourcePlayer.Listener listener;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private SourceDataLine outputLine = null;
	AudioFormat audioFormat;

    private final int bufferSize = 2048;
    private byte[] buffer = null;                                                       
    private int count = 0;                                                              
    private int index = 0;                                                              
    private byte[] convertedBuffer = null;
    private int convertedBufferSize;                                                    
    private float[][][] pcmInfo = null;
    private int[] pcmIndex = null;
    private boolean toContinue = true;

    private Packet joggPacket = null;
    private Page joggPage = null;
    private StreamState joggStreamState = null;
    private SyncState joggSyncState = null;
    private DspState jorbisDspState = null;
    private Block jorbisBlock = null;
    private Comment jorbisComment = null;
    private Info jorbisInfo = null;

    private InputStream inputStream = null;
	private long totalSamples;                                     
	private long skipSamples;                                     

    OggPlayer(MediaResourcePlayer.Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	this.listener = listener;
    }

    @Override public MediaResourcePlayer.Result play(URL url, long playFromMsec, Set<MediaResourcePlayer.Flags> flags)
	{
	    NullCheck.notNull(url, "url");
	    NullCheck.notNull(flags, "flags");
	    //		this.task=task;
		try {
				inputStream = url.openStream();
		} catch(Exception e)
		{
			e.printStackTrace();
			listener.onPlayerFinish(OggPlayer.this);
			return new MediaResourcePlayer.Result();//FIXME:
		}
		//
    	FutureTask<Boolean> futureTask = new FutureTask<>(()->
    	{
			try
			{
				try
				{
				    init();
				    if(readHeader() && initSound())
				    {
				    	readBody();
				    }
				}
				finally 
				{
				    cleanUp();                                                              
				    inputStream.close();
				    inputStream = null;
				}
			}
			catch(Exception e)
			{
			    Log.error("ogg", "unexpected exception while playing:" + e.getClass().getName() + ":" + e.getMessage());
			    e.printStackTrace();
			}
			return true;
    	});
    	executor.execute(futureTask);
	return new MediaResourcePlayer.Result();
	}

	private void init()
	{
		totalSamples=0;
		// Log.debug("ogg", "initializing JOrbis");
		index=0;
		count=0;
		joggPacket=new Packet();
		joggPage=new Page();
		joggStreamState=new StreamState();
		joggSyncState=new SyncState();
		jorbisDspState=new DspState();
		jorbisBlock=new Block(jorbisDspState);
		jorbisComment=new Comment();
		jorbisInfo=new Info();
		joggSyncState.init();
		joggSyncState.buffer(bufferSize);
		buffer=joggSyncState.data;
	}

	private long mSecToSamples(long msec)
	{
		return (long)(audioFormat.getSampleRate()*msec/1000);
	}
	private boolean readHeader() throws IOException
	{
		// Log.debug("ogg", "starting header reading");
		boolean needMoreData=true;
		int packet=1;
		while(needMoreData)
		{
			count=inputStream.read(buffer,index,bufferSize);
			joggSyncState.wrote(count);
			switch(packet)
			{
				case 1:
				{
					switch(joggSyncState.pageout(joggPage))
					{
						case -1:
						{
							System.err.println("There is a hole in the first "+"packet data.");
							return false;
						}
						case 0:
						{
							break;
						}
						case 1:
						{
							joggStreamState.init(joggPage.serialno());
							joggStreamState.reset();
							jorbisInfo.init();
							jorbisComment.init();
							if(joggStreamState.pagein(joggPage)==-1)
							{
								System.err.println("We got an error while "+"reading the first header page.");
								return false;
							}
							if(joggStreamState.packetout(joggPacket)!=1)
							{
								System.err.println("We got an error while "+"reading the first header packet.");
								return false;
							}
							if(jorbisInfo.synthesis_headerin(jorbisComment,joggPacket)<0)
							{
								System.err.println("We got an error while "+"interpreting the first packet. "+"Apparantly, it's not Vorbis data.");
								return false;
							}
							packet++;
							break;
						}
					}
					if(packet==1) break;
				}
				case 2:
				case 3:
				{
					switch(joggSyncState.pageout(joggPage))
					{
						case -1:
						{
							System.err.println("There is a hole in the second "+"or third packet data.");
							return false;
						}
						case 0:
						{
							break;
						}
						case 1:
						{
							joggStreamState.pagein(joggPage);
							switch(joggStreamState.packetout(joggPacket))
							{
								case -1:
								{
									System.err.println("There is a hole in the first"+"packet data.");
									return false;
								}
								case 0:
								{
									break;
								}
								case 1:
								{
									jorbisInfo.synthesis_headerin(jorbisComment,joggPacket);
									packet++;
									if(packet==4)
									{
										needMoreData=false;
									}
									break;
								}
							}
							break;
						}
					}
					break;
				}
			}
			index=joggSyncState.buffer(bufferSize);
			buffer=joggSyncState.data;
			if(count==0&&needMoreData)
			{
				System.err.println("Not enough header data was supplied.");
				return false;
			}
		}
		debugOutput("Finished reading the header.");
		return true;
	}

	synchronized private boolean initSound() throws LineUnavailableException// ,
																			// IllegalStateException
	{
		// Log.debug("ogg", "initializing the sound system");
		convertedBufferSize=bufferSize*2;
		convertedBuffer=new byte[convertedBufferSize];
		jorbisDspState.synthesis_init(jorbisInfo);
		jorbisBlock.init(jorbisDspState);
		int channels=jorbisInfo.channels;
		int rate=jorbisInfo.rate;
		audioFormat=new AudioFormat((float)rate,16,channels,true,false);
		DataLine.Info datalineInfo=new DataLine.Info(SourceDataLine.class,audioFormat,AudioSystem.NOT_SPECIFIED);
		if(!AudioSystem.isLineSupported(datalineInfo))
		{
			Log.error("ogg","audio output line is not supported");
			return false;
		}
		outputLine=(SourceDataLine)AudioSystem.getLine(datalineInfo);
		outputLine.open(audioFormat);
		outputLine.start();
		pcmInfo=new float[1][][];
		pcmIndex=new int[jorbisInfo.channels];
		//
		skipSamples=mSecToSamples(3000);//task.startPosMsec());
		return true;
	}

	private void readBody() throws IOException
	{
		// Log.debug("ogg", "reading the body");
		boolean needMoreData=true;
		while(needMoreData&&toContinue)
		{
			switch(joggSyncState.pageout(joggPage))
			{
				case -1:
				// Log.debug("ogg", "there is a hole in the data");
				break;
				case 0:
				break;
				case 1:
				{
					joggStreamState.pagein(joggPage);
					if(joggPage.granulepos()==0)
					{
						needMoreData=false;
						break;
					}
					processPackets: while(toContinue)
					{
						switch(joggStreamState.packetout(joggPacket))
						{
							case -1:
								// Log.debug("ogg", "there is a hole in the
								// data");
							case 0:
							break processPackets;
							case 1:
								decodeCurrentPacket();
						} // switch()
					} // while(true)
					if(joggPage.eos()!=0) needMoreData=false;
				}
			} // switch()
			if(needMoreData)
			{
				index=joggSyncState.buffer(bufferSize);
				buffer=joggSyncState.data;
				count=inputStream.read(buffer,index,bufferSize);
				joggSyncState.wrote(count);
				if(count==0) needMoreData=false;
			}
		}
		// Log.debug("ogg", "body reading finished");
	}

	synchronized private void cleanUp()
	{
		outputLine.close();
		// Log.debug("ogg", "cleaning up");
		joggStreamState.clear();
		jorbisBlock.clear();
		jorbisDspState.clear();
		jorbisInfo.clear();
		joggSyncState.clear();
	}

	private void decodeCurrentPacket()
	{
		int samples;
		if(jorbisBlock.synthesis(joggPacket)==0) jorbisDspState.synthesis_blockin(jorbisBlock);
		int range;
		while((samples=jorbisDspState.synthesis_pcmout(pcmInfo,pcmIndex))>0&&toContinue)
		{
			if(samples<convertedBufferSize) range=samples;
			else
				range=convertedBufferSize;
			for(int i=0;i<jorbisInfo.channels;i++)
			{
				int sampleIndex=i*2;
				for(int j=0;j<range;j++)
				{
					int value=(int)(pcmInfo[0][i][pcmIndex[i]+j]*32767);
					if(value>32767) value=32767;
					if(value<-32768) value=-32768;
					if(value<0) value=value|32768;
					convertedBuffer[sampleIndex]=(byte)(value);
					convertedBuffer[sampleIndex+1]=(byte)(value>>>8);
					sampleIndex+=2*(jorbisInfo.channels);
				}
			}
			//skip or play
			if(totalSamples>=skipSamples)
			{
				if(toContinue) synchronized(this)
				{
					outputLine.write(convertedBuffer,0,2*jorbisInfo.channels*range);
				}
			}
			totalSamples+=jorbisInfo.channels*range;
			jorbisDspState.synthesis_read(range);
		}
	}

	private void debugOutput(String output)
	{
		System.out.println("Debug: "+output);
	}

	@Override public void stop()
	{
		toContinue = false;
		if (outputLine != null)
		    outputLine.stop();
	}

}
