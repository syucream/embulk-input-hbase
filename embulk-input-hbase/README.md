# embulk-input-hbase

An embulk input plugin for HBase data source.

## Examples

```sh
$ hbase shell ./examples/dummydata.txt
$ embulk run ./examples/hbase.yaml
...
{"cf":{"a":"value1"}}
{"cf":{"b":"value2"}}
{"cf":{"c":"value3"}}
{"cf":{"d":"value4"}}
...
```

