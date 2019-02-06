/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

class ActionLists
{
    private final Strings strings;

    ActionLists(Strings strings)
    {
	NullCheck.notNull(strings, "strings");
	this.strings = strings;
    }

    Action[] getPlaylistsActions()
    {
	return new Action[]{
	    new Action("add-playlist", "Добавить плейлист", new KeyboardEvent(KeyboardEvent.Special.INSERT)),//FIXME:
	    new Action("delete-playlist", "Удалить плейлист", new KeyboardEvent(KeyboardEvent.Special.DELETE)),//FIXME:
	};
    }

    Action[] getPlaylistActions()
    {
	return new Action[]{
	};
    }
}
