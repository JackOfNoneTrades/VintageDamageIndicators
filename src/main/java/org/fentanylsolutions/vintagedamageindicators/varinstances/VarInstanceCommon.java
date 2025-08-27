package org.fentanylsolutions.vintagedamageindicators.varinstances;

import org.fentanylsolutions.vintagedamageindicators.Config;
import org.fentanylsolutions.vintagedamageindicators.VintageDamageIndicators;
import org.fentanylsolutions.vintagedamageindicators.util.MobUtil;
import org.fentanylsolutions.vintagedamageindicators.util.XSTR;

import java.util.ArrayList;

public class VarInstanceCommon {
    public XSTR rand = new XSTR();

    public ArrayList<Class> playerTypeOverrides;
    public  ArrayList<Class> bossTypeOverrides;
    public  ArrayList<Class> waterAnimalTypeOverrides;
    public  ArrayList<Class> waterMonsterTypeOverrides;
    public  ArrayList<Class> monsterTypeOverrides;
    public  ArrayList<Class> undeadTypeOverrides;
    public  ArrayList<Class> undeadAnimalTypeOverrides;
    public  ArrayList<Class> arthropodTypeOverrides;
    public  ArrayList<Class> illagerTypeOverrides;
    public  ArrayList<Class> villagerTypeOverrides;
    public  ArrayList<Class> golemTypeOverrides;
    public  ArrayList<Class> ambientTypeOverrides;
    public  ArrayList<Class> animalTypeOverrides;
    public  ArrayList<Class> arthropodMonsterTypeOverrides;
    public  ArrayList<Class> arthropodWaterMonsterTypeOverrides;
    public  ArrayList<Class> arthropodWaterTypeOverrides;

    public  ArrayList<Class> entityBlacklist;

    public void postInitHook() {
        buildMobLists();
    }

    public void buildMobLists() {
        for (String s : Config.playerTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    playerTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
        for (String s : Config.bossTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    bossTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
        for (String s : Config.waterAnimalTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    waterAnimalTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }

        for (String s : Config.waterMonsterTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    waterMonsterTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
        for (String s : Config.monsterTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    monsterTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
        for (String s : Config.undeadTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    undeadTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
        for (String s : Config.undeadAnimalTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    undeadAnimalTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
        for (String s : Config.arthropodTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    arthropodTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
        for (String s : Config.illagerTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    illagerTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
        for (String s : Config.villagerTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    villagerTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
        for (String s : Config.golemTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    golemTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
        for (String s : Config.ambientTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    ambientTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
        for (String s : Config.animalTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    animalTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
        for (String s : Config.arthropodMonsterTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    arthropodMonsterTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
        for (String s : Config.arthropodWaterMonsterTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    arthropodWaterMonsterTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
        for (String s : Config.arthropodWaterTypeOverrides) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    arthropodWaterTypeOverrides.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }

        for (String s : Config.entityBlacklist) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                VintageDamageIndicators.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    entityBlacklist.add(c);
                } catch (ClassNotFoundException e) {
                    VintageDamageIndicators.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
    }
}
