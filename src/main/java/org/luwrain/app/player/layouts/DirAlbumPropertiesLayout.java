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

package org.luwrain.app.player.layouts;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;
import org.luwrain.app.player.*;

public final class DirAlbumPropertiesLayout extends LayoutBase
{
    static private final String
	TITLE = "title",
	PATH = "path",
	SAVE_POSITION = "save-pos";

    private final App app;
    private final FormArea formArea;

    public DirAlbumPropertiesLayout(App app, Album album, ActionHandler closing)
    {
	super(app);
	this.app = app;
	this.formArea = new FormArea(getControlContext(), album.getTitle()){
		@Override public boolean onInputEvent(InputEvent event)
		{
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ENTER:
			    if (getHotPointY() != 1)
				break;
			    {
				final File path = app.getConv().dirAlbumPath();
				if (path == null)
				    return true;
				formArea.setEnteredText(PATH, path.getAbsolutePath());
			    }
			}
		    return super.onInputEvent(event);
		}
	    };
	formArea.addEdit(TITLE, app.getStrings().albumPropTitle(), album.getTitle());
	formArea.addEdit(PATH, app.getStrings().albumPropPath(), album.getPath());
	formArea.addCheckbox(SAVE_POSITION, app.getStrings().albumPropSavePosition(), album.isSavePosition());
	setCloseHandler(closing);
	setOkHandler(()->onOk(album, closing));
	setAreaLayout(formArea, null);
    }

    private boolean onOk(Album album, ActionHandler closing)
    {
	final String
	title = formArea.getEnteredText(TITLE),
	path = formArea.getEnteredText(PATH);
	if (title.trim().isEmpty())
	{
	    app.message(app.getStrings().albumPropTitleCannotBeEmpty(), Luwrain.MessageType.ERROR);
	    return true;
	}
	album.setTitle(title.trim());
	album.setPath(path);
	album.setSavePosition(formArea.getCheckboxState(SAVE_POSITION));
	closing.onAction();
	return true;
    }
}
