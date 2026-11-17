package railwayreservation;

import javax.swing.*;

public class Main {

    public static void main(
            String[] args) {

        Theme.setup();

        SwingUtilities.invokeLater(
                () -> {

                    new LoginFrame()
                            .setVisible(true);

                }
        );
    }
}