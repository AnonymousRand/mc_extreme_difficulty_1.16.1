package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityPhantom;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntitySilverfish;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityVex;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityWitherMini;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.ListenerMobSpawnAndReplaceWithCustom;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;

public class CustomEntityEnderCrystal extends EntityEnderCrystal {

    public CustomEntityEnderCrystal(World world) {
        super(EntityTypes.END_CRYSTAL, world);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (!(damagesource instanceof EntityDamageSourceIndirect) && damagesource instanceof EntityDamageSource) { /**end crystals can only be blown up by direct damage and not arrows etc.; every time an arrow is shot at the end crystal, it duplicates*/
            if (damagesource.getEntity() instanceof EntityPlayer) {
                this.world.createExplosion(null, this.locX(), this.locY(), this.locZ(), 8.0F, true, Explosion.Effect.DESTROY); /**end crystals explode power 8*/
                return super.damageEntity(damagesource, f);
            }
        } else if (damagesource instanceof EntityDamageSourceIndirect) {
            CustomEntityEnderCrystal newCrystal = new CustomEntityEnderCrystal(this.getWorld());
            newCrystal.setPosition(this.locX(), this.locY() + 3.0, this.locZ());
            this.getWorld().addEntity(newCrystal);
        }

        return false;
    }

    @Override
    public void die() {
        super.die();
        this.onCrystalDestroyed(DamageSource.GENERIC); //make sure to update dragon fight

        /**end crystals summon a mini wither, 4 phantoms and 3 vexes when destroyed*/
        new SpawnEntity(this.getWorld(), new CustomEntityWitherMini(this.getWorld()), 1, null, null, this, false, false);
        new SpawnEntity(this.getWorld(), (int)ListenerMobSpawnAndReplaceWithCustom.phantomSize, new CustomEntityPhantom(this.getWorld(), (int)ListenerMobSpawnAndReplaceWithCustom.phantomSize), 4, null, null, this, false, false);
        new SpawnEntity(this.getWorld(), new CustomEntityVex(this.getWorld()), 3, null, null, this, false, false);
    }

    private void onCrystalDestroyed(DamageSource damagesource) {
        if (this.world instanceof WorldServer) {
            EnderDragonBattle enderdragonbattle = ((WorldServer) this.world).getDragonBattle();

            if (enderdragonbattle != null) {
                enderdragonbattle.a(this, damagesource);
            }
        }
    }
}
