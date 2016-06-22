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
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.fail;

public final class OkHttp3IdlingResourceTest {
  @Rule public final MockWebServer server = new MockWebServer();

  @Test public void nullNameThrows() {
    OkHttpClient client = new OkHttpClient();
    try {
      OkHttp3IdlingResource.create(null, client);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("name == null");
    }
  }

  @Test public void nullClientThrows() {
    try {
      OkHttp3IdlingResource.create("Ok!", null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessage("client == null");
    }
  }

  @Test public void name() {
    OkHttpClient client = new OkHttpClient();
    IdlingResource idlingResource = OkHttp3IdlingResource.create("Ok!", client);
    assertThat(idlingResource.getName()).isEqualTo("Ok!");
  }

  @Test public void idleNow() throws InterruptedException {
    server.enqueue(new MockResponse());

    final CountDownLatch requestReady = new CountDownLatch(1);
    final CountDownLatch requestProceed = new CountDownLatch(1);
    OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(new Interceptor() {
          @Override public Response intercept(Chain chain) throws IOException {
            requestReady.countDown();
            try {
              requestProceed.await(10, SECONDS);
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
            return chain.proceed(chain.request());
          }
        })
        .build();
    IdlingResource idlingResource = OkHttp3IdlingResource.create("Ok!", client);

    assertThat(idlingResource.isIdleNow()).isTrue();

    Call call = client.newCall(new Request.Builder().url(server.url("/")).build());
    call.enqueue(new Callback() {
      @Override public void onFailure(Call call, IOException e) {
        throw new AssertionError();
      }

      @Override public void onResponse(Call call, Response response) throws IOException {
        response.close();
      }
    });

    // Wait until the interceptor is called signifying we are not idle.
    requestReady.await(10, SECONDS);
    assertThat(idlingResource.isIdleNow()).isFalse();

    // Allow the request to proceed and wait for the executor to stop to signify we became idle.
    requestProceed.countDown();
    client.dispatcher().executorService().shutdown();
    client.dispatcher().executorService().awaitTermination(10, SECONDS);
    assertThat(idlingResource.isIdleNow()).isTrue();
  }

  @Test public void idleCallback() throws InterruptedException, IOException {
    server.enqueue(new MockResponse());

    OkHttpClient client = new OkHttpClient();
    IdlingResource idlingResource = OkHttp3IdlingResource.create("Ok!", client);

    final AtomicInteger count = new AtomicInteger();
    IdlingResource.ResourceCallback callback = new IdlingResource.ResourceCallback() {
      @Override public void onTransitionToIdle() {
        count.getAndIncrement();
      }
    };
    idlingResource.registerIdleTransitionCallback(callback);

    assertThat(count.get()).isEqualTo(0);

    // Use a synchronous call as a quick way to transition from busy to idle in a blocking way.
    client.newCall(new Request.Builder().url(server.url("/")).build()).execute().close();
    assertThat(count.get()).isEqualTo(1);
  }
}
