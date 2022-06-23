/*
   Copyright 2012-2022 Michael Pozhidaev <msp@luwrain.org>

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

package org.luwrain.app.player;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.player.*;

class ControlArea extends NavigationArea
{
    enum Mode {STOPPED, PAUSED, PLAYING, PLAYING_STREAMING};

    interface Callback
{
}

    private final App app;
    //    private final ControlContext controlContext;
    private final Callback callback;
    private final Strings strings;

    private Mode mode = Mode.STOPPED;
    private String playlistTitle = "";
    private String trackTitle = "";
    private long timeSec = 0;

    ControlArea(App app, ControlContext controlContext, Callback callback, Strings strings)
    {
	super(controlContext);
	this.app = app;
	//	this.controlContext = controlContext;
	this.callback = callback;
	this.strings = strings;
    }

    void setMode(Mode mode)
    {
	this.mode = mode;
	context.onAreaNewContent(this);
    }

        void setPlaylistTitle(String value)
    {
	this.playlistTitle = value;
	context.onAreaNewContent(this);
    }

    void setTrackTitle(String value)
    {
	this.trackTitle = value;
	context.onAreaNewContent(this);
    }

    void setTrackTime(long msec)
    {
	if (msec < 0)
	    throw new IllegalArgumentException("msec (" + msec + ") may not be negative");
	final long sec = msec / 1000;
	if (sec != timeSec)
	{
	    timeSec = sec;
	    context.onAreaNewContent(this);
	}
    }

    @Override public int getLineCount()
    {
	switch(mode)
	{
	case STOPPED:
	    return 1;
	case PLAYING_STREAMING:
	    return 2;
	case PLAYING:
	case PAUSED:
	    if (!playlistTitle.isEmpty())
		return 5;
	    return 4;
	default:
	    return 1;
	}
    }

    @Override public String getLine(int index)
    {
	if (index < 0)
	    return "";
	switch(mode)
	{
	case STOPPED:
	    return "";
	case PLAYING_STREAMING:
	    return index == 0?playlistTitle:"";
	case PLAYING:
	case PAUSED:
	    switch(index)
	    {
	    case 0:
		return "";
	    case 1:
	    return Utils.getTimeStr(timeSec);
	    case 2:
		return trackTitle;
	    case 3:
		return playlistTitle;
	    default:
		return "";
	    }
	default:
	    return "";
	}
    }

    @Override public boolean onInputEvent(InputEvent event)
    {
	if (event.isSpecial() && !event.isModified())
	    switch(event.getSpecial())
	    {
	    case ARROW_LEFT:
		return app.getPlayer().jump(-1 * MainLayout.STEP_JUMP);
			    case ARROW_RIGHT:
		return app.getPlayer().jump(MainLayout.STEP_JUMP);
	    case ARROW_UP:
		return app.getPlayer().prevTrack();
	    case ARROW_DOWN:
		return app.getPlayer().nextTrack();
	    }
	return super.onInputEvent(event);
    }

    @Override public String getAreaName()
    {
	return strings.controlAreaName();
    }
    }
