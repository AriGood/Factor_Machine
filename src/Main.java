import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {
    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.setTitle("The Factor Machine");
        frame.setSize(1200, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        //end python on window close
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    Runtime.getRuntime().exec("taskkill /f /im python.exe");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}

    class MainFrame extends JFrame {
        private JTextField inputField;
        private JComboBox<String> algorithmSelector;
        private JButton runScriptButton;
        private JTable historyTable;
        private DefaultTableModel tableModel;
        private boolean taskRunning = false;
        private JTextField inputBits;
        private JButton runSemiprimeButton;
        private ScriptWorker scriptWorker;
        private JLabel workingLabel;
        private Process process;


        public MainFrame() {

            try {
                UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }


            // set up window
            setResizable(true);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();



            // Create a JTable to show the output history
            DefaultTableModel model = new DefaultTableModel();
            JTable outputTable = new JTable(model);
            model.addColumn("Input");
            model.addColumn("Algorithm");
            model.addColumn("Output");
            model.addColumn("Time");
            JScrollPane scrollPane = new JScrollPane(outputTable);
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 1;
            c.weighty = 1;
            add(scrollPane, c);
            outputTable.getColumnModel().getColumn(2).setCellRenderer(new CustomTableCellRenderer());

            // Create a panel for the input field and algorithm selection dropdown
            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));

            // add JLabel to display a message
            inputPanel.add(new JLabel("Enter a number: "));

            // add text field for input
            inputField = new JTextField();
            inputField.setPreferredSize(new Dimension(200, 30));
            inputPanel.add(inputField);

            // Add a document filter to the input field to only allow numeric characters
            ((AbstractDocument) inputField.getDocument()).setDocumentFilter(new NumericFilter(64));


            // add algorithm selection dropdown
            algorithmSelector = new JComboBox<>(new String[]{"ECM", "PollardRho", "SimpleFactor"});
            algorithmSelector.setPreferredSize(new Dimension(150, 30));
            inputPanel.add(algorithmSelector);

            // Create a JLabel to indicate that the program is working
            workingLabel = new JLabel("");
            inputPanel.add(workingLabel);

            // add button to run the script
            runScriptButton = new JButton("Run Script");
            runScriptButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (runScriptButton.getText().equals("Run Script")) {
                        // Disable the input field and the "Run Script" button
                        inputField.setEnabled(false);
                        // Create a new ScriptWorker task and execute it
                        String input = inputField.getText();
                        String algorithm = (String) algorithmSelector.getSelectedItem();
                        scriptWorker = new ScriptWorker(input, algorithm, model, workingLabel, inputField, runScriptButton);
                        scriptWorker.execute();
                        // Change the text of the button to "Cancel"
                        runScriptButton.setText("Cancel");
                    } else {
                        // Cancel the script and add "Terminated" to the output table
                        scriptWorker.cancel(true);
                        inputField.setEnabled(true);
                        runScriptButton.setText("Run Script");
                    }
                }
            });

            inputPanel.add(runScriptButton);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;
            add(inputPanel, c);

            // Create a panel for the semi-prime generation input and button
            JPanel semiprimePanel = new JPanel();
            semiprimePanel.setLayout(new BoxLayout(semiprimePanel, BoxLayout.X_AXIS));

            // add JLabel to display a message
            semiprimePanel.add(new JLabel("Enter number of bits: "));

            JTextField semiprimeField = new JTextField();
            semiprimeField.setPreferredSize(new Dimension(200, 30));
            semiprimePanel.add(semiprimeField);

            // add button to run the script
            JButton runSemiprimeButton = new JButton("Generate Semiprime");
            runSemiprimeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String input = semiprimeField.getText();
                    new SemiprimeWorker(input, inputField).execute();
                }
            });
            semiprimePanel.add(runSemiprimeButton);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 2;
            c.weightx = 1;
            c.weighty = 0;
            add(semiprimePanel, c);

            // center the window on the screen
            setLocationRelativeTo(null);
            // make the window visible
            setVisible(true);

            pack();

        }

        public class NumericFilter extends DocumentFilter {
            private int characterLimit;

            public NumericFilter(int characterLimit) {
                this.characterLimit = characterLimit;
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) {
                    return;
                }
                if (fb.getDocument().getLength() + string.length() <= characterLimit) {
                    super.insertString(fb, offset, string.replaceAll("[^\\d]", ""), attr);
                }
                else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) {
                    return;
                }
                if (fb.getDocument().getLength() + text.length() - length <= characterLimit) {
                    super.replace(fb, offset, length, text.replaceAll("[^\\d]", ""), attrs);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }





        public class CustomTableCellRenderer extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 2 && value.equals("Terminated")) {
                    Color myRed = new Color(241, 44, 44, 255);
                    c.setBackground(myRed);
                } else {
                    Color myGreen = new Color(6, 215, 47, 190);
                    c.setBackground(myGreen);
                }

                return c;
            }
        }


        private class SemiprimeWorker extends SwingWorker<String, Void> {
            private String input;
            private JTextField inputField;

            public SemiprimeWorker(String input, JTextField inputField) {
                this.input = input;
                this.inputField = inputField;
            }

            @Override
            protected String doInBackground() throws Exception {
                String command = "C:/Users/arigo/AppData/Local/Microsoft/WindowsApps/python3.10.exe \"RandomSemiprime.py\" " + input;
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
                builder.redirectErrorStream(true);
                Process p = builder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                String result = "";
                while (true) {
                    line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    result += line + "\n";
                }
                return result;
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    // update the inputField text with the output of the script
                    inputField.setText(result);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }


        class ScriptWorker extends SwingWorker<String, Void> {
            private String input;
            private String algorithm;
            private DefaultTableModel model;
            private JLabel workingLabel;
            private JTextField inputField;
            private JButton runScriptButton;
            private String output = "";
            private String time = "";
            private Process process;

            public ScriptWorker(String input, String algorithm, DefaultTableModel model, JLabel workingLabel, JTextField inputField, JButton runScriptButton) {
                this.input = input;
                this.algorithm = algorithm;
                this.model = model;
                this.workingLabel = workingLabel;
                this.inputField = inputField;
                this.runScriptButton = runScriptButton;
            }
            public void setProcess(Process process) {
                this.process = process;
            }

            @Override
            protected String doInBackground() throws IOException {
                String command = "";
                String output = "";
                String time = "";

                // Add variables for the path of the python executable and the base path of the script files
                String pythonPath = "C:/Users/arigo/AppData/Local/Microsoft/WindowsApps/python3.10.exe";


                // Use the switch statement to determine the command to run based on the selected algorithm
                switch (algorithm) {
                    case "ECM":
                        command = pythonPath + " \"Lester.py\" " + input;
                        break;
                    case "PollardRho":
                        command = pythonPath + " \"PollardRho.py\" " + input;
                        break;
                    case "SimpleFactor":
                        command = pythonPath + " \"SimpleFactor.py\" " + input;
                        break;
                }
                Process process = Runtime.getRuntime().exec(command);
                setProcess(process); // add this line

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String s;
                while ((s = stdInput.readLine()) != null) {
                    if(isCancelled()){
                        Runtime.getRuntime().exec("taskkill /f /im python.exe");
                        process.destroy();
                        break;
                    }
                    output += s + "\n";
                    time = stdInput.readLine();
                }
                if (isCancelled()) {
                    Runtime.getRuntime().exec("taskkill /f /im python.exe");
                    process.destroy();
                } else {
                    model.addRow(new Object[]{input, algorithm, output, time});
                }
                return output;
            }



            @Override
            protected void done() {
                if(!isCancelled()){
                    workingLabel.setText("");
                    inputField.setEnabled(true);
                    runScriptButton.setEnabled(true);
                    runScriptButton.setText("Run Script");
                }
                else {
                    model.addRow(new Object[]{input, algorithm, "Terminated", ""});
                    // Runtime.getRuntime().exec("taskkill /f /im python.exe");
                    process.destroy();
                    System.out.println("test");
                    workingLabel.setText("");
                    inputField.setEnabled(true);
                    runScriptButton.setText("Run Script");
                }
            }
        }
    }