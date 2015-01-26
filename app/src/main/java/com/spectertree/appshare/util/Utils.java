
package com.spectertree.appshare.util;

import com.spectertree.appshare.model.AppInfoData;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Utils {

    public interface Constants {
        public static final String MIME_APP = "application/vnd.android.package-archive";
        public static final String MIME_AUDIO = "audio/*";
        public static final String MIME_IMAGE = "image/*";
        public static final String MIME_VIDEO = "video/*";
        //public static final String MIME_APP = "application/*";
        public static final int REQ_SYSTEM_SETTINGS = 0;
        public static final int REQ_BACKUP_DERICTORY = 1;
    }

    public interface Setting {
        public static final String KEY_BAKCUP_DIR = "backup_dir";
        public static final String KEY_DEFAULT_FILTER = "default_filter";
        public static final String KEY_DEFAULT_SORTING = "default_sorting";
        public static final String KEY_DEFAULT_ITEM_CLICK = "default_item_click";
        public static final String KEY_FORMAT_EXPORT_NAME = "format_export_name";
        public static final String KEY_HEIGHLIGHT_NOT_INSTALLED = "highlight_not_installed";

        public static final String DEFAULT_BACKUP_DIR = "/sdcard/apkbackup";
        public static final String DEFAULT_FILTER = "user";
        public static final String DEFAULT_SORTING = "date";
        public static final String DEFAULT_ITEM_CLICK = "display";
        public static final String DEFAULT_FORMAT_EXPORT_NAME = "appname";

        public static final String FILTER_TYPE_ALL = "all";
        public static final String FILTER_TYPE_USER = "user";
        public static final String FILTER_TYPE_SYSTEM = "system";

        public static final String SORTING_TYPE_NAME = "name";
        public static final String SORTING_TYPE_SIZE = "size";
        public static final String SORTING_TYPE_DATE = "date";
        public static final String SORTING_TYPE_ASC = "asc";
        public static final String SORTING_TYPE_DES = "des";

        public static final String FORMAT_EXPORT_NAME_APPNAME = "appname";
        public static final String FORMAT_EXPORT_NAME_PACKAGENAME = "packagename";
    }

    public static String getAppSize(ApplicationInfo ai) {
        File file = new File(ai.sourceDir);
        float tmpSize = file.length()/1024f;
        String appSize = "";
        if (tmpSize > 1024f) {
            appSize = new DecimalFormat("#.##").format(tmpSize/1024f) + "MB";
        } else {
            appSize = new DecimalFormat("#.##").format(tmpSize) + "KB";
        }

        return appSize;
    }

    public static String getFormatAppSize(long fileSize) {
        float tmpSize = fileSize/1024f;
        String appSize = "";
        if (tmpSize > 1024f) {
            appSize = new DecimalFormat("#.##").format(tmpSize/1024f) + "MB";
        } else {
            appSize = new DecimalFormat("#.##").format(tmpSize) + "KB";
        }

        return appSize;
    }

    public static String getAppInstallTime(ApplicationInfo ai) {
        return DateFormat.format("yyyy-MM-dd",
                new Date(new File(ai.sourceDir).lastModified())).toString();
    }

    public static List<File> getFiles(String sourceDir, String type) throws IOException {
        List<File> mFileList = new ArrayList<File>();
        File[] file = (new File(sourceDir)).listFiles();

		if (file != null) {
			for (int i = 0; i < file.length; i++) {
				if (file[i].isFile() && file[i].getName().contains("." + type)) {
					mFileList.add(file[i]);
				}
				if (file[i].isDirectory()) {
					mFileList.addAll(getFiles(file[i].getPath(), type));
				}
			}
		}

        return mFileList;
    }

    public static ProgressDialog createProcessDialog(Context ctx, String title, String msg) {
        ProgressDialog dialog = new ProgressDialog(ctx);
        dialog.setIcon(android.R.drawable.ic_dialog_info);
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setCancelable(true);

        return dialog;
    }

    public static boolean isFileExist(String dir, String fileName) throws IOException {
        List<File> list = getFiles(dir, "apk");
        for (File f : list) {
            if (f.getName().equals(fileName))
                return true;
        }

        return false;
    }


    /**
     * Get not installed apk info
     *
     * @param ctx
     * @param apkPath
     * @return AppInfoData
     */
    public static AppInfoData getApkFileInfo(Context ctx, String apkPath) {
        //System.out.println(apkPath);
        File apkFile = new File(apkPath);
        if (!apkFile.exists() || !apkPath.toLowerCase().endsWith(".apk")) {
            //System.out.println("file path is Incorrect");
            return null;
        }
        AppInfoData appInfoData;
        String PATH_PackageParser = "android.content.pm.PackageParser";
        String PATH_AssetManager = "android.content.res.AssetManager";
        try {
            //反射得到pkgParserCls对象并实例化,有参数
            Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
            Class<?>[] typeArgs = {String.class};
            Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs = {apkPath};
            Object pkgParser = pkgParserCt.newInstance(valueArgs);

            //从pkgParserCls类得到parsePackage方法
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();//这个是与显示有关的, 这边使用默认
            typeArgs = new Class<?>[]{File.class,String.class,
                                    DisplayMetrics.class,int.class};
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(
                    "parsePackage", typeArgs);

            valueArgs=new Object[]{new File(apkPath),apkPath,metrics,0};

            //执行pkgParser_parsePackageMtd方法并返回
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
                    valueArgs);

            //从返回的对象得到名为"applicationInfo"的字段对象
            if (pkgParserPkg==null) {
                return null;
            }
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField(
                    "applicationInfo");

            //从对象"pkgParserPkg"得到字段"appInfoFld"的值
            if (appInfoFld.get(pkgParserPkg)==null) {
                return null;
            }
            ApplicationInfo info = (ApplicationInfo) appInfoFld
                    .get(pkgParserPkg);

            //反射得到assetMagCls对象并实例化,无参
            Class<?> assetMagCls = Class.forName(PATH_AssetManager);
            Object assetMag = assetMagCls.newInstance();
            //从assetMagCls类得到addAssetPath方法
            typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
                    "addAssetPath", typeArgs);
            valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            //执行assetMag_addAssetPathMtd方法
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);


            //得到Resources对象并实例化,有参数
            Resources res = ctx.getResources();
            typeArgs = new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();
            Constructor<Resources> resCt = Resources.class
                    .getConstructor(typeArgs);
            valueArgs = new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();
            res = (Resources) resCt.newInstance(valueArgs);

            // 读取apk文件的信息
            appInfoData = new AppInfoData();
            if (info!=null) {
                if (info.icon != 0) {// 图片存在，则读取相关信息
                    Drawable icon = res.getDrawable(info.icon);// 图标
                    appInfoData.setAppicon(icon);
                } else {
                    Drawable icon = res.getDrawable(com.spectertree.appshare.R.drawable.app_icon);// 图标
                    appInfoData.setAppicon(icon);
                }
//                if (info.labelRes != 0) {
//                    String neme = (String) res.getText(info.labelRes);// 名字
//                    appInfoData.setAppname(neme);
//                }else {
                    String apkName=apkFile.getName();
                    appInfoData.setAppname(apkName.substring(0,apkName.lastIndexOf(".")));
//                }
                String pkgName = info.packageName;// 包名
                appInfoData.setApppackagename(pkgName);
            }else {
                return null;
            }
            PackageManager pm = ctx.getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                appInfoData.setAppversion(packageInfo.versionName);//版本号
                appInfoData.setAppversioncode(packageInfo.versionCode);//版本码
            }
            appInfoData.setAppsize(apkFile.length());
            appInfoData.setAppdate(new Date(apkFile.lastModified()));
            appInfoData.setAppSourcedir(apkFile.getName());
            return appInfoData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Copy file from source to target
     *
     * @param sourceFile
     * @param targetFile
     * @throws IOException
     */
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        FileInputStream input = new FileInputStream(sourceFile);
        BufferedInputStream inBuff = new BufferedInputStream(input);

        FileOutputStream output = new FileOutputStream(targetFile);
        BufferedOutputStream outBuff = new BufferedOutputStream(output);

        byte[] b = new byte[1024 * 5];
        int len;
        while ((len = inBuff.read(b)) != -1) {
            outBuff.write(b, 0, len);
        }
        outBuff.flush();

        inBuff.close();
        outBuff.close();
        output.close();
        input.close();
    }

    /**
     * Copy dir and content from source to target
     *
     * @param sourceDirTAG
     * @param targetDir
     * @throws IOException
     */
    public static void copyDirectiory(String sourceDir, String targetDir) throws IOException {
        (new File(targetDir)).mkdirs();
        File[] file = (new File(sourceDir)).listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                File sourceFile = file[i];
                File targetFile = new File(new File(targetDir).getAbsolutePath() + File.separator
                        + file[i].getName());
                copyFile(sourceFile, targetFile);
            }
            if (file[i].isDirectory()) {
                String dir1 = sourceDir + "/" + file[i].getName();
                String dir2 = targetDir + "/" + file[i].getName();
                copyDirectiory(dir1, dir2);
            }
        }
    }

    public static void changeSort(List<AppInfoData> list, final String type) {
        if (!list.isEmpty()) {
            Collections.sort(list, new Comparator<AppInfoData>() {
                @Override
                public int compare(AppInfoData lhs, AppInfoData rhs) {
                    int result = 0;
                    if (type.equals("name")) {
                        Collator myCollator = Collator.getInstance(java.util.Locale.CHINA);
                        result = myCollator.compare(lhs.getAppname(), rhs.getAppname());
                        /*if (myCollator.compare(lhs.getAppname(), rhs.getAppname()) < 0)
                            result = -1;
                        else if (myCollator.compare(lhs.getAppname(), rhs.getAppname()) > 0)
                            result = 1;
                        else
                            result = 0;*/
                        //result = lhs.getAppname().compareTo(rhs.getAppname());
                    } else if (type.equals("size")) {
                        Long l = (Long)(lhs.getAppsize() - rhs.getAppsize());
                        result = l.intValue();
                    } else if (type.equals("date")) {
                        result = lhs.getAppdate().compareTo(rhs.getAppdate());
                    } else if (type.equals("location")) {
                        result = (lhs.getAppflags()&ApplicationInfo.FLAG_EXTERNAL_STORAGE) -
                                (rhs.getAppflags()&ApplicationInfo.FLAG_EXTERNAL_STORAGE);
                    }
                    return result;
                }
            });
        }
    }

    public static boolean isSdcardExist() {
        String status = Environment.getExternalStorageState();

        if (status.equals(Environment.MEDIA_SHARED) ||
                status.equals(Environment.MEDIA_UNMOUNTED)) {
            return false;
        } else if (status.equals(Environment.MEDIA_REMOVED)) {
            return false;
        } else if (status.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }

        return true;
    }

}
