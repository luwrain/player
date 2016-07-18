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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.player.*;

class ControlArea extends NavigationArea
{
    private Luwrain luwrain;
    private Actions actions;
    private Strings strings;
    private Base base;

    private String opPauseResume, opStop, opPrevTrack, opNextTrack;

    private long timeSec = -1;

ControlArea(Luwrain luwrain, Actions actions,
	       Strings strings)
    {
	super(new DefaultControlEnvironment(luwrain));
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(strings, "strigns");
	this.luwrain = luwrain;
	this.actions = actions;
	this.strings = strings;
	base = actions.getBase();
	opPauseResume = strings.opPauseResume();
	opStop = strings.opStop();
	opPrevTrack = strings.opPrevTrack();
	opNextTrack = strings.opNextTrack();
    }

    void onNewPlaylist(Playlist playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	base.onNewPlaylist(playlist);
	actions.refreshPlaylist();
	luwrain.onAreaNewContent(this);
    }

    void onNewTrack(int trackNum)
    {
	base.onNewTrack(trackNum);
	luwrain.onAreaNewContent(this);
    }

    void onTrackTime(long msec)
    {
	long sec = msec / 1000;
	if (sec != timeSec)
	{
	timeSec = sec;
	luwrain.onAreaNewContent(this);
	}
    }

    void onStop()
    {
	base.onStop();
	timeSec = -1;
	luwrain.onAreaNewContent(this);
    }

    @Override public int getLineCount()
    {
	int count = 0;
	if (!base.getCurrentPlaylistTitle().isEmpty())
	    ++count;
	if (!base.getCurrentTrackTitle().isEmpty())
	    ++count;
	if (timeSec >= 0)
	    ++count;
	return count + 6;
    }

    @Override public String getLine(int index)
    {
	int offset = 0;
	if (base.getCurrentPlaylistTitle().isEmpty())
	    ++offset;
	if (base.getCurrentTrackTitle().isEmpty())
	    ++offset;
	if (timeSec < 0)
	    ++offset;
	switch(index + offset)
	{
	case 0:
	    return base.getCurrentPlaylistTitle();
	case 1:
	    return base.getCurrentTrackTitle();
	case 2:
	    return Base.getTimeStr(timeSec);
	case 3:
	    return "";
	case 4:
	    return opPauseResume;
	case 5:
	    return opStop;
	case 6:
	    return opPrevTrack;
	case 7:
	    return opNextTrack;
	default:
	    return "";
	}
    }

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    actions.goToTree();
			    return true;
			case BACKSPACE:
			    actions.goToTree();
			    return true;
			case ENTER:
			    return onEnter(event);
			}
		    return super.onKeyboardEvent(event);
		}

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch(event.getCode())
	{
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

    @Override public void announceLine(int index, String line)
    {
	if (line == opPauseResume || line == opStop ||
line == opPrevTrack || line == opNextTrack)
	    luwrain.playSound(Sounds.LIST_ITEM);
	if (line.isEmpty())
	    luwrain.hint(Hints.EMPTY_LINE); else
	    luwrain.say(line);
    }

    private boolean onEnter(KeyboardEvent event)
    {
	final String line = getLine(getHotPointY());
	if (line == opPauseResume)
	{
	    actions.pauseResume();
	    luwrain.playSound(Sounds.LIST_ITEM);
	    return true;
	}
	if (line == opStop)
	{
	    actions.stop();
	    luwrain.playSound(Sounds.LIST_ITEM);
	    return true;
	}
	if (line == opPrevTrack)
	{
	    actions.prevTrack();
	    luwrain.playSound(Sounds.LIST_ITEM);
	    return true;
	}
	if (line == opNextTrack)
	{
	    actions.nextTrack();
	    luwrain.playSound(Sounds.LIST_ITEM);
	    return true;
	}
	return false;
    }
}
