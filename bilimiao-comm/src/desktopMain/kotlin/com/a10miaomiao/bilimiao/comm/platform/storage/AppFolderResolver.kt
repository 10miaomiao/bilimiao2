package com.a10miaomiao.bilimiao.comm.platform.storage

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.win32.W32APIOptions
import java.io.File

data class AppInfo(
    val qualifier: String,
    val organization: String,
    val name: String,
)

data class AppDataDirectories(
    val data: File,
    val cache: File
)

interface AppFolderResolver {
    fun resolve(appInfo: AppInfo): AppDataDirectories

    companion object {
        val INSTANCE: AppFolderResolver by lazy {
            val os = System.getProperty("os.name").lowercase()
            when {
                os.contains("win") -> WindowsAppFolderResolver
                else -> UnixAppFolderResolver
            }
        }
    }
}

object WindowsAppFolderResolver : AppFolderResolver {

    private interface Shell32 : Library {
        fun SHGetFolderPathW(
            hwndOwner: Pointer?,
            nFolder: Int,
            hToken: Pointer?,
            dwFlags: Int,
            pszPath: CharArray?
        ): Int

        companion object {
            val INSTANCE: Shell32 by lazy {
                Native.load(
                    "shell32",
                    Shell32::class.java,
                    W32APIOptions.DEFAULT_OPTIONS,
                )
            }
        }
    }

    private const val CSIDL_APPDATA = 0x001A
    private const val CSIDL_LOCAL_APPDATA = 0x001C
    private const val MAX_PATH = 260

    private fun getRoamingAppDataDirectory(
        organizationName: String,
        applicationName: String
    ): File {
        return try {
            val pathBuffer = CharArray(MAX_PATH)
            val result = Shell32.INSTANCE.SHGetFolderPathW(null, CSIDL_APPDATA, null, 0, pathBuffer)

            val appDataPath = if (result == 0) {
                Native.toString(pathBuffer)
            } else {
                System.getenv("APPDATA")
                    ?: throw RuntimeException("Failed to retrieve APPDATA. SHGetFolderPath error code: $result")
            }

            File(appDataPath, "$organizationName/$applicationName").also { it.mkdirs() }
        } catch (e: Exception) {
            val appDataPath = System.getenv("APPDATA")
                ?: throw RuntimeException("APPDATA environment variable not set")
            File(appDataPath, "$organizationName/$applicationName").also { it.mkdirs() }
        }
    }

    private fun getLocalAppDataDirectory(
        organizationName: String,
        applicationName: String
    ): File {
        return try {
            val pathBuffer = CharArray(MAX_PATH)
            val result = Shell32.INSTANCE.SHGetFolderPathW(null, CSIDL_LOCAL_APPDATA, null, 0, pathBuffer)

            val localAppDataPath = if (result == 0) {
                Native.toString(pathBuffer)
            } else {
                System.getenv("LOCALAPPDATA")
                    ?: throw RuntimeException("Failed to retrieve LOCALAPPDATA. SHGetFolderPath error code: $result")
            }

            File(localAppDataPath, "$organizationName/$applicationName").also { it.mkdirs() }
        } catch (e: Exception) {
            val localAppDataPath = System.getenv("LOCALAPPDATA")
                ?: throw RuntimeException("LOCALAPPDATA environment variable not set")
            File(localAppDataPath, "$organizationName/$applicationName").also { it.mkdirs() }
        }
    }

    override fun resolve(appInfo: AppInfo): AppDataDirectories {
        val roamingDir = getRoamingAppDataDirectory(appInfo.organization, appInfo.name)
        val localDir = getLocalAppDataDirectory(appInfo.organization, appInfo.name)
        return AppDataDirectories(
            File(roamingDir, "data").also { it.mkdirs() },
            File(localDir, "cache").also { it.mkdirs() }
        )
    }
}

object UnixAppFolderResolver : AppFolderResolver {

    private fun getXdgDataHome(): String {
        return System.getenv("XDG_DATA_HOME")
            ?: File(System.getProperty("user.home"), ".local/share").absolutePath
    }

    private fun getXdgCacheHome(): String {
        return System.getenv("XDG_CACHE_HOME")
            ?: File(System.getProperty("user.home"), ".cache").absolutePath
    }

    override fun resolve(appInfo: AppInfo): AppDataDirectories {
        val dataDir = File(getXdgDataHome(), appInfo.name).also { it.mkdirs() }
        val cacheDir = File(getXdgCacheHome(), appInfo.name).also { it.mkdirs() }
        return AppDataDirectories(dataDir, cacheDir)
    }
}
