package it.unibo.oop.reactivegui03;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;

/**
 * Third experiment with reactive gui.
 */
public final class AnotherConcurrentGUI extends JFrame {

    @Serial
    private static final int TIME_TO_STOP = 10_000;
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherConcurrentGUI.class);
    private final JLabel display = new JLabel();

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
        final JButton stop = new JButton("stop");
        final JButton up = new JButton("up");
        panel.add(up);
        final JButton down = new JButton("down");
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        final Agent agent = new Agent();

        //new agent
        new Thread(() -> {
            try {
                Thread.sleep(TIME_TO_STOP);
                SwingUtilities.invokeLater(stop::doClick);
            } catch (final InterruptedException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }).start();

        new Thread(agent).start();
        up.addActionListener(e -> agent.upCounting());
        down.addActionListener(e -> agent.downCounting());
        stop.addActionListener(e -> {
            agent.stopCounting();
            stop.setEnabled(false);
            up.setEnabled(false);
            down.setEnabled(false);
        });
    }

    private final class Agent implements Runnable {

        private volatile boolean stop;
        private volatile boolean dir = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    if (dir) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        public void stopCounting() {
            this.stop = true;
        }

        public void upCounting() {
            this.dir = true;
        }

        public void downCounting() {
            this.dir = false;
        }
    }
}
