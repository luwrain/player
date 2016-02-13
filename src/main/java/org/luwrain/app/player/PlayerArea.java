/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.player;

//import java.net.URL;                                                         

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.player.*;

class PlayerArea extends NavigateArea
{
    private Luwrain luwrain;
    private Actions actions;
    private Strings strings;

    private Playlist playlist = null;
    private int trackNum = 0;
    private String trackTitle = "-";
    private int trackTime;

    PlayerArea(Luwrain luwrain, Actions actions,
	       Strings strings, Playlist currentPlaylist, int currentTrackNum)
    {
	super(new DefaultControlEnvironment(luwrain));
	this.luwrain = luwrain;
	this.actions = actions;
	this.strings = strings;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(strings, "strigns");
	this.playlist = currentPlaylist;
	if (playlist != null)
	{
	    this.trackNum = currentTrackNum;
	    final String[] items = playlist.getPlaylistItems();
	    if (items != null && trackNum < items.length && items[trackNum] != null)
		trackTitle = items[trackNum];
	}
    }

    @Override public int getLineCount()
    {
	return 4;
    }

    @Override public String getLine(int index)
    {
	switch(index)
	{
	case 0:
	    if (playlist != null)
	    {
		final String title = playlist.getPlaylistTitle();
		return title != null?title:"-";
	    }
	    return "";
	case 1:
	    return trackTitle != null?trackTitle:"";
	case 2:
	    return getTimeStr();
	default:
	    return "";
	}
    }

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (actions.commonKeys(event))
			return true;
		    if (event.isCommand() && !event.isModified())
			switch(event.getCommand())
			{
			case KeyboardEvent.TAB:
			    actions.goToDoc();
			    return true;
			case KeyboardEvent.BACKSPACE:
			    actions.goToTree();
			    return true;
			}
		    return super.onKeyboardEvent(event);
		}


    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch(event.getCode())
	{
	    /*
	case EnvironmentEvent.THREAD_SYNC:
	    if (event instanceof  ListenerEvent)
		onListenerEvent((ListenerEvent)event);
	    return true;
	    */
	case CLOSE:
	    actions.closeApp();
	    return true;
	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public String getAreaName()
    {
	return strings.controlAreaName();
    }

    private String getTimeStr()
    {
	final StringBuilder b = new StringBuilder();
	final int min = trackTime / 60;
	final int sec = trackTime % 60;
	if (min < 10)
	    b.append("0" + min); else
	    b.append("" + min);
	b.append(":");
	if (sec < 10)
	    b.append("0" + sec); else
	    b.append("" + sec);
	return new String(b);
    }

    /*
    private void play()
    {
	final Media media = new Media("http://internet-radio.org.ua/go.php?site=http://www.radiovos.ru/radiovos-128.m3u");
    final MediaPlayer mediaPlayer = new MediaPlayer(media);                  
    mediaPlayer.play();                                                      
    }
    */

    void onTrackTime(int sec)
    {
	trackTime = sec;
luwrain.onAreaNewContent(this);
    }

    void onNewPlaylist(Playlist playlist)
    {
	if (playlist == null)
	    return;
	this.playlist = playlist;
	trackNum = 0;
	trackTitle = "-";
	trackTime = 0;
	luwrain.onAreaNewContent(this);
    }

    void onNewTrack(int trackNum)
    {
    }

    void onStop()
    {
	trackTitle = "-";
	trackNum = 0;
	trackTime = 0;
	luwrain.onAreaNewContent(this);
    }
}
