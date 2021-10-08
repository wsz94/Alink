package com.alibaba.alink.pipeline.classification;

import org.apache.flink.ml.api.misc.param.Params;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.BertTextClassifierTrainBatchOp;
import com.alibaba.alink.params.tensorflow.bert.BaseEasyTransferTrainParams;
import com.alibaba.alink.params.classification.TFTableModelClassificationPredictParams;
import com.alibaba.alink.pipeline.Trainer;

public class BertTextClassifier extends Trainer <BertTextClassifier, BertClassificationModel>
	implements BaseEasyTransferTrainParams <BertTextClassifier>,
	TFTableModelClassificationPredictParams <BertTextClassifier> {

	public BertTextClassifier() {this(null);}

	public BertTextClassifier(Params params) {
		super(params);
	}

	@Override
	protected BatchOperator <?> train(BatchOperator <?> in) {
		return new BertTextClassifierTrainBatchOp(this.getParams()).linkFrom(in);
	}
}