package com.mutantraisedead.goggles;

import com.mutantraisedead.Achievements;
import com.mutantraisedead.Config;
import com.mutantraisedead.GuardHandler;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * El laser de las Gafas de amatista (lado del servidor).
 *  - La tecla prende el laser (otra vez la tecla lo apaga antes de tiempo).
 *  - Dura gogglesSeconds. Cada medio segundo lastima a todo lo que toca (dano = gogglesDamage) y se corta en el primer bloque.
 *  - Al terminar, la vision queda reducida (Ceguera) gogglesBlindSeconds y las gafas tardan gogglesCooldownSeconds en recargarse.
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class GogglesLaser {
    /** Cada cuantos ticks lastima a lo que esta tocando el rayo (10 = dos veces por segundo). */
    private static final int HIT_INTERVAL = 10;

    private static final Map<UUID, Beam> ACTIVE = new HashMap<>();
    /** Cuando (hora del juego) se recargan las gafas de cada jugador. */
    private static final Map<UUID, Long> READY_AT = new HashMap<>();

    private static final class Beam {
        int ticksLeft;
        int age;
        final int rgb;
        final boolean red;
        final Map<Integer, Integer> lastHit = new HashMap<>();

        Beam(int ticks, int rgb, boolean red) {
            this.ticksLeft = ticks;
            this.rgb = rgb;
            this.red = red;
        }
    }

    private GogglesLaser() {
    }

    /** Las gafas que lleva puestas el jugador (en cualquier ranura de Curios), o vacio. */
    public static ItemStack findGoggles(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(handler -> handler.findFirstCurio(ModGoggles.GOGGLES.get()))
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }

    /** El jugador apreto la tecla. */
    public static void toggle(ServerPlayer player) {
        ItemStack goggles = findGoggles(player);
        if (goggles.isEmpty() || !player.isAlive() || player.isSpectator()) {
            return;
        }
        if (ACTIVE.containsKey(player.getUUID())) {
            finish(player, true, true);
            return;
        }
        long now = player.level().getGameTime();
        Long ready = READY_AT.get(player.getUUID());
        if (ready != null && now < ready) {
            int seconds = (int) Math.ceil((ready - now) / 20.0D);
            player.displayClientMessage(Component.translatable("message.mutantraisedead.goggles_cooldown", seconds)
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        int ticks = Config.GOGGLES_SECONDS.get() * 20;
        int rgb = GogglesColors.rgbOf(goggles);
        boolean red = GogglesColors.dyeOf(goggles) == DyeColor.RED;
        ACTIVE.put(player.getUUID(), new Beam(ticks, rgb, red));
        GogglesNetwork.sendBeam(player, rgb, ticks);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.7F);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Beam beam = ACTIVE.get(player.getUUID());
        if (beam == null) {
            return;
        }
        if (!player.isAlive() || player.isSpectator() || findGoggles(player).isEmpty()) {
            finish(player, false, true);
            return;
        }
        ServerLevel level = player.serverLevel();
        fire(level, player, beam);
        beam.age++;
        beam.ticksLeft--;
        if (beam.ticksLeft <= 0) {
            finish(player, true, true);
            return;
        }
        if (beam.age % 20 == 0) {
            player.displayClientMessage(Component.translatable("message.mutantraisedead.goggles_left",
                    (beam.ticksLeft + 19) / 20).withStyle(ChatFormatting.LIGHT_PURPLE), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.5F, 1.6F);
            // se repite cada segundo para que quien llegue a mirar mas tarde tambien lo vea
            GogglesNetwork.sendBeam(player, beam.rgb, beam.ticksLeft);
        }
    }

    /** Un tick del rayo: lastima a todo lo que toca entre la salida del rayo y el primer bloque. */
    private static void fire(ServerLevel level, ServerPlayer owner, Beam beam) {
        Vec3 eye = owner.getEyePosition();
        Vec3 look = owner.getLookAngle();
        Vec3 origin = GogglesGeometry.origin(eye, look);
        Vec3 target = GogglesGeometry.target(level, owner, eye, look);

        float damage = Config.GOGGLES_DAMAGE.get().floatValue();
        AABB bounds = new AABB(origin, target).inflate(1.5D);
        for (Entity entity : level.getEntities(owner, bounds, e -> canHit(owner, e))) {
            LivingEntity victim = (LivingEntity) entity;
            AABB box = victim.getBoundingBox().inflate(0.35D);
            if (!box.contains(origin) && box.clip(origin, target).isEmpty()) {
                continue;
            }
            Integer last = beam.lastHit.get(victim.getId());
            if (last != null && beam.age - last < HIT_INTERVAL) {
                continue;
            }
            beam.lastHit.put(victim.getId(), beam.age);
            boolean mob = victim instanceof Mob;
            victim.hurt(level.damageSources().indirectMagic(owner, owner), damage);
            level.sendParticles(dust(beam.rgb, 1.4F), victim.getX(), victim.getY() + victim.getBbHeight() * 0.5D, victim.getZ(),
                    10, 0.35D, 0.5D, 0.35D, 0.0D);
            if (beam.red && mob && victim.isDeadOrDying()) {
                Achievements.grant(owner, "me_proyecto");
            }
        }
        if (beam.age % 2 == 0) {
            level.sendParticles(dust(beam.rgb, 1.2F), target.x, target.y, target.z, 3, 0.2D, 0.2D, 0.2D, 0.0D);
            level.sendParticles(ParticleTypes.END_ROD, target.x, target.y, target.z, 1, 0.15D, 0.15D, 0.15D, 0.02D);
        }
    }

    /** A quien NO lastima: a vos, a tus mascotas y aliados, y a los soportes de armadura. */
    private static boolean canHit(ServerPlayer owner, Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity == owner || !living.isAlive() || living.isSpectator()) {
            return false;
        }
        if (entity instanceof ArmorStand || GuardHandler.isAllied(entity)) {
            return false;
        }
        if (GuardHandler.isPet(entity) && owner.getUUID().equals(GuardHandler.ownerIdOf(entity))) {
            return false;
        }
        return !(entity instanceof TamableAnimal pet && pet.isOwnedBy(owner));
    }

    private static DustParticleOptions dust(int rgb, float scale) {
        return new DustParticleOptions(new Vector3f(((rgb >> 16) & 255) / 255.0F, ((rgb >> 8) & 255) / 255.0F, (rgb & 255) / 255.0F), scale);
    }

    /**
     * Apaga el laser y arranca la recarga. Si punish es true, ademas deja la vision reducida (Ceguera).
     * Con notify en false no se manda nada por red (cuando el jugador se esta yendo).
     */
    private static void finish(ServerPlayer player, boolean punish, boolean notify) {
        Beam beam = ACTIVE.remove(player.getUUID());
        if (beam == null) {
            return;
        }
        int cooldown = Config.GOGGLES_COOLDOWN_SECONDS.get() * 20;
        READY_AT.put(player.getUUID(), player.level().getGameTime() + cooldown);
        if (!notify) {
            return;
        }
        if (cooldown > 0) {
            player.getCooldowns().addCooldown(ModGoggles.GOGGLES.get(), cooldown);
        }
        GogglesNetwork.sendBeam(player, beam.rgb, 0);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.6F);
        int blind = Config.GOGGLES_BLIND_SECONDS.get();
        if (punish && blind > 0 && player.isAlive()) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, blind * 20, 0, false, false, true));
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            finish(player, false, true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            finish(player, false, false);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE.clear();
        READY_AT.clear();
    }
}
