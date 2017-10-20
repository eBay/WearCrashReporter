package com.stubhub.crash.watch;

/**
 * Copyright (c) 2017 eBay Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class WearCrashHelper {  //singleton

  private static final String CRASHES_PATH = "crashes";
  private static final String CRASH_FILENAME_PREFIX = "crash";

  private Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;
  private Context mContext;


  private static final WearCrashHelper INSTANCE;

  static {
    INSTANCE = new WearCrashHelper();
  }

  public static WearCrashHelper getInstance() {
    return INSTANCE;
  }

  public void startAgent(Context context, boolean installHandler) {
    mContext = context;
    if(installHandler){
      mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

      Thread.setDefaultUncaughtExceptionHandler(mUncaughtExceptionHandler);
    }

    //send any pending crash reports
    List<File> reports = getCrashReports();
    if(reports != null && reports.size() > 0) {
      startLoggingService();
    }
  }

  private final Thread.UncaughtExceptionHandler mUncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
    @Override
    public void uncaughtException(Thread thread, Throwable e) {
      handleUncaughtException(e);

      //pass it along
      mDefaultUncaughtExceptionHandler.uncaughtException(thread, e);
    }

    private void handleUncaughtException(Throwable e) {
      //write it to file
      File crashFile = getNextCrashFile();
      if(crashFile != null) {
        e.getStackTrace(); //make sure the stacktrace gets built up

        Class<?> throwableClass = e.getClass();

        //serialize to json
        String toJson = toJson(e, throwableClass);

        //register the exception class
        String className = throwableClass.getCanonicalName();

        //write it out
        FileOutputStream outputStream = null;
        try {
          outputStream = new FileOutputStream(crashFile);
          outputStream.write((toJson + "|" + className).getBytes());

          //fire the logging service
          startLoggingService();
        } catch(IOException ioe) {
          //Do nothing
        } finally {
          if(outputStream != null) {
            try {
              outputStream.close();
            } catch(IOException ioe2) {
              //Do nothing
            }
          }
        }
      }
    }
  };

  private String toJson(Object src, Type typeOfSrc) {
    Gson gsonObject = new GsonBuilder().create();
    return gsonObject.toJson(src, typeOfSrc);
  }

  private File getNextCrashFile() {
    return new File(getCrashDirectory(), CRASH_FILENAME_PREFIX + System.currentTimeMillis());
  }

  List<File> getCrashReports() {
    File crashDir = getCrashDirectory();

    File[] files = crashDir.listFiles();

    if(files != null){
      List<File> allFiles = new ArrayList<>();
      allFiles.addAll(Arrays.asList(files));
      if(allFiles.size() > 1){
        Collections.sort(allFiles, new Comparator<File>() {
          @Override
          public int compare(File file1, File file2) {
            return file1.getName().compareTo(file2.getName());
          }
        });
      }

      return allFiles;
    }
    else{
      return null;
    }
  }

  private File getCrashDirectory() {
    File crashDir = new File(mContext.getFilesDir() + File.pathSeparator + CRASHES_PATH);
    if(!crashDir.exists()) {
      crashDir.mkdir();
    }
    return crashDir;
  }

  private void startLoggingService() {
    mContext.startService(new Intent(mContext, WearCrashLoggingService.class));
  }
}
