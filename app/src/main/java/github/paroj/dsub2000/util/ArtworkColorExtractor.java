package github.paroj.dsub2000.util;

import java.util.Arrays;

public final class ArtworkColorExtractor {
	private static final int CHANNEL_BITS = 4;
	private static final int CHANNEL_LEVELS = 1 << CHANNEL_BITS;
	private static final int HISTOGRAM_SIZE = CHANNEL_LEVELS * CHANNEL_LEVELS * CHANNEL_LEVELS;
	private static final int MIN_COLOR_DISTANCE_SQUARED = 10800;

	private ArtworkColorExtractor() {
	}

	public static int[] extract(int[] pixels) {
		int[] counts = new int[HISTOGRAM_SIZE];
		long[] redSums = new long[HISTOGRAM_SIZE];
		long[] greenSums = new long[HISTOGRAM_SIZE];
		long[] blueSums = new long[HISTOGRAM_SIZE];

		for(int color : pixels) {
			int alpha = color >>> 24;
			if(alpha < 128) {
				continue;
			}
			int red = (color >>> 16) & 0xFF;
			int green = (color >>> 8) & 0xFF;
			int blue = color & 0xFF;
			int index = ((red >>> 4) << 8) | ((green >>> 4) << 4) | (blue >>> 4);
			counts[index]++;
			redSums[index] += red;
			greenSums[index] += green;
			blueSums[index] += blue;
		}

		int first = -1;
		for(int i = 0; i < counts.length; i++) {
			if(first < 0 || counts[i] > counts[first]) {
				first = i;
			}
		}
		if(first < 0 || counts[first] == 0) {
			return new int[] {0xFF808080, 0xFF808080};
		}

		int firstColor = averageColor(first, counts, redSums, greenSums, blueSums);
		int second = -1;
		for(int i = 0; i < counts.length; i++) {
			if(counts[i] == 0 || i == first) {
				continue;
			}
			int candidateColor = averageColor(i, counts, redSums, greenSums, blueSums);
			int distanceSquared = colorDistanceSquared(firstColor, candidateColor);
			if(distanceSquared < MIN_COLOR_DISTANCE_SQUARED) {
				continue;
			}
			if(second < 0 || counts[i] > counts[second]) {
				second = i;
			}
		}
		if(second < 0) {
			return new int[] {firstColor, firstColor};
		}

		int secondColor = averageColor(second, counts, redSums, greenSums, blueSums);
		long[] clusterCounts = new long[2];
		long[] clusterRed = new long[2];
		long[] clusterGreen = new long[2];
		long[] clusterBlue = new long[2];
		for(int iteration = 0; iteration < 8; iteration++) {
			Arrays.fill(clusterCounts, 0);
			Arrays.fill(clusterRed, 0);
			Arrays.fill(clusterGreen, 0);
			Arrays.fill(clusterBlue, 0);
			for(int i = 0; i < counts.length; i++) {
				if(counts[i] == 0) {
					continue;
				}
				int color = averageColor(i, counts, redSums, greenSums, blueSums);
				int cluster = colorDistanceSquared(color, firstColor) <= colorDistanceSquared(color, secondColor) ? 0 : 1;
				clusterCounts[cluster] += counts[i];
				clusterRed[cluster] += redSums[i];
				clusterGreen[cluster] += greenSums[i];
				clusterBlue[cluster] += blueSums[i];
			}
			if(clusterCounts[0] == 0 || clusterCounts[1] == 0) {
				return new int[] {firstColor, firstColor};
			}
			firstColor = averageClusterColor(0, clusterCounts, clusterRed, clusterGreen, clusterBlue);
			secondColor = averageClusterColor(1, clusterCounts, clusterRed, clusterGreen, clusterBlue);
		}
		if(colorDistanceSquared(firstColor, secondColor) < MIN_COLOR_DISTANCE_SQUARED) {
			return new int[] {firstColor, firstColor};
		}
		return clusterCounts[0] >= clusterCounts[1]
				? new int[] {firstColor, secondColor} : new int[] {secondColor, firstColor};
	}

	private static int averageColor(int index, int[] counts, long[] redSums, long[] greenSums, long[] blueSums) {
		int count = counts[index];
		return 0xFF000000
				| ((int) (redSums[index] / count) << 16)
				| ((int) (greenSums[index] / count) << 8)
				| (int) (blueSums[index] / count);
	}

	private static int averageClusterColor(int cluster, long[] counts, long[] redSums, long[] greenSums, long[] blueSums) {
		long count = counts[cluster];
		return 0xFF000000
				| ((int) (redSums[cluster] / count) << 16)
				| ((int) (greenSums[cluster] / count) << 8)
				| (int) (blueSums[cluster] / count);
	}

	private static int colorDistanceSquared(int first, int second) {
		int redDifference = ((first >>> 16) & 0xFF) - ((second >>> 16) & 0xFF);
		int greenDifference = ((first >>> 8) & 0xFF) - ((second >>> 8) & 0xFF);
		int blueDifference = (first & 0xFF) - (second & 0xFF);
		return 2 * redDifference * redDifference
				+ 4 * greenDifference * greenDifference
				+ 3 * blueDifference * blueDifference;
	}
}
