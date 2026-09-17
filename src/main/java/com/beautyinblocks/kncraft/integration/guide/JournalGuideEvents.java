package com.beautyinblocks.kncraft.integration.guide;

import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;

/** Public FTB/Architectury event bridge, loaded only when those optional mods are available. */
public final class JournalGuideEvents {
    public static AutoCloseable register(Consumer<ResourceLocation> openEntry) throws ReflectiveOperationException {
        var click = Class.forName("dev.ftb.mods.ftblibrary.ui.CustomClickEvent");
        var actor = Class.forName("dev.architectury.event.EventActor");
        var result = Class.forName("dev.architectury.event.EventResult");
        var eventType = Class.forName("dev.architectury.event.Event");
        var id = click.getMethod("id"); var pass = result.getMethod("pass"); var handled = result.getMethod("interruptTrue");
        Object listener = Proxy.newProxyInstance(actor.getClassLoader(), new Class<?>[] {actor}, (proxy, method, args) -> {
            if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
            if (method.getName().equals("equals")) return proxy == args[0];
            if (method.getName().equals("toString")) return "KNCraft journal guide links";
            var entry = JournalGuideRoute.entry((ResourceLocation) id.invoke(args[0]));
            if (entry == null) return pass.invoke(null);
            openEntry.accept(entry);
            return handled.invoke(null);
        });
        Object event = click.getField("EVENT").get(null);
        eventType.getMethod("register", Object.class).invoke(event, listener);
        return () -> eventType.getMethod("unregister", Object.class).invoke(event, listener);
    }
    private JournalGuideEvents() {}
}
