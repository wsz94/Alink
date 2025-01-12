package org.apache.flink.connectors.hive;

import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.common.io.RichInputFormat;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.CatalogTable;
import org.apache.flink.table.catalog.hive.HiveCatalog;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.factories.DynamicTableFactory;
import org.apache.flink.table.factories.TableSinkFactory;
import org.apache.flink.table.factories.TableSourceFactory.Context;
import org.apache.flink.table.runtime.types.TypeInfoDataTypeConverter;
import org.apache.flink.table.types.DataType;
import org.apache.flink.types.Row;

import org.apache.hadoop.mapred.JobConf;

import java.util.List;
import java.util.Map;

import static org.apache.flink.util.Preconditions.checkNotNull;

public class InputOutputFormat {

	public static Tuple3 <TableSchema, TypeInformation<RowData>, RichInputFormat <RowData, ?>> createInputFormat(
		Catalog catalog, Context context, List <Map <String, String>> remainingPartitions) {

		if (!(catalog instanceof HiveCatalog)) {
			throw new RuntimeException("Catalog should be hive catalog.");
		}

		HiveCatalog hiveCatalog = (HiveCatalog) catalog;
		CatalogTable table = checkNotNull(context.getTable());

		HiveBatchAndStreamTableSource hiveTableSource = new HiveBatchAndStreamTableSource(
			new JobConf(hiveCatalog.getHiveConf()),
			context.getConfiguration(),
			context.getObjectIdentifier().toObjectPath(),
			table);

		if (remainingPartitions != null) {
			hiveTableSource.applyPartitions(remainingPartitions);
		}

		return Tuple3.of(
			hiveTableSource.getTableSchema(),
			(TypeInformation<RowData>) TypeInfoDataTypeConverter
				.fromDataTypeToTypeInfo(hiveTableSource.getProducedDataType()),
			hiveTableSource.getInputFormat()
		);
	}

	public static OutputFormat <Row> createOutputFormat(
		Catalog catalog, DynamicTableFactory.Context context, Map <String, String> partitions, Boolean overwriteSink) {

		if (!(catalog instanceof HiveCatalog)) {
			throw new RuntimeException("Catalog should be hive catalog.");
		}

		HiveCatalog hiveCatalog = (HiveCatalog) catalog;

		HiveBatchAndStreamTableSink hiveTableSink = new HiveBatchAndStreamTableSink(
			context.getConfiguration(),
			new JobConf(hiveCatalog.getHiveConf()),
			context.getObjectIdentifier(),
			context.getCatalogTable(), null);

		if (partitions != null) {
			hiveTableSink.applyStaticPartition(partitions);
		}

        hiveTableSink.applyOverwrite(overwriteSink);

		return hiveTableSink.getOutputFormat();
	}
}
