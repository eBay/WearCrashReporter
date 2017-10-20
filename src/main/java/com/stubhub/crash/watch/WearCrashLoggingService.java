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

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import com.stubhub.crash.WearCrashProtocol;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class WearCrashLoggingService extends IntentService {

  public WearCrashLoggingService() {
    super("WearCrashLoggingService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    GoogleApiClient googleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addApi(Wearable.API).build();

    ConnectionResult connectionResult = googleApiClient.blockingConnect(WearCrashProtocol.PLAY_SERVICES_CONNECT_TIMEOUT, TimeUnit.SECONDS);

    if(connectionResult.isSuccess()) {
      CapabilityApi.GetCapabilityResult result = Wearable.CapabilityApi.getCapability(googleApiClient, WearCrashProtocol.WEAR_CRASH_PEER_CAPABILITY, CapabilityApi.FILTER_REACHABLE).await();

      CapabilityInfo capabilityInfo = result.getCapability();

      if(capabilityInfo != null) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();

        String peerNodeId = pickBestNodeId(connectedNodes);

        if(peerNodeId != null){
          //get crash files, oldest first
          List<File> crashes = WearCrashHelper.getInstance().getCrashReports();
          //only send one at a time
          if(crashes != null && crashes.size() > 0) {
            try {
              //read file contents
              final File crashFile = crashes.get(0);
              int fileLength = (int) crashFile.length();
              byte payloadBytes[] = new byte[fileLength];
              BufferedInputStream bis = new BufferedInputStream(new FileInputStream(crashFile));
              DataInputStream dis = new DataInputStream(bis);
              dis.readFully(payloadBytes);

              //send it out
              Wearable.MessageApi.sendMessage(googleApiClient, peerNodeId, WearCrashProtocol.PUSH_EXCEPTION_PATH, payloadBytes).setResultCallback(
                  new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                      if(sendMessageResult.getStatus().isSuccess()) {
                        //cleanup
                        crashFile.delete();
                      }
                    }
                  });
            } catch(IOException e) {
              //Do nothing
            }
          }
        }
      }
    }
  }

  private static String pickBestNodeId(Set<Node> nodes) {
    String bestNodeId = null;
    // Find a nearby node or pick one arbitrarily
    for(Node node : nodes) {
      if(node.isNearby()) {
        return node.getId();
      }
      bestNodeId = node.getId();
    }
    return bestNodeId;
  }
}
