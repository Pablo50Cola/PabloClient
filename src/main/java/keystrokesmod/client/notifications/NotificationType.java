package keystrokesmod.client.notifications;

import keystrokesmod.client.ui.gfx.Colors;

public enum NotificationType {
    INFO(Colors.ACCENT), SUCCESS(Colors.GREEN), WARNING(Colors.ORANGE), ERROR(Colors.RED);

    /** Colour of the dot on the banner. */
    public final int color;

    NotificationType(int color) {
        this.color = color;
    }
}
