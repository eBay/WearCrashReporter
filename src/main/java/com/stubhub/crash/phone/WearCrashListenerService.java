package com.stubhub.crash.phone;

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

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.stubhub.crash.WearCrashProtocol;

import java.util.StringTokenizer;


public class WearCrashListenerService extends WearableListenerService {

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    String messagePath = messageEvent.getPath();

    //make sure it's for us
    if(messagePath.equals(WearCrashProtocol.PUSH_EXCEPTION_PATH)) {
      String payload = new String(messageEvent.getData());
      reportException(payload);
    }
  }

  private void reportException(String payload) {
    StringTokenizer st = new StringTokenizer(payload, "|");

    String exceptionJson = st.nextToken();
    String className = st.nextToken();

    Throwable exception;

    try {
      exception = (Throwable) fromJson(exceptionJson, Class.forName(className));
    } catch(ClassNotFoundException cnfe) {
      exception = fromJson(exceptionJson, Throwable.class);
    }

    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), exception);
  }

  private <T> T fromJson(String json, Class<T> classOfT) {
    Gson gsonObject = new GsonBuilder().create();
    return gsonObject.fromJson(json, classOfT);
  }
}
