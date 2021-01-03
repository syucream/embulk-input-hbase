package org.embulk.input.hbase

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.{Connection, Scan}
import org.apache.hadoop.hbase.util.Bytes
import org.embulk.spi.PageBuilder
import org.embulk.spi.`type`.Types
import org.embulk.spi.json.JsonParser

import scala.collection.JavaConverters._

class HBaseClient(
    connection: Connection,
    tableName: String,
    columns: Option[Seq[String]] = None
) {
  private val table = connection.getTable(TableName.valueOf(tableName))
  private val scan = new Scan()

  private val jsonParser = new JsonParser()
  private val objectMapper = new ObjectMapper()

  columns match {
    case Some(cols) => cols.foreach(c => scan.addFamily(c.getBytes))
    case _ => // The default behavior is like "SELECT *"
  }

  def scanAsJson(pageBuilder: PageBuilder): PageBuilder = {
    val schema = pageBuilder.getSchema
    require(schema.size() == 1)
    require(schema.getColumn(0).getType == Types.JSON)

    val jsonColumn = schema.getColumn(0)
    val scanner = table.getScanner(scan)

    scanner.asScala.foreach { row =>
      // NOTE currently it supports only string
      val stringified = row.getNoVersionMap.asScala.map {
        case (cf, sub) =>
          Bytes.toString(cf) -> sub.asScala.map {
            case (q, v) =>
              Bytes.toString(q) -> Bytes.toString(v)
          }.asJava
      }.asJava
      val obj = objectMapper.writeValueAsString(stringified)
      pageBuilder.setJson(jsonColumn, jsonParser.parse(obj))
      pageBuilder.addRecord()
    }

    pageBuilder
  }

}
