package github.paroj.dsub2000.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArtworkColorExtractorTest {
	@Test
	public void testReturnsMostCommonDistinctArtworkColorsInFrequencyOrder() {
		int plum = 0xFF744C86;
		int sage = 0xFF73836A;
		int[] pixels = {plum, plum, plum, plum, plum, sage, sage, sage};

		int[] colors = ArtworkColorExtractor.extract(pixels);

		assertEquals(plum, colors[0]);
		assertEquals(sage, colors[1]);
	}

	@Test
	public void testMonochromeArtworkRemainsNeutral() {
		int[] colors = ArtworkColorExtractor.extract(new int[] {
				0xFF7E7E7E, 0xFF808080, 0xFF818181, 0xFF7F7F7F
		});

		assertEquals(colors[0], colors[1]);
		assertEquals((colors[0] >>> 16) & 0xFF, (colors[0] >>> 8) & 0xFF);
		assertEquals((colors[0] >>> 8) & 0xFF, colors[0] & 0xFF);
	}

	@Test
	public void testGrayIsNotDiscardedWhenItIsTheDominantColor() {
		int gray = 0xFF969696;
		int accent = 0xFF2E6D80;

		int[] colors = ArtworkColorExtractor.extract(new int[] {gray, gray, gray, accent});

		assertEquals(gray, colors[0]);
		assertEquals(accent, colors[1]);
	}
}
