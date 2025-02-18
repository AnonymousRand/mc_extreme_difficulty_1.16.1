package AnonymousRand.anonymousrand.plugin.nms.customentities.misc;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.CustomEntityPhantom;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.CustomEntityVex;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.CustomEntityWitherMini;
import AnonymousRand.anonymousrand.plugin.listeners.ListenerMobSpawnAndReplaceWithCustom;
import AnonymousRand.anonymousrand.plugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;

public class CustomEntityEnderCrystal extends EntityEnderCrystal {

    public CustomEntityEnderCrystal(World world) {
        super(EntityTypes.END_CRYSTAL, world);
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        if (!(damageSource instanceof EntityDamageSourceIndirect) && damageSource instanceof EntityDamageSource) { /* end crystals can only be blown up by direct damage and not arrows etc.; every time an arrow is shot at the end crystal, it duplicates */
            if (damageSource.getEntity() instanceof EntityPlayer) {
                this.world.createExplosion(null, this.locX(), this.locY(), this.locZ(), 8.0F, true, Explosion.Effect.DESTROY); /* end crystals explode power 8 */
                return super.damageEntity(damageSource, damageAmount);
            }
        } else if (damageSource instanceof EntityDamageSourceIndirect) {
            CustomEntityEnderCrystal newCrystal = new CustomEntityEnderCrystal(this.world);
            newCrystal.setPosition(this.locX(), this.locY() + 3.0, this.locZ());
            this.world.addEntity(newCrystal);
        }

        return false;
    }

    @Override
    public void die() {
        super.die();
        this.onCrystalDestroyed(DamageSource.GENERIC); // make sure to update dragon fight

        /* end crystals summon a mini wither, 3 phantoms and 3 vexes when destroyed */
        new SpawnEntity(this.world, new CustomEntityWitherMini(this.world), 1, null, null, this, false, false);
        new SpawnEntity(this.world, (int) ListenerMobSpawnAndReplaceWithCustom.phantomSize, new CustomEntityPhantom(this.world, (int) ListenerMobSpawnAndReplaceWithCustom.phantomSize), 3, null, null, this, false, false);
        new SpawnEntity(this.world, new CustomEntityVex(this.world), 3, null, null, this, false, false);
    }

    private void onCrystalDestroyed(DamageSource damageSource) {
        if (this.world instanceof WorldServer) {
            EnderDragonBattle enderdragonbattle = ((WorldServer) this.world).getDragonBattle();

            if (enderdragonbattle != null) {
                enderdragonbattle.a(this, damageSource);
            }
        }
    }
}
