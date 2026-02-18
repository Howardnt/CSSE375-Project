package rhit.csse.csse374.linter.presentation.gui;

import javax.swing.*;

/**
 * GUI entry point.
 *
 * Run this class from the IDE to start the Swing UI.
 */
public final class LinterGuiMain {

    private LinterGuiMain() {
        // utility
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Prefer FlatLaf if available; otherwise fall back to system look-and-feel.
            try {
                Class<?> flatLight = Class.forName("com.formdev.flatlaf.FlatLightLaf");
                flatLight.getMethod("setup").invoke(null);
            } catch (Exception ignored) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored2) {
                    // fall back to default
                }
            }

            LinterGuiFrame frame = new LinterGuiFrame();
            frame.setVisible(true);
        });
    }
}

