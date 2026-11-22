package keystrokesmod.client.command.commands;

import keystrokesmod.client.clickgui.raven.Terminal;
import keystrokesmod.client.command.Command;
import keystrokesmod.client.notifications.Notification;
import keystrokesmod.client.notifications.NotificationManager;
import keystrokesmod.client.notifications.NotificationType;

/**
 * {@code notify} shows a demo of the notifications. {@code notify <info|success|warning|error> text}
 * shows one banner. They only appear while "Notifications" is switched on in the Gui module.
 */
public class Notify extends Command {
    public Notify() {
        super("notify", "Shows test notifications. 'notify [info|success|warning|error] <text>'", 0, 40,
                new String[] { "type", "text" }, new String[] {});
    }

    @Override
    public void onCall(String[] args) {
        Terminal.print("Notifications only show while 'Notifications' is on in the Gui module settings.");

        if (args.length == 0) {
            NotificationManager.island("Sprint", true);
            NotificationManager.info("Info", "This is a normal message");
            NotificationManager.success("Success", "Config saved");
            NotificationManager.warning("Warning", "Something needs attention");
            NotificationManager.error("Error", "Something went wrong");
            return;
        }

        NotificationType type = NotificationType.INFO;
        int first = 0;
        for (NotificationType t : NotificationType.values()) {
            if (t.name().equalsIgnoreCase(args[0])) {
                type = t;
                first = 1;
            }
        }
        StringBuilder text = new StringBuilder();
        for (int i = first; i < args.length; i++) {
            if (i > first) text.append(' ');
            text.append(args[i]);
        }
        if (text.length() == 0) {
            this.incorrectArgs();
            return;
        }
        String title = type.name().charAt(0) + type.name().substring(1).toLowerCase();
        NotificationManager.show(new Notification(type, title, text.toString(), 1));
    }
}
