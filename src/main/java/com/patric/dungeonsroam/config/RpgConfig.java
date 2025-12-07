package com.patric.dungeonsroam.config;

import java.util.Collections;
import java.util.List;

public class RpgConfig {
    private List<String> rpgWorlds = Collections.emptyList();
    private boolean useWorldGuard;
    private boolean regionInventorySwitch;
    private boolean allowPvp;
    private List<String> safeRegions = Collections.emptyList();
    private InventoryConfig inventory = new InventoryConfig();
    private EconomyConfig economy = new EconomyConfig();
    private KitBuilderConfig kitBuilder = new KitBuilderConfig();
    private ShopConfig shop = new ShopConfig();
    private VanillaOverrides vanillaOverrides = new VanillaOverrides();
    private LoggingConfig logging = new LoggingConfig();

    public List<String> getRpgWorlds() {
        return rpgWorlds;
    }

    public void setRpgWorlds(List<String> rpgWorlds) {
        this.rpgWorlds = rpgWorlds;
    }

    public boolean isUseWorldGuard() {
        return useWorldGuard;
    }

    public void setUseWorldGuard(boolean useWorldGuard) {
        this.useWorldGuard = useWorldGuard;
    }

    public boolean isRegionInventorySwitch() {
        return regionInventorySwitch;
    }

    public void setRegionInventorySwitch(boolean regionInventorySwitch) {
        this.regionInventorySwitch = regionInventorySwitch;
    }

    public boolean isAllowPvp() {
        return allowPvp;
    }

    public void setAllowPvp(boolean allowPvp) {
        this.allowPvp = allowPvp;
    }

    public List<String> getSafeRegions() {
        return safeRegions;
    }

    public void setSafeRegions(List<String> safeRegions) {
        this.safeRegions = safeRegions;
    }

    public InventoryConfig getInventory() {
        return inventory;
    }

    public EconomyConfig getEconomy() {
        return economy;
    }

    public KitBuilderConfig getKitBuilder() {
        return kitBuilder;
    }

    public ShopConfig getShop() {
        return shop;
    }

    public VanillaOverrides getVanillaOverrides() {
        return vanillaOverrides;
    }

    public LoggingConfig getLogging() {
        return logging;
    }

    public static class InventoryConfig {
        private boolean preferSharedEnderChest;

        public boolean isPreferSharedEnderChest() {
            return preferSharedEnderChest;
        }

        public void setPreferSharedEnderChest(boolean preferSharedEnderChest) {
            this.preferSharedEnderChest = preferSharedEnderChest;
        }
    }

    public static class EconomyConfig {
        private boolean useVault;
        private boolean useXConomy;
        private double startingBalance;
        private String currencyName;

        public boolean isUseVault() {
            return useVault;
        }

        public void setUseVault(boolean useVault) {
            this.useVault = useVault;
        }

        public boolean isUseXConomy() {
            return useXConomy;
        }

        public void setUseXConomy(boolean useXConomy) {
            this.useXConomy = useXConomy;
        }

        public double getStartingBalance() {
            return startingBalance;
        }

        public void setStartingBalance(double startingBalance) {
            this.startingBalance = startingBalance;
        }

        public String getCurrencyName() {
            return currencyName;
        }

        public void setCurrencyName(String currencyName) {
            this.currencyName = currencyName;
        }
    }

    public static class KitBuilderConfig {
        private boolean enabled;
        private boolean forceSafeZones;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isForceSafeZones() {
            return forceSafeZones;
        }

        public void setForceSafeZones(boolean forceSafeZones) {
            this.forceSafeZones = forceSafeZones;
        }
    }

    public static class ShopConfig {
        private String title;
        private int rows;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }
    }

    public static class VanillaOverrides {
        private boolean disableVanillaMobSpawns;
        private boolean enforceNoPvp;

        public boolean isDisableVanillaMobSpawns() {
            return disableVanillaMobSpawns;
        }

        public void setDisableVanillaMobSpawns(boolean disableVanillaMobSpawns) {
            this.disableVanillaMobSpawns = disableVanillaMobSpawns;
        }

        public boolean isEnforceNoPvp() {
            return enforceNoPvp;
        }

        public void setEnforceNoPvp(boolean enforceNoPvp) {
            this.enforceNoPvp = enforceNoPvp;
        }
    }

    public static class LoggingConfig {
        private boolean debug;

        public boolean isDebug() {
            return debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }
    }
}
