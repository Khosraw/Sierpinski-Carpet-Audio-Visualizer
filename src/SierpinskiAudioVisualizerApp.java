/**
 * SierpinskiAudioVisualizerApp.java
 * Entry point for the Sierpinski's Carpet Audio Visualizer application.
 * This class creates a GUI for the user to select an audio file and the number of bands for the
 * audio visualizer. When the user clicks the "Start Visualization" button, the audio file is
 * processed and the audio visualizer is displayed. The audio visualizer consists of a fractal
 * image for each band, where the depth of the fractal is determined by the volume of the audio
 * in that band.
 *
 * @version 1.0
 * @author Khosraw Azizi <UT CS>
 * @since 2024-10-16
 * @see AudioProcessor
 * @see VisualizerPanel
 * @see Constants
 * @see FractalGenerator
 * @see AudioUtils
 * @see FFT
 * Email: khosraw.azizi@gmail.com
 * GitHub: github.com/khosraw
 * LinkedIn: linkedin.com/in/khosraw-azizi
 */

import javax.swing.*;
import java.awt.*;

public class SierpinskiAudioVisualizerApp {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Sierpinski's Carpet Audio Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel inputPanel = new JPanel(new GridLayout(5, 1));

        JButton fileButton = new JButton("Choose Audio File");
        JTextField fileTextField = new JTextField(30);
        fileTextField.setEditable(false);

        JPanel filePanel = new JPanel();
        filePanel.add(fileButton);
        filePanel.add(fileTextField);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 20, 1);
        JSpinner bandSpinner = new JSpinner(spinnerModel);
        JLabel bandLabel = new JLabel("Number of Bands (1-20):");

        JPanel bandPanel = new JPanel();
        bandPanel.add(bandLabel);
        bandPanel.add(bandSpinner);

        JButton startButton = new JButton("Start Visualization");
        JButton liveModeButton = new JButton("Start Live Visualization");
        JButton stopButton = new JButton("Stop Visualization");
        stopButton.setEnabled(false);

        inputPanel.add(filePanel);
        inputPanel.add(bandPanel);
        inputPanel.add(startButton);
        inputPanel.add(liveModeButton);
        inputPanel.add(stopButton);

        frame.add(inputPanel, BorderLayout.NORTH);

        frame.setSize(1000, 1100);
        frame.setVisible(true);

        final String[] selectedFile = new String[1];
        final AudioProcessor[] audioProcessor = new AudioProcessor[1];

        fileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnVal = fileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = fileChooser.getSelectedFile().getAbsolutePath();
                fileTextField.setText(selectedFile[0]);
            }
        });

        startButton.addActionListener(e -> {
            if (selectedFile[0] == null) {
                JOptionPane.showMessageDialog(frame, "Please select an audio file.");
                return;
            }

            if (audioProcessor[0] != null) {
                audioProcessor[0].stop();
            }

            int numBands = (Integer) bandSpinner.getValue();
            Constants.setNumBands(numBands);

            VisualizerPanel visualizerPanel = new VisualizerPanel(numBands);
            frame.add(visualizerPanel, BorderLayout.CENTER);
            frame.revalidate();
            frame.repaint();

            Thread audioThread = new Thread(() -> {
                try {
                    audioProcessor[0] = new AudioProcessor(visualizerPanel);
                    audioProcessor[0].preprocessAudio(selectedFile[0]);
                    audioProcessor[0].processAudio(selectedFile[0]);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            audioThread.start();

            fileButton.setEnabled(false);
            bandSpinner.setEnabled(false);
            startButton.setEnabled(false);
            liveModeButton.setEnabled(false);
            stopButton.setEnabled(true);
        });

        liveModeButton.addActionListener(e -> {
            if (audioProcessor[0] != null) {
                audioProcessor[0].stop();
            }

            int numBands = (Integer) bandSpinner.getValue();
            Constants.setNumBands(numBands);

            VisualizerPanel visualizerPanel = new VisualizerPanel(numBands);
            frame.add(visualizerPanel, BorderLayout.CENTER);
            frame.revalidate();
            frame.repaint();

            Thread liveThread = new Thread(() -> {
                try {
                    audioProcessor[0] = new AudioProcessor(visualizerPanel);
                    audioProcessor[0].processLiveAudio();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            liveThread.start();

            fileButton.setEnabled(false);
            bandSpinner.setEnabled(false);
            startButton.setEnabled(false);
            liveModeButton.setEnabled(false);
            stopButton.setEnabled(true);
        });

        stopButton.addActionListener(e -> {
            if (audioProcessor[0] != null) {
                audioProcessor[0].stop();
            }
            fileButton.setEnabled(true);
            bandSpinner.setEnabled(true);
            startButton.setEnabled(true);
            liveModeButton.setEnabled(true);
            stopButton.setEnabled(false);
        });
    }
}