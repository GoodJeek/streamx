/*
 * Copyright (c) 2019 The StreamX Project
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.streamxhub.streamx.plugin.profiling;

import java.util.concurrent.atomic.AtomicLong;

import com.streamxhub.streamx.plugin.profiling.util.AgentLogger;

/** @author benjobs */
public class ProfilerRunner implements Runnable {
  private static final AgentLogger logger = AgentLogger.getLogger(ProfilerRunner.class.getName());

  private static final int MAX_ERROR_COUNT_TO_LOG = 100;

  private final Profiler profiler;
  private final AtomicLong errorCounter = new AtomicLong(0);

  public ProfilerRunner(Profiler profiler) {
    this.profiler = profiler;
  }

  @Override
  public void run() {
    try {
      profiler.profile();
    } catch (Throwable e) {
      long errorCountValue = errorCounter.incrementAndGet();
      if (errorCountValue <= MAX_ERROR_COUNT_TO_LOG) {
        logger.warn("Failed to run profile: " + profiler, e);
      } else {
        e.printStackTrace();
      }
    }
  }
}
