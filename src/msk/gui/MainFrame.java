package msk.gui;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

public class MainFrame extends JFrame {

    public static JLabel countLabel1 = new JLabel("0");
    public  static JLabel statusLabel = new JLabel("Task not completed.");
    public JButton startButton = new JButton("Start");

    public MainFrame(String title) {
        super(title);
        setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.NONE;

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.weighty = 1;
        add(countLabel1, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 1;
        gc.weighty = 1;
        add(statusLabel, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx = 1;
        gc.weighty = 1;

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                start();
            }
        });

        setSize(200, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void start() {
        SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                for(int i=0; i<3000; i++) {
                    Thread.sleep(100);
                    System.out.println("Hello: " + i);
                    publish(i);
                }

                return false;
            }

            @Override
            protected void process(List<Integer> chunks) {
                Integer value = chunks.get(chunks.size() - 1);

                countLabel1.setText("Current value: " + value);
            }

            @Override
            protected void done() {

                try {
                    Boolean status = get();
                    statusLabel.setText("Completed with status: " + status);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        };

        worker.execute();
    }
}