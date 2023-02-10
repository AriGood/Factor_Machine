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
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main
{
    public static void main(String[] args)
    {
        // set up frame
        MainFrame frame = new MainFrame();
        frame.setTitle("The Factor Machine");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //end python on window close
        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                try {
                    Runtime.getRuntime().exec("taskkill /f /im python.exe");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}

class MainFrame extends JFrame
{
    private final JTextField inputField;
    private final JComboBox<String> algorithmSelector;
    private final JButton runScriptButton;
    private ScriptWorker scriptWorker;


    public MainFrame()
    {

        //make my program nice colors
        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException |
                 IllegalAccessException ex) {
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
        outputTable.setEnabled(false);

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

        // Create a panel for the input field and algorithm selection dropdown and number entering
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));

        // add text field for input
        inputPanel.add(new JLabel("Enter a number: "));
        inputField = new JTextField();
        inputField.setPreferredSize(new Dimension(200, 30));
        inputPanel.add(inputField);
        ((AbstractDocument) inputField.getDocument()).setDocumentFilter(new NumericFilter(64));

        // add algorithm selection dropdown
        algorithmSelector = new JComboBox<>(new String[]{"ECM", "PollardRho", "SimpleFactor", "Shor"});
        algorithmSelector.setPreferredSize(new Dimension(150, 30));
        inputPanel.add(algorithmSelector);

        // add button to run the script
        runScriptButton = new JButton("Run Script");
        runScriptButton.setPreferredSize(new Dimension(100, 30));

        runScriptButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (runScriptButton.getText().equals("Run Script")) {
                    // Create a new ScriptWorker task and execute it
                    String input = inputField.getText();
                    BigInteger n = new BigInteger(input);
                    String algorithm = (String) algorithmSelector.getSelectedItem();
                    if (prime.isPrime(n, 5)) {
                        model.addRow(new Object[]{input, algorithm, "Prime", ""});
                        runScriptButton.setText("Run Script");
                    } else {
                        model.addRow(new Object[]{input, algorithm, "Working", ""});
                        scriptWorker = new ScriptWorker(input, algorithm, model, inputField, runScriptButton);
                        scriptWorker.execute();
                        // Change the text of the button to "Cancel"
                        runScriptButton.setText("Cancel");
                    }
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
        ((AbstractDocument) semiprimeField.getDocument()).setDocumentFilter(new NumericFilter(64));

        // add button to run the script
        JButton runSemiprimeButton = new JButton("Generate Semiprime");

        // make semi-prime when button is clicked
        runSemiprimeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
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

        JButton Clear = new JButton("Clear");

        // make a button clear things
        Clear.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                model.setRowCount(0);
            }
        });
        semiprimePanel.add(Clear);
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

    public static class NumericFilter extends DocumentFilter
    {
        private final int characterLimit;

        public NumericFilter(int characterLimit)
        {
            this.characterLimit = characterLimit;
        }

        // replace characters when the text box gets to long
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
        {
            if (text == null) {
                return;
            }
            if (fb.getDocument().getLength() + text.length() - length <= characterLimit) {
                super.replace(fb, offset, length, text.replaceAll("\\D", ""), attrs);
            } else {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showInternalMessageDialog(null, "Int Overflow Max Length 64 Digits");
            }
        }
    }


    public static class prime
    {
        public static boolean isPrime(BigInteger n, int k)
        {
            // check if n is even
            if (n.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
                return false;
            }

            Random rand = new Random();
            for (int i = 0; i < k; i++) {
                // choose a random integer a in the range [2, n-2]
                BigInteger a = new BigInteger(n.bitLength() - 1, rand);
                a = a.add(BigInteger.ONE);

                // if a^(n-1) % n != 1, n is not prime
                if (!a.modPow(n.subtract(BigInteger.ONE), n).equals(BigInteger.ONE)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class CustomTableCellRenderer extends DefaultTableCellRenderer
    {

        // edit the getTableCellRendererComponent so I can make it do what I want
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            // call the super class to get the default renderer
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // if the value in the column is "Terminated" and the column is the 3rd column
            if (column == 2 && value.equals("Terminated")) {
                // set the background color to red
                Color myRed = new Color(241, 44, 44, 255);
                c.setBackground(myRed);
            } else if (column == 2 && value.equals("Prime")) {
                // set the background color to orange
                Color myOrange = new Color(247, 96, 15);
                c.setBackground(myOrange);
            } else if (column == 2 && value.equals("Working")) {
                // set the background color to yellow
                Color myOrange = new Color(204, 183, 2);
                c.setBackground(myOrange);
            } else {
                // set the background color to green
                Color myGreen = new Color(6, 215, 47, 190);
                c.setBackground(myGreen);
            }

            // return the component with the desired background color
            return c;
        }
    }


    public static class SemiprimeWorker extends SwingWorker<String, Void>
    {
        private final String input;
        private final JTextField inputField;

        public SemiprimeWorker(String input, JTextField inputField)
        {
            this.input = input;
            this.inputField = inputField;

        }

        @Override
        protected String doInBackground() throws Exception
        {

            //check if the input is greater than 212
            if (Integer.parseInt(input) > 212) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showInternalMessageDialog(null, "range error can't generate a semi-prime with more than 212 bits as that would exceed the 64 digit limit ");
                return ("");
            } else {

                //generate the command to run the python script and run the script
                String command = "python3 \"RandomSemiprime.py\" " + input;
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
                builder.redirectErrorStream(true);
                Process p = builder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    result.append(line).append("\n");
                }
                return result.toString();
            }
        }

        @Override
        protected void done()
        {
            try {
                String result = get();
                // update the inputField text with the output of the script
                inputField.setText(result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    static class ScriptWorker extends SwingWorker<String, Void>
    {
        private final String input;
        private final String algorithm;
        private final DefaultTableModel model;
        private final JTextField inputField;
        private final JButton runScriptButton;
        private Process process;

        public ScriptWorker(String input, String algorithm, DefaultTableModel model, JTextField inputField, JButton runScriptButton)
        {
            // Save the input, selected algorithm, table model, input field, and run button as instance variables
            this.input = input;
            this.algorithm = algorithm;
            this.model = model;
            this.inputField = inputField;
            this.runScriptButton = runScriptButton;
        }

        // Add a method to set the process variable
        public void setProcess(Process process)
        {
            this.process = process;
        }

        @Override
        protected String doInBackground() throws IOException
        {
            String command = "";
            String output = "";
            String time = "";

            // Add variables for the path of the python executable and the base path of the script files
            String pythonPath = "python3";

            // Use the switch statement to determine the command to run based on the selected algorithm
            switch (algorithm) {
                case "ECM" ->
                        command = pythonPath + " \"LesterThread.py\" " + input;
                case "PollardRho" ->
                        command = pythonPath + " \"PollardRho.py\" " + input;
                case "SimpleFactor" ->
                        command = pythonPath + " \"SimpleFactor.py\" " + input;
                case "Shor" ->
                        command = pythonPath + " \"Shor_Simulator.py\" " + input;
            }
            // Start the process using the command
            process = Runtime.getRuntime().exec(command);
            // Set the process variable using the setter method
            setProcess(process);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s;
            // Read the output from the script
            while ((s = stdInput.readLine()) != null) {
                // Check if the task has been cancelled
                if (isCancelled()) {
                    // If the task is cancelled, use the "taskkill" command to kill the python process
                    Runtime.getRuntime().exec("taskkill /f /im python.exe");
                    // Break out of the loop
                    break;
                }
                output += s + "\n";
                time = stdInput.readLine();
            }
            // If the task has not been cancelled
            if (!isCancelled()) {
                // Update the output and time columns in the table with the output and time from the script
                model.setValueAt(output, model.getRowCount() - 1, 2);
                model.setValueAt(time, model.getRowCount() - 1, 3);
            }

            return output;
        }

        @Override
        protected void done()
        {
            if (!isCancelled()) {
                // rest stuff if cancelled is not clicked
                inputField.setEnabled(true);
                runScriptButton.setEnabled(true);
                runScriptButton.setText("Run Script");
            } else {
                // rest stuff if cancelled is clicked
                model.setValueAt("Terminated", model.getRowCount() - 1, 2);
                inputField.setEnabled(true);
                runScriptButton.setText("Run Script");
            }
        }
    }
}
