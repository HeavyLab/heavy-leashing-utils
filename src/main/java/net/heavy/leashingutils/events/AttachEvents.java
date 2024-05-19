package net.heavy.leashingutils.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class AttachEvents {
    public static final Event<Runnable> ATTACH_LEASH =
            EventFactory.createArrayBacked(Runnable.class, callbacks -> () -> {
                for (Runnable callback : callbacks) {
                    callback.run();
                }
            });

    public static final Event<Runnable> DETACH_LEASH =
            EventFactory.createArrayBacked(Runnable.class, callbacks -> () -> {
                for (Runnable callback : callbacks) {
                    callback.run();
                }
            });
}
