import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class VisualizerPanel extends JPanel {

    private BufferedImage[] fractalImages;
    private int[] currentDepths;
    private int numBands;

    public VisualizerPanel(int numBands) {
        this.numBands = numBands;
        fractalImages = new BufferedImage[numBands];
        currentDepths = new int[numBands];

        Timer drawingTimer = new Timer(5, e -> repaint());
        drawingTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);
        Graphics2D g2d = (Graphics2D) g;

        int panelWidth = getWidth() / numBands;
        for (int i = 0; i < numBands; i++) {
            if (fractalImages[i] == null && currentDepths[i] > 0) {
                fractalImages[i] = FractalGenerator.generateFractalImage(
                    currentDepths[i],
                    panelWidth,
                    getHeight(),
                    i
                );
            }
            if (fractalImages[i] != null) {
                g2d.drawImage(
                    fractalImages[i],
                    i * panelWidth,
                    0,
                    panelWidth,
                    getHeight(),
                    null
                );
            }
        }
    }

    public void updateFractalImage(int bandIndex, int depth) {
        currentDepths[bandIndex] = depth;
        fractalImages[bandIndex] = FractalGenerator.generateFractalImage(
            depth,
            getWidth() / numBands,
            getHeight(),
            bandIndex
        );
    }
}
