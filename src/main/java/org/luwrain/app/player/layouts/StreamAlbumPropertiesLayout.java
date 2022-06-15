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

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;
import org.luwrain.app.player.*;

public final class StreamAlbumPropertiesLayout extends LayoutBase
{
    static private final String
	TITLE = "title",
	URL = "url";

    private final App app;
    private final FormArea formArea;

    public StreamAlbumPropertiesLayout(App app, Album album, ActionHandler closing)
    {
	super(app);
	this.app = app;
	this.formArea = new FormArea(getControlContext(), album.getTitle());
	formArea.addEdit(TITLE, app.getStrings().albumPropTitle(), album.getTitle());
	formArea.addEdit(URL, app.getStrings().albumPropUrl(), album.getUrl());
	setCloseHandler(closing);
	setOkHandler(()->onOk(album, closing));
	setAreaLayout(formArea, null);
    }

    private boolean onOk(Album album, ActionHandler closing)
    {
	final String
	title = formArea.getEnteredText(TITLE),
	url = formArea.getEnteredText(URL);
	if (title.trim().isEmpty())
	{
	    app.message(app.getStrings().albumPropTitleCannotBeEmpty(), Luwrain.MessageType.ERROR);
	    return true;
	}
	album.setTitle(title.trim());
	album.setUrl(url.trim());
	closing.onAction();
	return true;
    }
}
