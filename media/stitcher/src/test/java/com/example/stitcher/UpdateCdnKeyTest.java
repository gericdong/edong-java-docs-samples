/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.stitcher;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UpdateCdnKeyTest {

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);
  private static final String CLOUD_CDN_KEY_ID = TestUtils.getCdnKeyId();
  private static final String MEDIA_CDN_KEY_ID = TestUtils.getCdnKeyId();
  private static final String UPDATED_CLOUD_CDN_PRIVATE_KEY =
      "VGhpcyBpcyBhbiB1cGRhdGVkIHRlc3Qgc3RyaW5nLg==";
  private static final String UPDATED_MEDIA_CDN_PRIVATE_KEY =
      "ZZZzNDU2Nzg5MDEyMzQ1Njc4OTAxzg5MDEyMzQ1Njc4OTAxMjM0NTY3DkwMTIZZZ";

  private static String PROJECT_ID;
  private static String CLOUD_CDN_KEY_NAME; // resource name for the Cloud CDN key
  private static String MEDIA_CDN_KEY_NAME; // resource name for the Media CDN key
  private static PrintStream originalOut;
  private ByteArrayOutputStream bout;

  private static String requireEnvVar(String varName) {
    String varValue = System.getenv(varName);
    assertNotNull(
        String.format("Environment variable '%s' is required to perform these tests.", varName));
    return varValue;
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    PROJECT_ID = requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void beforeTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    TestUtils.cleanStaleCdnKeys(PROJECT_ID, TestUtils.LOCATION);
    originalOut = System.out;
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    // Cloud CDN key
    CLOUD_CDN_KEY_NAME = String.format("/locations/%s/cdnKeys/%s", TestUtils.LOCATION,
        CLOUD_CDN_KEY_ID);
    CreateCdnKey.createCdnKey(
        PROJECT_ID, TestUtils.LOCATION, CLOUD_CDN_KEY_ID, TestUtils.HOSTNAME, TestUtils.KEYNAME,
        TestUtils.CLOUD_CDN_PRIVATE_KEY, false);

    // Media CDN key
    MEDIA_CDN_KEY_NAME = String.format("/locations/%s/cdnKeys/%s", TestUtils.LOCATION,
        MEDIA_CDN_KEY_ID);
    CreateCdnKey.createCdnKey(
        PROJECT_ID, TestUtils.LOCATION, MEDIA_CDN_KEY_ID, TestUtils.HOSTNAME, TestUtils.KEYNAME,
        TestUtils.MEDIA_CDN_PRIVATE_KEY, true);

    bout.reset();
  }

  @Test
  public void test_UpdateCdnKey()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Cloud CDN key
    UpdateCdnKey.updateCdnKey(
        PROJECT_ID,
        TestUtils.LOCATION,
        CLOUD_CDN_KEY_ID,
        TestUtils.UPDATED_HOSTNAME,
        TestUtils.KEYNAME,
        UPDATED_CLOUD_CDN_PRIVATE_KEY,
        false);
    String output = bout.toString();
    assertThat(output, containsString(CLOUD_CDN_KEY_NAME));
    bout.reset();

    // Media CDN key
    UpdateCdnKey.updateCdnKey(
        PROJECT_ID,
        TestUtils.LOCATION,
        MEDIA_CDN_KEY_ID,
        TestUtils.UPDATED_HOSTNAME,
        TestUtils.KEYNAME,
        UPDATED_MEDIA_CDN_PRIVATE_KEY,
        true);
    output = bout.toString();
    assertThat(output, containsString(MEDIA_CDN_KEY_NAME));
    bout.reset();
  }

  @After
  public void tearDown()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Cloud CDN key
    DeleteCdnKey.deleteCdnKey(PROJECT_ID, TestUtils.LOCATION, CLOUD_CDN_KEY_ID);
    // Media CDN key
    DeleteCdnKey.deleteCdnKey(PROJECT_ID, TestUtils.LOCATION, MEDIA_CDN_KEY_ID);
    System.setOut(originalOut);
    bout.reset();
  }
}
