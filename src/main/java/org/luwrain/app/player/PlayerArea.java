/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

import java.net.URL;                                                         
                                                                             
import javafx.application.Application;                                       
import javafx.scene.media.Media;                                             
import javafx.scene.media.MediaPlayer;                                       
import javafx.stage.Stage;                                                   


import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;

class PlayerArea extends SimpleArea
{
    private Luwrain luwrain;
    private Actions actions;
    private Strings strings;

    PlayerArea(Luwrain luwrain, Actions actions, Strings strings)
    {
	super(new DefaultControlEnvironment(luwrain), strings.appName());
	this.luwrain = luwrain;
	this.actions = actions;
	this.strings = strings;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(strings, "strigns");
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch(event.getCode())
	{
	case EnvironmentEvent.OK:
	    play();
	    return true;
	case EnvironmentEvent.CLOSE:
	    actions.closeApp();
	    return true;
	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    private void play()
    {
    final Media media = new Media("file:/tmp/proba.mp3");                      
    final MediaPlayer mediaPlayer = new MediaPlayer(media);                  
    mediaPlayer.play();                                                      


    }
}
