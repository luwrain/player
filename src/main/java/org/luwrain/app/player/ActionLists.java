
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
