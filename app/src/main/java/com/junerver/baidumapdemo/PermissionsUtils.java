package com.junerver.baidumapdemo;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限申请工具类
 *
 * @author Lee
 */
public class PermissionsUtils {

    private final int mRequestCode = 100;
    private PermissionsResult mPermissionsResult;
    private static PermissionsUtils permissionsUtils;

    public static PermissionsUtils getInstance() {
        if (permissionsUtils == null) {
            synchronized (PermissionsUtils.class) {
                if (permissionsUtils == null) {
                    permissionsUtils = new PermissionsUtils();
                }
            }
        }
        return permissionsUtils;
    }

    public void checkPermissions(Activity activity, String[] permissions, PermissionsResult result) {
        mPermissionsResult = result;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            result.passPermission();
            return;
        }

        List<String> mPermissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }

        if (mPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(activity, permissions, mRequestCode);
        } else {
            result.passPermission();
        }
    }

    public void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        boolean hasPermissionDenied = false;
        boolean notRemindAgain = false;

        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    boolean flag = ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i]);
                    if (flag) {
                        hasPermissionDenied = true;
                    } else {
                        notRemindAgain = true;
                    }
                }
            }
            if (hasPermissionDenied) {
                mPermissionsResult.continuePermission();
            } else if (notRemindAgain) {
                mPermissionsResult.refusePermission();
            } else {
                mPermissionsResult.passPermission();
            }
        }
    }

    public interface PermissionsResult {

        /**
         * 权限全部通过
         */
        void passPermission();

        /**
         * 权限部分通过
         */
        void continuePermission();

        /**
         * 权限拒绝且不再提醒
         */
        void refusePermission();
    }
}