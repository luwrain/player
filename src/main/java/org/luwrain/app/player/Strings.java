/*
   Copyright 2012-2024 Michael Pozhidaev <msp@luwrain.org>

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

public interface Strings
{
static final String NAME = "luwrain.player";

        String actionAddAlbum();
    String albumDeletingPopupName();
    String albumDeletingPopupText(String albumName);
    String albumsAreaName();
    String albumTypeDir();
    String albumTypeM3u();
    String albumTypeSection();
    String albumTypeStreaming();
    String appName();
    String controlAreaName(); 
    String newAlbumPopupName();
    String newAlbumTitlePopupPrefix();
    String newAlbumTypePopupName();
    String newSectionPopupName();
    String newSectionTitlePopupPrefix();
    String playlistAreaName();

    String actionPauseResume();
    String actionPrevTrack();
    String actionNextTrack();
    String actionVolumePlus();
    String actionVolumeMinus();
    String actionJumpForward();
    String actionJumpBackward();

    String albumPropTitle();
    String albumPropUrl();
    String albumPropPath();
    String albumPropSavePosition();
    String albumPropTitleCannotBeEmpty();
}
