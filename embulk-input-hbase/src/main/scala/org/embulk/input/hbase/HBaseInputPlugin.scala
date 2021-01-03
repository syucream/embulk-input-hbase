package org.embulk.input.hbase

import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory}
import org.embulk.config.{ConfigDiff, ConfigSource, TaskReport, TaskSource}
import org.embulk.spi.`type`.Types
import org.embulk.spi.PageBuilder
import org.embulk.spi.{Exec, InputPlugin, PageOutput, Schema}

class HBaseInputPlugin() extends InputPlugin {
  private val TASK_COUNT = 1

  override def transaction(
      config: ConfigSource,
      control: InputPlugin.Control
  ): ConfigDiff = {
    val task = config.loadConfig(classOf[PluginTask])

    val schema = Schema
      .builder()
      .add(task.getJsonColumnName, Types.JSON)
      .build()

    resume(task.dump(), schema, TASK_COUNT, control)
  }

  override def resume(
      taskSource: TaskSource,
      schema: Schema,
      taskCount: Int,
      control: InputPlugin.Control
  ): ConfigDiff = {
    // XXX unimplemented
    control.run(taskSource, schema, taskCount)
    Exec.newConfigDiff()
  }

  override def run(
      taskSource: TaskSource,
      schema: Schema,
      taskIndex: Int,
      output: PageOutput
  ): TaskReport = {
    val task = taskSource.loadTask(classOf[PluginTask])
    val allocator = task.getBufferAllocator
    val pageBuilder = new PageBuilder(allocator, schema, output)

    val client = new HBaseClient(createConnection(), task.getTable)
    client.scanAsJson(pageBuilder)
    pageBuilder.finish()

    Exec.newTaskReport()
  }

  override def guess(config: ConfigSource): ConfigDiff =
    Exec.newConfigDiff()

  override def cleanup(
      taskSource: TaskSource,
      schema: Schema,
      taskCount: Int,
      successTaskReports: java.util.List[TaskReport]
  ): Unit = {}

  def createConnection(): Connection =
    ConnectionFactory.createConnection()

}
