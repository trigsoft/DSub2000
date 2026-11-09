package github.paroj.dsub2000.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.TextView;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import github.paroj.dsub2000.R;
import github.paroj.dsub2000.domain.MusicDirectory.Entry;
import github.paroj.dsub2000.util.Constants;

import static org.junit.Assert.assertEquals;

public class SongViewTest {
	@Test
	public void albumNumbersUseFixedColumnWithoutLeadingZero() {
		InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
			Context context = testContext(true);
			SongView view = new SongView(context, true);
			Entry song = new Entry();
			song.setTitle("Song");
			song.setArtist("Artist");
			song.setTrack(1);
			song.setCustomOrder(9);
			view.setObjectImpl(song, true);

			TextView track = view.findViewById(R.id.album_song_track);
			assertEquals("1", track.getText().toString());
			assertEquals(View.VISIBLE, view.findViewById(R.id.album_song_track_column).getVisibility());
			assertEquals(View.GONE, view.findViewById(R.id.song_track).getVisibility());

			view.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
					View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
			view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
			Rect titleBounds = boundsIn(view, view.findViewById(R.id.song_title));
			Rect artistBounds = boundsIn(view, view.findViewById(R.id.song_artist));
			Rect numberBounds = boundsIn(view, track);
			assertEquals(titleBounds.left, artistBounds.left);
			assertEquals(view.getHeight() / 2, numberBounds.centerY(), 1);

			song.setTrack(null);
			view.setObjectImpl(song, true);
			assertEquals("", track.getText().toString());
			assertEquals(View.VISIBLE, view.findViewById(R.id.album_song_track_column).getVisibility());
		});
	}

	@Test
	public void trackPreferenceAndOtherSongListsRemainUnchanged() {
		InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
			Context context = testContext(false);
			Entry song = new Entry();
			song.setTitle("Song");
			song.setTrack(2);
			SongView album = new SongView(context, true);
			album.setObjectImpl(song, true);
			assertEquals(View.GONE, album.findViewById(R.id.album_song_track_column).getVisibility());

			context = testContext(true);
			SongView other = new SongView(context);
			other.setObjectImpl(song, true);
			assertEquals("02", ((TextView) other.findViewById(R.id.song_track)).getText().toString());
			assertEquals(View.GONE, other.findViewById(R.id.album_song_track_column).getVisibility());
		});
	}

	private Rect boundsIn(SongView parent, View child) {
		Rect bounds = new Rect();
		child.getDrawingRect(bounds);
		parent.offsetDescendantRectToMyCoords(child, bounds);
		return bounds;
	}

	private Context testContext(boolean showTrack) {
		Context test = InstrumentationRegistry.getInstrumentation().getContext();
		SharedPreferences preferences = test.getSharedPreferences("song-view-test", Context.MODE_PRIVATE);
		preferences.edit().putBoolean(Constants.PREFERENCES_KEY_DISPLAY_TRACK, showTrack).commit();
		return new ContextThemeWrapper(
				InstrumentationRegistry.getInstrumentation().getTargetContext(),
				R.style.Theme_DSub2000_Light) {
			@Override
			public SharedPreferences getSharedPreferences(String name, int mode) {
				return preferences;
			}
		};
	}
}
