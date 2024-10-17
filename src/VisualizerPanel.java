import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class VisualizerPanel extends JPanel {

    private final BufferedImage[] fractalImages = new BufferedImage[Constants.NUM_BANDS];
    private final int[] currentDepths = new int[Constants.NUM_BANDS];

    public VisualizerPanel() {
        Timer drawingTimer = new Timer(5, e -> repaint());
        drawingTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);
        Graphics2D g2d = (Graphics2D) g;

        int panelWidth = getWidth() / Constants.NUM_BANDS;
        for (int i = 0; i < Constants.NUM_BANDS; i++) {
            if (fractalImages[i] == null && currentDepths[i] > 0) {
                fractalImages[i] = FractalGenerator.generateFractalImage(currentDepths[i],
                        getWidth() / Constants.NUM_BANDS, getHeight(), i);
            }
            if (fractalImages[i] != null) {
                g2d.drawImage(fractalImages[i], 0, 0, panelWidth, getHeight(), null);
            }
        }
    }

    public void updateFractalImage(int bandIndex, int depth) {
        currentDepths[bandIndex] = depth;
        fractalImages[bandIndex] = FractalGenerator.generateFractalImage(depth,
                getWidth() / Constants.NUM_BANDS, getHeight(), bandIndex);
    }
}
