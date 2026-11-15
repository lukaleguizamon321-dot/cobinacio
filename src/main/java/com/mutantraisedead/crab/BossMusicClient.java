package com.mutantraisedead.crab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * Musica del jefe de cobre (solo cliente). Empieza cuando el jefe aparece y suena mientras este vivo.
 * Se escucha hasta 15 bloques a la redonda: a menos de 6 bloques suena a todo volumen y de ahi baja de a poco hasta
 * apagarse a los 15. (No usamos la distancia de Minecraft porque la cancion es estereo y asi no se apagaba.)
 * Cuando la cancion termina, empieza de nuevo mientras el jefe siga vivo.
 */
public final class BossMusicClient {
    /** A cuantos bloques deja de oirse. */
    public static final double RANGE = 15.0D;
    /** Hasta cuantos bloques suena a todo volumen. */
    public static final double FULL_VOLUME = 6.0D;

    private static BossMusic current;

    private BossMusicClient() {
    }

    /** Lo llama el jefe en cada tick del cliente. */
    public static void tick(CopperCrabBossEntity boss) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || boss.isRemoved() || !boss.isAlive()) {
            return;
        }
        if (current != null && !current.isStopped() && current.boss != boss && current.boss.isAlive() && !current.boss.isRemoved()) {
            return; // la musica de otro jefe ya esta sonando
        }
        if (current == null || current.isStopped() || current.boss != boss) {
            start(mc, boss);
        } else if (current.age > 60 && !mc.getSoundManager().isActive(current)) {
            // termino la cancion: vuelve a empezar
            current.halt();
            start(mc, boss);
        }
    }

    private static void start(Minecraft mc, CopperCrabBossEntity boss) {
        current = new BossMusic(boss);
        mc.getSoundManager().play(current);
    }

    private static final class BossMusic extends AbstractTickableSoundInstance {
        private final CopperCrabBossEntity boss;
        private int age;

        BossMusic(CopperCrabBossEntity boss) {
            super(ModSounds.COPPER_BOSS_MUSIC.get(), SoundSource.MUSIC, SoundInstance.createUnseededRandom());
            this.boss = boss;
            this.looping = false;
            this.delay = 0;
            this.relative = true;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.volume = 0.02F;
        }

        void halt() {
            stop();
        }

        @Override
        public void tick() {
            age++;
            Player player = Minecraft.getInstance().player;
            if (player == null || boss.isRemoved() || !boss.isAlive()) {
                stop();
                return;
            }
            double distance = player.distanceTo(boss);
            float wanted;
            if (distance <= FULL_VOLUME) {
                wanted = 1.0F;
            } else if (distance >= RANGE) {
                wanted = 0.0F;
            } else {
                wanted = (float) (1.0D - (distance - FULL_VOLUME) / (RANGE - FULL_VOLUME));
            }
            // sube y baja suave, sin saltos
            this.volume += (wanted - this.volume) * 0.2F;
            if (this.volume < 0.001F) {
                this.volume = 0.001F;
            }
            if (this.volume > 0.02F) {
                // que no se pise con la musica de fondo del juego
                Minecraft.getInstance().getMusicManager().stopPlaying();
            }
        }
    }
}
