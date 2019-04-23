package machalica.marcin.timetracker.helper;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import machalica.marcin.timetracker.main.Main;
import org.controlsfx.control.Notifications;

import java.net.URISyntaxException;

public class NotificationHelper {
    public static void showNotification(int duration, String title, String text, String imgPath) {
        Image img = null;
        try {
            img = new Image(Main.class.getResource(imgPath).toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Notifications notificationBuilder = Notifications.create()
                .title(title)
                .text(text)
                .graphic(new ImageView(img))
                .hideAfter(Duration.seconds(duration))
                .position(Pos.BOTTOM_RIGHT)
                .darkStyle();
        notificationBuilder.show();
    }
}
