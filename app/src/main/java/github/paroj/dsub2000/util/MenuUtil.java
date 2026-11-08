/*
	This file is part of Subsonic.
	Subsonic is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	Subsonic is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU General Public License for more details.
	You should have received a copy of the GNU General Public License
	along with Subsonic. If not, see <http://www.gnu.org/licenses/>.
	Copyright 2015 (C) Scott Jackson
*/

package github.paroj.dsub2000.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;

import java.io.File;

import github.paroj.dsub2000.R;
import github.paroj.dsub2000.domain.MusicDirectory;
import github.paroj.dsub2000.domain.ServerInfo;
import github.paroj.dsub2000.service.DownloadFile;
import github.paroj.dsub2000.view.AlbumView;
import github.paroj.dsub2000.view.ArtistEntryView;
import github.paroj.dsub2000.view.ArtistView;
import github.paroj.dsub2000.view.SongView;
import github.paroj.dsub2000.view.UpdateView;

public final class MenuUtil {
	private final static String TAG = MenuUtil.class.getSimpleName();

	private static boolean isSongView(UpdateView updateView) {
		if (!(updateView instanceof SongView)) {
			return false;
		}

		MusicDirectory.Entry entry = ((SongView) updateView).getEntry();
		return entry == null || (!entry.isPodcast() && !entry.isVideo());
	}

	public static boolean showSongStar(SharedPreferences prefs) {
		return prefs.getBoolean(Constants.PREFERENCES_KEY_SONG_MENU_STAR,
				prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_STAR, true));
	}

	public static boolean showSongShare(SharedPreferences prefs) {
		return prefs.getBoolean(Constants.PREFERENCES_KEY_SONG_MENU_SHARED,
				prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_SHARED, true));
	}

	public static boolean showSongRating(SharedPreferences prefs) {
		return prefs.getBoolean(Constants.PREFERENCES_KEY_SONG_MENU_RATING,
				prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_RATING, true));
	}

	public static boolean showAlbumStar(SharedPreferences prefs) {
		return prefs.getBoolean(Constants.PREFERENCES_KEY_ALBUM_MENU_STAR,
				prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_STAR, true));
	}

	public static boolean showAlbumShare(SharedPreferences prefs) {
		return prefs.getBoolean(Constants.PREFERENCES_KEY_ALBUM_MENU_SHARED,
				prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_SHARED, true));
	}

	public static boolean showAlbumRating(SharedPreferences prefs) {
		return prefs.getBoolean(Constants.PREFERENCES_KEY_ALBUM_MENU_RATING,
				prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_RATING, true));
	}

	public static void hideMenuItems(Context context, Menu menu, UpdateView updateView) {
		if(!ServerInfo.checkServerVersion(context, "1.8")) {
			menu.setGroupVisible(R.id.server_1_8, false);
			menu.setGroupVisible(R.id.hide_star, false);
		}
		if(!ServerInfo.checkServerVersion(context, "1.9")) {
			menu.setGroupVisible(R.id.server_1_9, false);
		}
		if(!ServerInfo.checkServerVersion(context, "1.10.1")) {
			menu.setGroupVisible(R.id.server_1_10, false);
		}

		SharedPreferences prefs = Util.getPreferences(context);
		if(!prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_PLAY_NOW, true)) {
			menu.setGroupVisible(R.id.hide_play_now, false);
		}
		if(!prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_PLAY_SHUFFLED, true)) {
			menu.setGroupVisible(R.id.hide_play_shuffled, false);
		}
		if(!prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_PLAY_NEXT, false)) {
			menu.setGroupVisible(R.id.hide_play_next, false);
		}
		if(!prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_PLAY_LAST, true)) {
			menu.setGroupVisible(R.id.hide_play_last, false);
		}
		if(!prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_DOWNLOAD, false)) {
			menu.setGroupVisible(R.id.hide_download, false);
		}
		if(!prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_PIN, false)) {
			menu.setGroupVisible(R.id.hide_pin, false);
		}
		if(!prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_DELETE, false)) {
			menu.setGroupVisible(R.id.hide_delete, false);
		}
		boolean songMenu = isSongView(updateView);
		boolean albumMenu = updateView instanceof AlbumView
				&& ((AlbumView) updateView).usesAlbumMenuOptions();
		boolean showStar = albumMenu ? showAlbumStar(prefs)
				: songMenu ? showSongStar(prefs)
				: prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_STAR, true);
		boolean showShare = albumMenu ? showAlbumShare(prefs)
				: songMenu ? showSongShare(prefs)
				: prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_SHARED, true);
		boolean showRating = albumMenu ? showAlbumRating(prefs)
				: songMenu ? showSongRating(prefs)
				: prefs.getBoolean(Constants.PREFERENCES_KEY_MENU_RATING, true);
		if(!showStar) {
			menu.setGroupVisible(R.id.hide_star, false);
		}
		if(!showShare || !UserUtil.canShare()) {
			menu.setGroupVisible(R.id.hide_share, false);
		}
		if(!showRating) {
			menu.setGroupVisible(R.id.hide_rating, false);
		}

		if(!Util.isOffline(context)) {
			// If we are looking at a standard song view, get downloadFile to cache what options to show
			if(updateView instanceof SongView) {
				SongView songView = (SongView) updateView;
				DownloadFile downloadFile = songView.getDownloadFile();

				try {
					if(downloadFile != null) {
						if(downloadFile.isWorkDone()) {
							// Remove permanent cache menu if already perma cached
							if(downloadFile.isSaved()) {
								menu.setGroupVisible(R.id.hide_pin, false);
							}

							// Remove cache option no matter what if already downloaded
							menu.setGroupVisible(R.id.hide_download, false);
						} else {
							// Remove delete option if nothing to delete
							menu.setGroupVisible(R.id.hide_delete, false);
						}
					}
				} catch(Exception e) {
					Log.w(TAG, "Failed to lookup downloadFile info", e);
				}
			}
			// Apply similar logic to album views
			else if(updateView instanceof AlbumView || updateView instanceof ArtistView || updateView instanceof ArtistEntryView) {
				File folder = null;
				if(updateView instanceof AlbumView) {
					folder = ((AlbumView) updateView).getFile();
				} else if(updateView instanceof ArtistView) {
					folder = ((ArtistView) updateView).getFile();
				} else if(updateView instanceof ArtistEntryView) {
					folder = ((ArtistEntryView) updateView).getFile();
				}

				try {
					if(folder != null && !folder.exists()) {
						menu.setGroupVisible(R.id.hide_delete, false);
					}
				} catch(Exception e) {
					Log.w(TAG, "Failed to lookup album directory info", e);
				}
			}
		}
	}
}
