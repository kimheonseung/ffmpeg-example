package com.devh.ffmpeg;

import com.devh.ffmpeg.helper.WorkSheetHelper;

public class Main {

    public static void main(String[] args) {
    	final String workDir = args[0];
    	final String resultDir = args[1];
	    WorkSheetHelper workSheetHelper = new WorkSheetHelper();
//	    workSheetHelper.setWorkDirectory("C:\\Download");
//	    workSheetHelper.setResultDirectory("D:\\ffmepgresult");
	    workSheetHelper.setWorkDirectory(workDir);
	    workSheetHelper.setResultDirectory(resultDir);
	    workSheetHelper.work();
    }
}
