package Love;
import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class Main {
    private Attitude attitude;
    public static void main(String[] args) {
        Main main = new Main();
        main.createDialog();
    }
    public void factoryAttitude(Attitude.Attitudes at) {
        switch (at) {
            case Attitude.Attitudes.Love:
                attitude = new Love();
                break;
            case Attitude.Attitudes.Hate:
                attitude = new Hate();
                break;
            default:
                attitude = new NotThatIntoYou();
        }
        if (attitude instanceof Degree) { // статическая проверка с иерархией
            createDegreeDialog();
        }
        else {
            printMessage();
        }
    }

    public void createDialog(){
        JDialog dialog = new JDialog();
        dialog.setVisible(true);
        dialog.setSize(500, 500);

        JPanel panel = new JPanel();

        JButton buttonLove = new JButton("Люблю");
        buttonLove.addActionListener(e -> {
            this.factoryAttitude(Attitude.Attitudes.Love);
            dialog.dispose();
        });
        panel.add(buttonLove);

        JButton buttonHate = new JButton("Ненавижу");
        buttonHate.addActionListener(e -> {
            this.factoryAttitude(Attitude.Attitudes.Hate);
            dialog.dispose();
        });
        panel.add(buttonHate);

        JButton buttonNotThat = new JButton("Нинаю(");
        buttonNotThat.addActionListener(e -> {
            this.factoryAttitude(Attitude.Attitudes.NotThatIntoYou);
            dialog.dispose();
        });
        panel.add(buttonNotThat);
        dialog.add(panel);

        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    void createDegreeDialog() {
        NumberFormat format = NumberFormat.getIntegerInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(0);
        formatter.setMaximum(10);
        formatter.setAllowsInvalid(false);

        JFormattedTextField jFormattedTextField = new JFormattedTextField(formatter);
        jFormattedTextField.setVisible(true);
        jFormattedTextField.setSize(500, 100);
        jFormattedTextField.setBorder(BorderFactory.createTitledBorder("Насколько сильно? (от 0 до 10)"));
        jFormattedTextField.setValue(0);

        JFrame frame = new JFrame("Степень чувств");
        frame.setSize(500, 500);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(jFormattedTextField);

        JButton buttonConfirm = new JButton("Подтвердить");
        buttonConfirm.setSize(500, 100);
        buttonConfirm.addActionListener(e -> {
            ((Degree) attitude).setDegree((int)jFormattedTextField.getValue());
            frame.dispose();
            printMessage();
        });
        panel.add(buttonConfirm);
        panel.setSize(500, 100);
        frame.add(panel);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        frame.show();
    }
    void printMessage(){
        JDialog dialog = new JDialog();
        dialog.setVisible(true);
        dialog.setSize(500, 500);

        JLabel message = new JLabel(String.valueOf(attitude));
        dialog.add(message);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.show();
    }
}


