# Wear Crash Reporter

Reports crashes on the watch app into the phone app's crash report system.

Works with any [third party] crash reporter installed on the Phone app.

We install a crash handler on the watch Virtual Machine, when a crash is caught, its trace and type are serialized to json, 
saved to the FileSystem, then sent with a service as a MessageApi Message. Upon reception by a WearableListenerService in 
the phone app it is deserialized and passed to the installed Phone Virtual Machine's crash reporter.

The top of the stack trace then looks like:

Thread [#] — WearableListenerService

in the crash report



Setting it up:

- Add to project build.gradle's repositories section:

maven {
     url = 'http://dl.bintray.com/stubhub/maven'
}

----------
Phone App:
----------

- Add to build.gradle:

compile "com.google.android.gms:play-services-wearable:$play_services_version"

compile 'com.stubhub.crash:wear-crash-reporter:1.0.2@aar'

- Add the following to the manifest:

<pre><code>
service name com.stubhub.crash.phone.WearCrashListenerService
  intent-filter
    action name com.google.android.gms.wearable.MESSAGE_RECEIVED
    data scheme="wear" host="*" pathPrefix "/wear-crash-peer"
</code></pre>

- Add the following item to wear.xml's android_wear_capabilities:

wear_crash_peer_capability


----------
Watch App:
----------

- Add to build.gradle:

compile "com.android.support:wear:$support_version"

compile "com.google.android.support:wearable:$latest_wear_libraries_version"
provided "com.google.android.wearable:wearable:$latest_wear_libraries_version"

compile "com.google.android.gms:play-services-wearable:$play_services_version"

compile 'com.stubhub.crash:wear-crash-reporter:1.0.2@aar'


- Add the following to the manifest:

<pre><code>
service name com.stubhub.crash.watch.WearCrashLoggingService
</code></pre>

- Call the following in your Application class' onCreate:

WearCrashHelper.getInstance().startAgent(getApplicationContext());



<h2><a href="#license" aria-hidden="true" class="anchor" id="user-content-license"><svg aria-hidden="true" class="octicon octicon-link" height="16" version="1.1" viewBox="0 0 16 16" width="16"><path fill-rule="evenodd" d="M4 9h1v1H4c-1.5 0-3-1.69-3-3.5S2.55 3 4 3h4c1.45 0 3 1.69 3 3.5 0 1.41-.91 2.72-2 3.25V8.59c.58-.45 1-1.27 1-2.09C10 5.22 8.98 4 8 4H4c-.98 0-2 1.22-2 2.5S3 9 4 9zm9-3h-1v1h1c1 0 2 1.22 2 2.5S13.98 12 13 12H9c-.98 0-2-1.22-2-2.5 0-.83.42-1.64 1-2.09V6.25c-1.09.53-2 1.84-2 3.25C6 11.31 7.55 13 9 13h4c1.45 0 3-1.69 3-3.5S14.5 6 13 6z"></path></svg></a>License</h2>
<pre><code>Copyright 2017 Ebay, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</code></pre>
