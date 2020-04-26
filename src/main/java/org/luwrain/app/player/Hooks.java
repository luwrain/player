/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>

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

final class Hooks
{
    private final Luwrain luwrain;

    Hooks(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    boolean onAlbumsInput(KeyboardEvent event, Object selected)
    {
	NullCheck.notNull(event, "event");
	if (selected == null)
	    return false;
	return luwrain.xRunHooks("luwrain.app.player.areas.albums.input", new Object[]{org.luwrain.script.ScriptUtils.createInputEvent(event), selected}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY);
	    }

boolean onPlaylistInput(KeyboardEvent event, Object selected)
{
    NullCheck.notNull(event, "event");
    if (selected == null)
	return false;
    return luwrain.xRunHooks("luwrain.app.player.areas.playlist.input", new Object[]{org.luwrain.script.ScriptUtils.createInputEvent(event), selected}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY);
	}

boolean onControlInput(KeyboardEvent event)
{
    NullCheck.notNull(event , "event");
    return luwrain.xRunHooks("luwrain.app.player.areas.control.input", new Object[]{org.luwrain.script.ScriptUtils.createInputEvent(event)}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY);
	}
}
