package machalica.marcin.timetracker.helper;

import javafx.scene.control.Dialog;
import machalica.marcin.timetracker.main.Main;

public class DialogHelper {
    public static void centerDialog(Dialog dialog, double dialogWidth, double dialogHeight) {
        double stageX = Main.getPosX();
        double stageY = Main.getPosY();
        double stageWidth = Main.getWidth();
        double stageHeight = Main.getHeight();

        double stageCenterX = stageX + stageWidth / 2.0;
        double stageCenterY = stageY + stageHeight / 2.0;

        double dialogCenterX = stageCenterX - dialogWidth / 2.0;
        double dialogCenterY = stageCenterY - dialogHeight / 2.0;

        dialog.setX(dialogCenterX);
        dialog.setY(dialogCenterY);
    }
}