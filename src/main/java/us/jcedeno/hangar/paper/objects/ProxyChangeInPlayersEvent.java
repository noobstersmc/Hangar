package us.jcedeno.hangar.paper.objects;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProxyChangeInPlayersEvent extends Event {    /*
    * Methods Required by BukkitAPI
    */
   private @Getter static final HandlerList HandlerList = new HandlerList();
   private @Getter final HandlerList Handlers = HandlerList;
   /*
    * Custom data, use @NonNull for the constructor
    */
   private @NonNull @Getter Integer players;
    
}
