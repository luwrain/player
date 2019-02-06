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

public interface Strings
{
static final String NAME = "luwrain.player";

    String appName();
    String treeAreaName();
    String playlistAreaName();
    String controlAreaName(); 
    String opPauseResume();
    String opStop();
    String opNextTrack();
    String opPrevTrack();
    String playlistPropertiesAreaName();
    String playlistPropertiesAreaTitle();
    String playlistPropertiesAreaUrl();
    String badPlaylistPath(String value);
    String errorLoadingPlaylist(String path);
    String actionAddPlaylistWithoutBookmark();
    String actionAddPlaylistWithBookmark();
    String actionAddStreamingPlaylist();
    String addPlaylistPopupName();
    String addPlaylistPopupPrefix();
    String choosePlaylistFilePopupName();
    String choosePlaylistFilePopupPrefix();
    String playlistFileMayNotBeDir(String path);
    String playlistTitleMayNotBeEmpty();
    String treeRoot();
    String treePlaylistsWithBookmarks();
    String treePlaylistsWithoutBookmarks();
    String treeStreamingPlaylists();
}
