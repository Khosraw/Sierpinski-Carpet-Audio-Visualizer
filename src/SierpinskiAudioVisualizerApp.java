import javax.swing.*;

public class SierpinskiAudioVisualizerApp {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please provide a WAV file.");
            System.exit(1);
        }

        String filename = args[0];
        VisualizerPanel visualizerPanel = new VisualizerPanel();

        JFrame frame = new JFrame("Sierpinski's Carpet Audio Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(visualizerPanel);
        frame.setSize(1100, 1100);
        frame.setVisible(true);

        new Thread(() -> {
            try {
                AudioProcessor audioProcessor = new AudioProcessor(visualizerPanel);
                audioProcessor.preprocessAudio(filename);
                audioProcessor.processAudio(filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
