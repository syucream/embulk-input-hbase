package org.embulk.input.hbase

import org.embulk.config.{Config, ConfigDefault, ConfigInject, Task}
import org.embulk.spi.BufferAllocator

trait PluginTask extends Task {

  @Config("table")
  def getTable: String

  @Config("json_column_name")
  @ConfigDefault("\"record\"")
  def getJsonColumnName: String

  @ConfigInject
  def getBufferAllocator: BufferAllocator
}
