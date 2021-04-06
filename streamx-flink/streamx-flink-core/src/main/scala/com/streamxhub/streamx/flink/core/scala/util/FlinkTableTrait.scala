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
package com.streamxhub.streamx.flink.core.scala.util

import com.streamxhub.streamx.common.conf.ConfigConst.KEY_FLINK_SQL
import com.streamxhub.streamx.common.util.Logger
import com.streamxhub.streamx.flink.common.util.SQLCommand._
import com.streamxhub.streamx.flink.common.util.SQLCommandUtil
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.configuration.ConfigOption
import org.apache.flink.table.api.TableEnvironment
import org.apache.flink.table.api.config.{ExecutionConfigOptions, OptimizerConfigOptions, TableConfigOptions}

import java.util.{HashMap => JavaHashMap, Map => JavaMap}
import scala.util.{Failure, Success, Try}


trait FlinkTableTrait extends Logger {

  /**
   * all the available sql config options. see
   * https://ci.apache.org/projects/flink/flink-docs-release-1.10/dev/table/config.html
   */
  private lazy val tableConfigOptions: JavaMap[String, ConfigOption[_]] = {
    def extractConfig(clazz: Class[_]): JavaMap[String, ConfigOption[_]] = {
      val configOptions = new JavaHashMap[String, ConfigOption[_]]
      clazz.getDeclaredFields.foreach(field => {
        if (field.getType.isAssignableFrom(classOf[ConfigOption[_]])) {
          Try {
            val configOption = field.get(classOf[ConfigOption[_]]).asInstanceOf[ConfigOption[_]]
            configOptions.put(configOption.key, configOption)
          } match {
            case Success(_) =>
            case Failure(e) => logError("Fail to get ConfigOption", e)
          }
        }
      })
      configOptions
    }

    val configOptions = new JavaHashMap[String, ConfigOption[_]]
    val configList = List(
      //classOf[PythonOptions],
      classOf[ExecutionConfigOptions],
      classOf[OptimizerConfigOptions],
      classOf[TableConfigOptions]
    )
    configList.foreach(x => configOptions.putAll(extractConfig(x)))
    configOptions
  }

  private[core] def callSql(sql: String, parameter: ParameterTool, context: TableEnvironment): Unit = {
    val flinkSql: String = if (sql == null) parameter.get(KEY_FLINK_SQL()) else parameter.get(sql)
    //TODO registerHiveCatalog
    SQLCommandUtil.parseSQL(flinkSql).foreach(x => {
      val args = x.operands.head
      x.command match {
        case SET =>
          if (!tableConfigOptions.containsKey(args)) {
            throw new IllegalArgumentException(s"$args is not a valid table/sql config, please check link: https://ci.apache.org/projects/flink/flink-docs-release-1.10/dev/table/config.html")
          }
          context.getConfig.getConfiguration.setString(args, x.operands(1))
          logInfo(s"${x.command.name}: $args --> ${x.operands(1)}")
        case USE =>
          context.useDatabase(args)
          logInfo(s"${x.command.name}: $args")
        case USE_CATALOG =>
          context.useCatalog(args)
          logInfo(s"${x.command.name}: $args")
        case SELECT =>
          // TODO SELECT
          throw new UnsupportedOperationException(s"[StreamX] Unsupported select operation:$sql")
        case _ =>
          context.executeSql(args)
          logInfo(s"${x.command.name}:$args")
        case _ => throw new Exception(s"[StreamX] Unsupported command: ${x.command}")
      }
    })
    logInfo(s"tableSQL: $sql")
  }

}