package github.paroj.dsub2000.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import github.paroj.dsub2000.util.ArtworkColorExtractor;

public class ArtworkBackdropDrawable extends Drawable {
	private static final int BLUR_RADIUS = 9;
	private static final int SURFACE_OVERLAY_ALPHA = 0x40;
	private static final int BLURRED_ARTWORK_ALPHA = 0x55;
	private static final float DOMINANT_COLOR_WEIGHT = 0.55f;

	private final Bitmap blurredArtwork;
	private final int surfaceColor;
	private final int[] dominantColors;
	private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Rect bitmapBounds = new Rect();
	private LinearGradient colorGradient;

	private ArtworkBackdropDrawable(Bitmap blurredArtwork, int surfaceColor, int[] dominantColors) {
		this.blurredArtwork = blurredArtwork;
		this.surfaceColor = surfaceColor;
		this.dominantColors = dominantColors;
	}

	public static ArtworkBackdropDrawable create(int[] pixels, int width, int height, int surfaceColor) {
		int[] dominantColors = ArtworkColorExtractor.extract(pixels);
		int[] blurredPixels = blur(pixels, width, height, BLUR_RADIUS);
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(blurredPixels, 0, width, 0, 0, width, height);
		return new ArtworkBackdropDrawable(bitmap, surfaceColor, dominantColors);
	}

	@Override
	public void draw(Canvas canvas) {
		Rect bounds = getBounds();
		if(bounds.isEmpty()) {
			return;
		}

		float scale = Math.max(bounds.width() / (float) blurredArtwork.getWidth(),
				bounds.height() / (float) blurredArtwork.getHeight());
		float drawnWidth = blurredArtwork.getWidth() * scale;
		float drawnHeight = blurredArtwork.getHeight() * scale;
		float left = bounds.left + (bounds.width() - drawnWidth) / 2f;
		float top = bounds.top + (bounds.height() - drawnHeight) / 2f;
		bitmapBounds.set(Math.round(left), Math.round(top), Math.round(left + drawnWidth),
				Math.round(top + drawnHeight));

		int saveCount = canvas.save();
		canvas.clipRect(bounds);
		if(colorGradient == null) {
			colorGradient = new LinearGradient(0, bounds.top, 0, bounds.bottom,
					blend(surfaceColor, dominantColors[0]),
					blend(surfaceColor, dominantColors[1]), Shader.TileMode.CLAMP);
		}
		overlayPaint.setShader(colorGradient);
		overlayPaint.setColor(Color.WHITE);
		canvas.drawRect(bounds, overlayPaint);
		overlayPaint.setShader(null);
		bitmapPaint.setAlpha(BLURRED_ARTWORK_ALPHA);
		canvas.drawBitmap(blurredArtwork, null, bitmapBounds, bitmapPaint);
		overlayPaint.setColor(withAlpha(surfaceColor, SURFACE_OVERLAY_ALPHA));
		canvas.drawRect(bounds, overlayPaint);
		canvas.restoreToCount(saveCount);
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		colorGradient = null;
	}

	@Override
	public void setAlpha(int alpha) {
		bitmapPaint.setAlpha(alpha);
		overlayPaint.setAlpha(alpha);
		invalidateSelf();
	}

	@Override
	public void setColorFilter(ColorFilter colorFilter) {
		bitmapPaint.setColorFilter(colorFilter);
		overlayPaint.setColorFilter(colorFilter);
		invalidateSelf();
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	private static int withAlpha(int color, int alpha) {
		return (color & 0x00FFFFFF) | (alpha << 24);
	}

	private static int blend(int surfaceColor, int artworkColor) {
		float surfaceWeight = 1f - DOMINANT_COLOR_WEIGHT;
		return Color.rgb(
				Math.round(Color.red(surfaceColor) * surfaceWeight + Color.red(artworkColor) * DOMINANT_COLOR_WEIGHT),
				Math.round(Color.green(surfaceColor) * surfaceWeight + Color.green(artworkColor) * DOMINANT_COLOR_WEIGHT),
				Math.round(Color.blue(surfaceColor) * surfaceWeight + Color.blue(artworkColor) * DOMINANT_COLOR_WEIGHT));
	}

	private static int[] blur(int[] source, int width, int height, int radius) {
		int[] horizontal = new int[source.length];
		int[] result = new int[source.length];
		blurRows(source, horizontal, width, height, radius);
		blurColumns(horizontal, result, width, height, radius);
		blurRows(result, horizontal, width, height, radius);
		blurColumns(horizontal, result, width, height, radius);
		return result;
	}

	private static void blurRows(int[] source, int[] target, int width, int height, int radius) {
		int window = radius * 2 + 1;
		for(int y = 0; y < height; y++) {
			int row = y * width;
			int red = 0;
			int green = 0;
			int blue = 0;
			for(int x = -radius; x <= radius; x++) {
				int color = source[row + clamp(x, 0, width - 1)];
				red += (color >>> 16) & 0xFF;
				green += (color >>> 8) & 0xFF;
				blue += color & 0xFF;
			}
			for(int x = 0; x < width; x++) {
				target[row + x] = 0xFF000000 | ((red / window) << 16)
						| ((green / window) << 8) | (blue / window);
				int remove = source[row + clamp(x - radius, 0, width - 1)];
				int add = source[row + clamp(x + radius + 1, 0, width - 1)];
				red += ((add >>> 16) & 0xFF) - ((remove >>> 16) & 0xFF);
				green += ((add >>> 8) & 0xFF) - ((remove >>> 8) & 0xFF);
				blue += (add & 0xFF) - (remove & 0xFF);
			}
		}
	}

	private static void blurColumns(int[] source, int[] target, int width, int height, int radius) {
		int window = radius * 2 + 1;
		for(int x = 0; x < width; x++) {
			int red = 0;
			int green = 0;
			int blue = 0;
			for(int y = -radius; y <= radius; y++) {
				int color = source[clamp(y, 0, height - 1) * width + x];
				red += (color >>> 16) & 0xFF;
				green += (color >>> 8) & 0xFF;
				blue += color & 0xFF;
			}
			for(int y = 0; y < height; y++) {
				target[y * width + x] = 0xFF000000 | ((red / window) << 16)
						| ((green / window) << 8) | (blue / window);
				int remove = source[clamp(y - radius, 0, height - 1) * width + x];
				int add = source[clamp(y + radius + 1, 0, height - 1) * width + x];
				red += ((add >>> 16) & 0xFF) - ((remove >>> 16) & 0xFF);
				green += ((add >>> 8) & 0xFF) - ((remove >>> 8) & 0xFF);
				blue += (add & 0xFF) - (remove & 0xFF);
			}
		}
	}

	private static int clamp(int value, int minimum, int maximum) {
		return Math.max(minimum, Math.min(maximum, value));
	}
}
