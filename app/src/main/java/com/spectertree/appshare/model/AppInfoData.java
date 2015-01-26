package com.spectertree.appshare.model;

import android.graphics.drawable.Drawable;

import java.util.Date;

public class AppInfoData {
    private Drawable icon;
    private String name;
    private String packageName;
    private String versionName;
    private int versionCode;
    private String sourceDir;
    private Date date;
    private int flags;
    private long size;
    private long dataSize;

    public Drawable getAppicon() {
        return icon;
    }
    public void setAppicon(Drawable appicon) {
        this.icon = appicon;
    }
    public String getAppname() {
        return name;
    }
    public void setAppname(String appname) {
        this.name = appname;
    }
    public String getApppackagename() {
        return packageName;
    }
    public void setApppackagename(String apppackagename) {
        this.packageName = apppackagename;
    }
    public String getAppversion() {
        return versionName;
    }
    public void setAppversion(String appversion) {
        this.versionName = appversion;
    }
    public void setAppversioncode(int appversioncode) {
        this.versionCode = appversioncode;
    }
	public int getAppversioncode() {
		return versionCode;
	}
	public long getAppsize() {
		return size;
	}
	public void setAppsize(long appsize) {
		this.size = appsize;
	}
	public Date getAppdate() {
		return date;
	}
	public void setAppdate(Date appdate) {
		this.date = appdate;
    }
	public String getAppSourcedir() {
		return sourceDir;
	}
	public void setAppSourcedir(String appSourcedir) {
		this.sourceDir = appSourcedir;
	}
	public int getAppflags() {
		return flags;
	}
	public void setAppflags(int appflags) {
		this.flags = appflags;
	}
	public long getAppdatasize() {
		return dataSize;
	}
	public void setAppdatasize(long appdatasize) {
		this.dataSize = appdatasize;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AppInfoData)) {
            return false;
        }
        AppInfoData that = (AppInfoData) o;
        if (this.packageName != that.packageName) {
            return false;
        }
        if (this.dataSize != that.dataSize) {
            return false;
        }
        if (this.versionCode < that.versionCode) {
            return false;
        }

        return true;
	}
}
