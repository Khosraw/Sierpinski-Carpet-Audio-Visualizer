import java.awt.*;
import java.awt.image.BufferedImage;

public class FractalGenerator {

    private static final Color[] squareColors = {Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.RED,
            Color.GREEN, Color.BLUE, Color.ORANGE, Color.PINK};
    private static final Color[] backgroundColors = new Color[255];

    static {
        // random colors for background
        for (int i = 0; i < 255; i++) {
            backgroundColors[i] = getColorForDepth();
        }
    }

    public static BufferedImage generateFractalImage(int depth, int width, int height,
                                                     int bandIndex) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        int colorIndex = (depth + bandIndex) % backgroundColors.length;
        Color randomColor = backgroundColors[colorIndex];
        g2d.setColor(randomColor);
        g2d.fillRect(0, 0, width, height);
        drawCarpet(g2d, 0, 0, width, depth, depth);
        g2d.dispose();
        return image;
    }

    private static void drawCarpet(Graphics g, int x, int y, int size, int depth, int maxDepth) {
        if (depth == 0) {
            return;
        }
        g.setColor(getColorForDepth());
        g.fillRect(x, y, size, size);

        int newSize = size / 3;
        int normalizedDepthZeroToColorLength = (maxDepth - depth) % squareColors.length;
        g.setColor(squareColors[normalizedDepthZeroToColorLength]);
        g.fillRect(x + newSize, y + newSize, newSize, newSize);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 1 && j == 1) continue;
                drawCarpet(g, x + i * newSize, y + j * newSize, newSize, depth - 1,
                        maxDepth);
            }
        }
    }

    private static Color getColorForDepth() {
        int r = (int) (Math.random() * 255);
        int g = (int) (Math.random() * 255);
        int b = (int) (Math.random() * 255);
        return new Color(r, g, b);
    }
}
