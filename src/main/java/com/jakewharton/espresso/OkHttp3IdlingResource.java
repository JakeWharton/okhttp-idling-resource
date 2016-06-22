/*
 * Copyright (C) 2016 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jakewharton.espresso;

import android.support.test.espresso.IdlingResource;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 *
 */
public final class OkHttp3IdlingResource implements IdlingResource {
  public static OkHttp3IdlingResource create(String name, OkHttpClient client) {
    if (name == null) throw new NullPointerException("name == null");
    if (client == null) throw new NullPointerException("client == null");
    return new OkHttp3IdlingResource(name, client.dispatcher());
  }

  private final String name;
  private final Dispatcher dispatcher;
  volatile ResourceCallback callback;

  private OkHttp3IdlingResource(String name, Dispatcher dispatcher) {
    this.name = name;
    this.dispatcher = dispatcher;
    dispatcher.setIdleCallback(new Runnable() {
        @Override public void run() {
          ResourceCallback callback = OkHttp3IdlingResource.this.callback;
          if (callback != null) {
            callback.onTransitionToIdle();
          }
        }
      });
  }

  @Override public String getName() {
    return name;
  }

  @Override public boolean isIdleNow() {
    return dispatcher.runningCallsCount() == 0;
  }

  @Override public void registerIdleTransitionCallback(ResourceCallback callback) {
    this.callback = callback;
  }
}
