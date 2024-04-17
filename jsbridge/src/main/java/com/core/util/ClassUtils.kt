package com.core.util

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.util.Log
import com.core.JSBridge
import com.core.bridge.Constants
import com.core.thread.DefaultPoolExecutor
import dalvik.system.DexFile
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 *
 * @author like
 * @date 2024/2/7 10:00
 */
object ClassUtils {

    private const val EXTRACTED_NAME_EXT = ".classes"
    private const val EXTRACTED_SUFFIX = ".zip"

    private val SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes"

    private const val PREFS_FILE = "multidex.version"
    private const val KEY_DEX_NUMBER = "dex.number"

    private const val VM_WITH_MULTIDEX_VERSION_MAJOR = 2
    private const val VM_WITH_MULTIDEX_VERSION_MINOR = 1

    /**
     * 获取所有类
     */
    private val allClazz: MutableSet<String> = mutableSetOf()

    private fun getMultiDexPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            PREFS_FILE,
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) Context.MODE_PRIVATE else Context.MODE_PRIVATE or Context.MODE_MULTI_PROCESS
        )
    }

    /**
     * 通过组获取桥梁类
     */
    fun getBridgeByGroup(group: String?): String? {
        var result: String? = null
        if (group.isNullOrEmpty()){
            return null
        }
        val filterFileName =
            Constants.PREFIX_FILE_NAME + Constants.SEPARATOR + Constants.FILE_GROUP + Constants.SEPARATOR + group
        val fileName = Constants.PACKAGE_OF_GENERATE_FILE + "." + filterFileName
        if (JSBridge.getInstance().getClazzList().contains(fileName)) {
            result = fileName
        }
        return result
    }

    /**
     * 通过组获取方法类
     */
    fun getMethodByGroup(group: String?): String? {
        var result: String? = null
        if (group.isNullOrEmpty()){
            return null
        }
        val filterFileName =
            Constants.PREFIX_FILE_NAME + Constants.SEPARATOR + Constants.FILE_METHOD + Constants.SEPARATOR + group
        val fileName = Constants.PACKAGE_OF_GENERATE_FILE + "." + filterFileName
        if (JSBridge.getInstance().getClazzList().contains(fileName)) {
            result = fileName
        }
        return result
    }

    /**
     * 通过指定包名，扫描包下面包含的所有的ClassName
     *
     * @param [context]     U know
     * @param [packageName] 包名
     * @return 所有class的集合
     */
    @Throws(NameNotFoundException::class, IOException::class, InterruptedException::class)
    fun getFileNameByPackageName(context: Context, packageName: String): MutableSet<String> {
        val classNames: MutableSet<String> = mutableSetOf()
        val paths = getSourcePaths(context)
        val parserCtl = CountDownLatch(paths.size)
        for (path in paths) {
            DefaultPoolExecutor.getInstance().execute(Runnable {
                var dexfile: DexFile? = null
                try {
                    dexfile = if (path.endsWith(EXTRACTED_SUFFIX)) {
                        DexFile.loadDex(path, "$path.tmp", 0)
                    } else {
                        DexFile(path)
                    }
                    val dexEntries = dexfile!!.entries()
                    while (dexEntries.hasMoreElements()) {
                        val className = dexEntries.nextElement()
                        if (className.startsWith(packageName)) {
                            classNames.add(className)
                        }
                    }
                } catch (ignore: Throwable) {
                    Log.e("JSBridge", "Scan map file in dex files made error.", ignore)
                } finally {
                    if (null != dexfile) {
                        try {
                            dexfile.close()
                        } catch (ignore: Throwable) {
                        }
                    }
                    parserCtl.countDown()
                }
            })
        }
        parserCtl.await()
        return classNames
    }

    @Throws(NameNotFoundException::class, IOException::class)
    fun getSourcePaths(context: Context): MutableList<String> {
        val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
        val sourceApk = File(applicationInfo.sourceDir)
        val sourcePaths: MutableList<String> = mutableListOf()
        sourcePaths.add(applicationInfo.sourceDir)
        val extractedFilePrefix: String = sourceApk.name + EXTRACTED_NAME_EXT
        if (!isVMMultidexCapable()) {
            val totalDexNumber = getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1)
            val dexDir = File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME)
            for (secondaryNumber in 2..totalDexNumber) {
                val fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX
                val extractedFile = File(dexDir, fileName)
                if (extractedFile.isFile) {
                    sourcePaths.add(extractedFile.absolutePath)
                } else {
                    throw IOException("Missing extracted secondary dex file '" + extractedFile.path + "'")
                }
            }
        }
        return sourcePaths
    }

    private fun isVMMultidexCapable(): Boolean {
        var isMultidexCapable = false
        try {
            val versionString = System.getProperty("java.vm.version")
            if (versionString != null) {
                val matcher: Matcher =
                    Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString)
                if (matcher.matches()) {
                    try {
                        val major = matcher.group(1).toInt()
                        val minor = matcher.group(2).toInt()
                        isMultidexCapable =
                            major > VM_WITH_MULTIDEX_VERSION_MAJOR || major == VM_WITH_MULTIDEX_VERSION_MAJOR && minor >= VM_WITH_MULTIDEX_VERSION_MINOR
                    } catch (ignore: NumberFormatException) {
                        // let isMultidexCapable be false
                    }
                }
            }
        } catch (e: Exception) {

        }
        return isMultidexCapable
    }
}