package com.allat.mboychenko.silverthread.presentation.helpers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.allat.mboychenko.silverthread.BuildConfig

object OptimizationUtils {

    private val AUTO_START_INTENTS = arrayOf(
        //xiaomi
        Intent().setComponent(
            ComponentName("com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity")
        ),

        //huawei
        Intent().setComponent(ComponentName.unflattenFromString("com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity")),
        Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
        Intent().setComponent(ComponentName.unflattenFromString("com.huawei.systemmanager/.appcontrol.activity.StartupAppControlActivity")),

        //meizu
        Intent().setComponent(ComponentName("com.meizu.safe", "com.meizu.safe.security.SHOW_APPSEC"))
            .addCategory(Intent.CATEGORY_DEFAULT)
            .putExtra("packageName", BuildConfig.APPLICATION_ID),

        //samsung
        Intent().setComponent(
            ComponentName("com.samsung.android.sm_cn",
                "com.samsung.android.sm.autorun.ui.AutoRunActivity")
        ),
        Intent().setComponent(
            ComponentName("com.samsung.android.sm_cn",
                "com.samsung.android.sm.ui.ram.AutoRunActivity")
        ),
        Intent().setComponent(
            ComponentName("com.samsung.android.sm_cn",
                "com.samsung.android.sm.ui.appmanagement.AppManagementActivity")
        ),
        Intent().setComponent(
            ComponentName("com.samsung.android.sm",
                "com.samsung.android.sm.autorun.ui.AutoRunActivity")
        ),
        Intent().setComponent(
            ComponentName("com.samsung.android.sm",
                "com.samsung.android.sm.ui.ram.AutoRunActivity")
        ),
        Intent().setComponent(
            ComponentName("com.samsung.android.sm",
                "com.samsung.android.sm.ui.appmanagement.AppManagementActivity")
        ),
        Intent().setComponent(
            ComponentName("com.samsung.android.sm_cn",
                "com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity")
        ),
        Intent().setComponent(
            ComponentName("com.samsung.android.sm",
                "com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity")
        ),
        Intent().setComponent(
            ComponentName.unflattenFromString(
                "com.samsung.android.sm_cn/.app.dashboard.SmartManagerDashBoardActivity")),
        Intent().setComponent(
            ComponentName.unflattenFromString(
                "com.samsung.android.sm/.app.dashboard.SmartManagerDashBoardActivity")),

        Intent().setComponent(
            ComponentName
                .unflattenFromString("com.coloros.safecenter/.startupapp.StartupAppListActivity")),
        Intent().setComponent(
            ComponentName
                .unflattenFromString("com.coloros.safecenter/.permission.startupapp.StartupAppListActivity")),
        Intent().setComponent(
            ComponentName("com.coloros.safecenter",
                "com.coloros.privacypermissionsentry.PermissionTopActivity")
        ),
        Intent().setComponent(
            ComponentName.unflattenFromString("com.oppo.safe/.permission.startup.StartupAppListActivity")),

        Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
        Intent().setComponent(ComponentName.unflattenFromString("com.iqoo.secure/.phoneoptimize.BgStartUpManager")),
        Intent().setComponent(ComponentName.unflattenFromString("com.vivo.permissionmanager/.activity.BgStartUpManagerActivity")),
        Intent().setComponent(ComponentName.unflattenFromString("com.vivo.permissionmanager/.activity.PurviewTabActivity")),
        Intent().setComponent(ComponentName.unflattenFromString("com.iqoo.secure/.ui.phoneoptimize.SoftwareManagerActivity")),

        Intent().setComponent(
            ComponentName
                .unflattenFromString("com.oneplus.security/.chainlaunch.view.ChainLaunchAppListActivity")),

        Intent().setComponent(
            ComponentName.unflattenFromString("com.letv.android.letvsafe/.AutobootManageActivity")),

        Intent().setComponent(
            ComponentName.unflattenFromString("com.htc.pitroad/.landingpage.activity.LandingPageActivity")),

        Intent().setComponent(ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity")),
        Intent().setComponent(ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")),
        Intent().setComponent(ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivity")),

        Intent().setComponent(ComponentName("com.dewav.dwappmanager", "com.dewav.dwappmanager.memory.SmartClearupWhiteList"))
    )

    private val BATTERY_INTENTS = arrayOf(
        Intent().setComponent(
            ComponentName
                .unflattenFromString("com.miui.powerkeeper/.ui.HiddenAppsContainerManagementActivity")),

        Intent().setComponent(
            ComponentName
                .unflattenFromString("com.huawei.systemmanager/.power.ui.HwPowerManagerActivity")),

        Intent().setComponent(
            ComponentName
                .unflattenFromString("com.meizu.safe/.SecurityCenterActivity")),

        //samsung
        Intent().setComponent(
            ComponentName("com.samsung.android.sm_cn",
                "com.samsung.android.sm.ui.battery.AppSleepListActivity")
        ),
        Intent().setComponent(
            ComponentName("com.samsung.android.sm_cn",
                "com.samsung.android.sm.ui.battery.BatteryActivity")
        ),
        Intent().setComponent(
            ComponentName("com.samsung.android.sm",
                "com.samsung.android.sm.ui.battery.AppSleepListActivity")
        ),
        Intent().setComponent(
            ComponentName("com.samsung.android.sm",
                "com.samsung.android.sm.ui.battery.BatteryActivity")
        ),
        Intent().setComponent(
            ComponentName("com.samsung.android.lool",
                "com.samsung.android.sm.battery.ui.BatteryActivity")
        ),
        Intent().setComponent(
            ComponentName("com.samsung.android.lool",
                "com.samsung.android.sm.ui.battery.BatteryActivity")
        ),
        Intent().setComponent(
            ComponentName("com.samsung.android.sm_cn",
                "com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity")
        ),

        // oppo
        Intent().setComponent(
            ComponentName
                .unflattenFromString("com.coloros.safecenter/.appfrozen.activity.AppFrozenSettingsActivity")),
        Intent().setComponent(
            ComponentName("com.coloros.oppoguardelf",
                "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity")
        ),
        Intent().setComponent(
            ComponentName("com.coloros.oppoguardelf",
                "com.coloros.powermanager.fuelgaue.PowerSaverModeActivity")
        ),
        Intent().setComponent(
            ComponentName("com.coloros.oppoguardelf",
                "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity")
        ),
        Intent().setComponent(
            ComponentName
                .unflattenFromString("com.oppo.safe/.SecureSafeMainActivity")),

        // vivo
        Intent().setComponent(
            ComponentName("com.vivo.abe",
                "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity")
        ),
        Intent().setComponent(ComponentName.unflattenFromString("com.iqoo.powersaving/.PowerSavingManagerActivity"))
    )

    fun autoStartManager(ctx: Context): Boolean {
        for (intent in AUTO_START_INTENTS) {
            if (ctx.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                try {
                    ctx.startActivity(intent)
                    return true
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return false
    }

    fun powerManager(ctx: Context): Boolean {
        for (intent in BATTERY_INTENTS) {
            if (ctx.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                try {
                    ctx.startActivity(intent)
                    return true
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return false
    }
}