/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.gearpump.services

import org.apache.gearpump.cluster.MasterToAppMaster.AppMasterData
import org.apache.gearpump.cluster.master.AppMasterRuntimeInfo
import org.apache.gearpump.util.LogUtil
import org.scalatest.{Matchers, FlatSpec, BeforeAndAfterEach}
import org.slf4j.Logger
import spray.routing.RequestContext
import spray.testkit.{ScalatestRouteTest}

import scala.util.{Failure, Success}
import scala.concurrent.duration._

class AppMasterServiceSpec extends FlatSpec with ScalatestRouteTest with AppMasterService with Matchers with BeforeAndAfterEach {
  import org.apache.gearpump.services.AppMasterProtocol._
  import spray.httpx.SprayJsonSupport._
  private val LOG: Logger = LogUtil.getLogger(getClass)
  def actorRefFactory = system
  var restUtil = RestTestUtil.startRestServices

  val master = restUtil match {
    case Success(v) =>
      v.miniCluster.mockMaster
    case Failure(v) =>
      LOG.error("Could not start rest services", v)
      null
  }

  override def beforeEach : Unit = {
  }

  "AppMasterService" should "return a JSON structure for GET request when detail = false" in {

    implicit val customTimeout = RouteTestTimeout(15.seconds)
    val tranform = implicitly[TildeArrow[RequestContext , Unit]]
    (Get("/appmaster/0?detail=false") ~> routes).asInstanceOf[RouteResult] ~> check{
      //check the response type
      responseAs[AppMasterData]
    }
  }

  override def afterEach: Unit = {
    restUtil match {
      case Success(v) =>
        LOG.info("shutting down the cluster....")
        v.shutdown()
      case Failure(v) =>
        LOG.error("Could not start rest services", v)
    }
  }
}
