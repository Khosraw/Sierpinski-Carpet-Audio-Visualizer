/**
 * SierpinskiAudioVisualizerApp.java
 * Entry point for the Sierpinski's Carpet Audio Visualizer application.
 * This class creates a GUI for the user to select an audio file and the number of bands for the
 * audio visualizer. When the user clicks the "Start Visualization" button, the audio file is
 * processed and the audio visualizer is displayed. The audio visualizer consists of a fractal
 * image for each band, where the depth of the fractal is determined by the volume of the audio
 * in that band.
 *
 * @version 1.2
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
import java.io.File;

public class SierpinskiAudioVisualizerApp {

    public static void main(String[] args) {
        try {
            // Set Nimbus Look and Feel for a modern UI
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.out.println("Could not set Look and Feel");
        }

        JFrame frame = new JFrame("Sierpinski's Carpet Audio Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel with GridBagLayout for flexible component arrangement
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(40, 44, 52)); // Dark background color
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15); // Padding around components
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // File Selection Components
        JLabel fileLabel = new JLabel("Audio File:");
        fileLabel.setForeground(Color.WHITE);
        fileLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        JTextField fileTextField = new JTextField(25);
        fileTextField.setEditable(false);
        fileTextField.setBackground(Color.WHITE);
        fileTextField.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton fileButton = new JButton("Browse");
        fileButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        fileButton.setBackground(new Color(61, 174, 233)); // Light blue
        fileButton.setForeground(Color.WHITE);
        fileButton.setFocusPainted(false);
        fileButton.setToolTipText("Select an audio file to visualize");

        // Panel for file selection
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filePanel.setBackground(new Color(40, 44, 52));
        filePanel.add(fileLabel);
        filePanel.add(fileTextField);
        filePanel.add(fileButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(filePanel, gbc);

        // Number of Bands Components
        JLabel bandLabel = new JLabel("Number of Bands (1-20):");
        bandLabel.setForeground(Color.WHITE);
        bandLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 20, 1);
        JSpinner bandSpinner = new JSpinner(spinnerModel);
        bandSpinner.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Panel for band selection
        JPanel bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bandPanel.setBackground(new Color(40, 44, 52));
        bandPanel.add(bandLabel);
        bandPanel.add(bandSpinner);

        gbc.gridy++;
        mainPanel.add(bandPanel, gbc);

        // Control Buttons
        JButton startButton = new JButton("Start Visualization");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        startButton.setBackground(new Color(46, 204, 113)); // Green
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setToolTipText("Start visualizing the selected audio file");

        JButton liveModeButton = new JButton("Live Visualization");
        liveModeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        liveModeButton.setBackground(new Color(155, 89, 182)); // Purple
        liveModeButton.setForeground(Color.WHITE);
        liveModeButton.setFocusPainted(false);
        liveModeButton.setToolTipText("Start live audio visualization");

        JButton stopButton = new JButton("Stop");
        stopButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        stopButton.setBackground(new Color(231, 76, 60)); // Red
        stopButton.setForeground(Color.WHITE);
        stopButton.setFocusPainted(false);
        stopButton.setEnabled(false);
        stopButton.setToolTipText("Stop the visualization");

        // Panel for control buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonsPanel.setBackground(new Color(40, 44, 52));
        buttonsPanel.add(startButton);
        buttonsPanel.add(liveModeButton);
        buttonsPanel.add(stopButton);

        gbc.gridy++;
        mainPanel.add(buttonsPanel, gbc);

        // Status Bar
        JLabel statusLabel = new JLabel("Welcome! Please select an audio file to begin.");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(33, 37, 43));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        // Add main panel and status bar to the frame
        frame.add(mainPanel, BorderLayout.NORTH);
        frame.add(statusPanel, BorderLayout.SOUTH);

        // Set frame properties
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);

        final String[] selectedFile = new String[1];
        final AudioProcessor[] audioProcessor = new AudioProcessor[1];

        // Event listeners
        fileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select an Audio File");
            int returnVal = fileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                selectedFile[0] = file.getAbsolutePath();
                fileTextField.setText(file.getName());
                statusLabel.setText("Selected file: " + file.getName());
            }
        });

        startButton.addActionListener(e -> {
            if (selectedFile[0] == null) {
                JOptionPane.showMessageDialog(frame, "Please select an audio file.", "No File Selected", JOptionPane.WARNING_MESSAGE);
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
                    JOptionPane.showMessageDialog(frame, "An error occurred during audio processing.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            audioThread.start();

            fileButton.setEnabled(false);
            bandSpinner.setEnabled(false);
            startButton.setEnabled(false);
            liveModeButton.setEnabled(false);
            stopButton.setEnabled(true);
            statusLabel.setText("Visualization running...");
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
                    JOptionPane.showMessageDialog(frame, "An error occurred during live audio processing.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            liveThread.start();

            fileButton.setEnabled(false);
            bandSpinner.setEnabled(false);
            startButton.setEnabled(false);
            liveModeButton.setEnabled(false);
            stopButton.setEnabled(true);
            statusLabel.setText("Live visualization running...");
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
            statusLabel.setText("Visualization stopped.");
        });
    }
}